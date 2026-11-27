package com.payroll.reconciliation.service;

import com.payroll.reconciliation.entity.MonthlyEntry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service responsible for calculating monthly payroll balances.
 *
 * <p>Implements the core business rules for determining what the employer owes
 * the employee each month, based on actual hours worked, paystub (LCA) amounts,
 * insurance deductions, and other adjustments.</p>
 *
 * <p>Key business rules:
 * <ul>
 *   <li>Actual Earnings = Hours Worked × Employee Hourly Rate</li>
 *   <li>Monthly Balance = Actual Earnings - Paystub (LCA)</li>
 *   <li>If Actual &lt; LCA, the shortfall reduces the cumulative balance</li>
 *   <li>Fees/Perm payments (negative adjustments) deducted from balance</li>
 *   <li>Insurance deductions reduce balance directly</li>
 *   <li>Direct employer payments are recorded separately in the direct_payments
 *       table and deducted from the total running balance — NOT per monthly entry</li>
 * </ul>
 * </p>
 *
 * @author PayrollReconciliation
 */
@Service
public class BalanceCalculationService {

    /**
     * Calculates the monthly balance based on hours, rate, paystub, and deductions.
     *
     * <p>Calculation steps:
     * <ol>
     *   <li>Actual Earnings = Hours × Rate</li>
     *   <li>Monthly Balance = Actual - Paystub (LCA).
     *       If Actual &lt; LCA, the negative amount is taken from cumulative balance.</li>
     *   <li>Fees/Perm payments (negative adjustments) deducted from balance.</li>
     *   <li>Insurance deductions reduce balance directly.</li>
     *   <li>Positive adjustments added after all deductions.</li>
     * </ol>
     * </p>
     *
     * @param hoursWorked        total hours worked in the month
     * @param employeeHourlyRate the hourly rate for the employee
     * @param paystubAmount      the LCA/paystub amount received from payroll
     * @param insuranceDeduction insurance deduction amount for the month
     * @param otherAdjustment    other adjustments (negative = fee/deduction, positive = credit)
     * @return a {@link MonthlyCalculation} record containing actual earnings and final monthly balance
     */
    public MonthlyCalculation calculate(BigDecimal hoursWorked,
                                        BigDecimal employeeHourlyRate,
                                        BigDecimal paystubAmount,
                                        BigDecimal insuranceDeduction,
                                        BigDecimal otherAdjustment) {
        BigDecimal hours      = value(hoursWorked);
        BigDecimal rate       = value(employeeHourlyRate);
        BigDecimal paystub    = value(paystubAmount);
        BigDecimal insurance  = value(insuranceDeduction);
        BigDecimal adjustment = value(otherAdjustment);

        // Step 1: Actual Earnings = Hours × Rate
        BigDecimal actual = money(hours.multiply(rate));

        // Step 2: Monthly Balance = Actual - Paystub (LCA)
        BigDecimal monthlyBalance = actual.subtract(paystub);

        // Step 3: Deduct negative adjustments (fees, perm payments)
        BigDecimal negativeAdjustment = adjustment.signum() < 0 ? adjustment : BigDecimal.ZERO;
        BigDecimal positiveAdjustment = adjustment.signum() > 0 ? adjustment : BigDecimal.ZERO;
        monthlyBalance = monthlyBalance.add(negativeAdjustment);

        // Step 4: Deduct insurance
        monthlyBalance = monthlyBalance.subtract(insurance);

        // Step 5: Add positive adjustments
        monthlyBalance = monthlyBalance.add(positiveAdjustment);

        return new MonthlyCalculation(money(actual), money(monthlyBalance));
    }

    /**
     * Applies the balance calculation to a {@link MonthlyEntry} entity,
     * setting the computed {@code actualEarnings} and {@code monthlyBalance} fields.
     *
     * @param entry the monthly entry entity to update with calculated values
     */
    public void apply(MonthlyEntry entry) {
        MonthlyCalculation calc = calculate(
                entry.getHoursWorked(),
                entry.getEmployeeHourlyRate(),
                entry.getPaystubAmountReceived(),
                entry.getInsuranceDeduction(),
                entry.getOtherAdjustment()
        );
        entry.setActualEarnings(calc.actualEarnings());
        entry.setMonthlyBalance(calc.finalBalance());
    }

    /**
     * Returns zero if the given value is null, otherwise returns the value as-is.
     *
     * @param value the BigDecimal to null-check
     * @return the value, or {@link BigDecimal#ZERO} if null
     */
    private BigDecimal value(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Rounds a BigDecimal to 2 decimal places using HALF_UP rounding.
     *
     * @param value the value to round
     * @return the value rounded to 2 decimal places
     */
    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Immutable record holding the results of a monthly balance calculation.
     *
     * @param actualEarnings computed actual earnings (hours × rate)
     * @param finalBalance   final monthly balance after all deductions
     */
    public record MonthlyCalculation(
            BigDecimal actualEarnings,
            BigDecimal finalBalance
    ) {}
}

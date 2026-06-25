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
 * direct employer payments, insurance deductions, and other adjustments.</p>
 *
 * <p>Key business rules:
 * <ul>
 *   <li>Actual Earnings = Hours Worked × Employee Hourly Rate</li>
 *   <li>Monthly Balance = Actual Earnings - Paystub (LCA)</li>
 *   <li>If Actual &lt; LCA, the shortfall reduces the cumulative balance</li>
 *   <li>Fees/Perm payments (negative adjustments) deducted BEFORE 80-20 tax reduction</li>
 *   <li>80-20 tax reduction (×0.80) ONLY applies when employee receives direct payment from employer</li>
 *   <li>Direct employer payment is grossed up (÷ 0.80) before deduction</li>
 *   <li>Insurance deductions reduce balance directly</li>
 * </ul>
 * </p>
 *
 * @author PayrollReconciliation
 */
@Service
public class BalanceCalculationService {

    /** The net rate factor for the 80-20 split (employee receives 80% after tax). */
    private static final BigDecimal DIRECT_PAYMENT_NET_RATE = new BigDecimal("0.80");

    /**
     * Calculates the monthly balance based on all input parameters.
     *
     * <p>Calculation steps:
     * <ol>
     *   <li>Actual Earnings = Hours × Rate</li>
     *   <li>Monthly Balance = Actual - Paystub (LCA).
     *       If Actual &lt; LCA, the negative amount is taken from cumulative balance.</li>
     *   <li>Fees/Perm payments (negative adjustments) deducted from balance
     *       BEFORE any after-tax (80-20) reduction.</li>
     *   <li>After-tax 80-20 reduction (×0.80) ONLY applied when:
     *       employee is actually TAKING/RECEIVING money from the employer
     *       ({@code directEmployerPayment > 0}). Otherwise, raw balance stands as-is.</li>
     *   <li>Direct employer payment grossed up (÷ 0.80) and subtracted from balance.</li>
     *   <li>Insurance deductions reduce balance directly.</li>
     *   <li>Positive adjustments added after all deductions.</li>
     * </ol>
     * </p>
     *
     * @param hoursWorked           total hours worked in the month
     * @param employeeHourlyRate    the 80-20 hourly rate for the employee
     * @param paystubAmount         the LCA/paystub amount received from payroll
     * @param directEmployerPayment net amount received directly from employer (if any)
     * @param insuranceDeduction    insurance deduction amount for the month
     * @param otherAdjustment       other adjustments (negative = fee/deduction, positive = credit)
     * @return a {@link MonthlyCalculation} record containing actual earnings,
     *         grossed-up employer payment, and final monthly balance
     */
    public MonthlyCalculation calculate(BigDecimal hoursWorked,
                                        BigDecimal employeeHourlyRate,
                                        BigDecimal paystubAmount,
                                        BigDecimal directEmployerPayment,
                                        BigDecimal insuranceDeduction,
                                        BigDecimal otherAdjustment) {
        BigDecimal hours = value(hoursWorked);
        BigDecimal rate = value(employeeHourlyRate);
        BigDecimal paystub = value(paystubAmount);
        BigDecimal direct = value(directEmployerPayment);
        BigDecimal insurance = value(insuranceDeduction);
        BigDecimal adjustment = value(otherAdjustment);

        // Step 1: Actual Earnings = Hours × Rate
        BigDecimal actual = money(hours.multiply(rate));

        // Step 2: Monthly Balance = Actual - Paystub (LCA)
        // If Actual < LCA, the negative difference is taken from cumulative balance
        BigDecimal monthlyBalance = actual.subtract(paystub);

        // Step 3: Deduct fees/perm payments (negative adjustments) BEFORE tax reduction
        BigDecimal negativeAdjustment = adjustment.signum() < 0 ? adjustment : BigDecimal.ZERO;
        BigDecimal positiveAdjustment = adjustment.signum() > 0 ? adjustment : BigDecimal.ZERO;
        monthlyBalance = monthlyBalance.add(negativeAdjustment);

        // Step 4: Apply 80-20 after-tax (×0.80) ONLY when employee is receiving
        // money directly from the employer. If no direct payment, no tax reduction.
        if (direct.signum() > 0 && monthlyBalance.signum() > 0) {
            monthlyBalance = money(monthlyBalance.multiply(DIRECT_PAYMENT_NET_RATE));
        }

        // Step 5: Direct employer payment gross-up (÷ 0.80)
        // Only when employee receives money directly from employer
        BigDecimal directGross = direct.signum() > 0
                ? money(direct.divide(DIRECT_PAYMENT_NET_RATE, 2, RoundingMode.HALF_UP))
                : BigDecimal.ZERO;

        // Step 6: Final balance = balance - direct gross - insurance + positive adjustments
        BigDecimal finalBalance = monthlyBalance
                .subtract(directGross)
                .subtract(insurance)
                .add(positiveAdjustment);

        return new MonthlyCalculation(money(actual), money(directGross), money(finalBalance));
    }

    /**
     * Applies the balance calculation to a {@link MonthlyEntry} entity,
     * setting the computed {@code actualEarnings} and {@code monthlyBalance} fields.
     *
     * <p>This method should be called before persisting the entry to ensure
     * derived fields are always consistent with the input fields.</p>
     *
     * @param entry the monthly entry entity to update with calculated values
     */
    public void apply(MonthlyEntry entry) {
        MonthlyCalculation calculation = calculate(
                entry.getHoursWorked(),
                entry.getEmployeeHourlyRate(),
                entry.getPaystubAmountReceived(),
                entry.getDirectEmployerPayment(),
                entry.getInsuranceDeduction(),
                entry.getOtherAdjustment()
        );
        entry.setActualEarnings(calculation.actualEarnings());
        entry.setMonthlyBalance(calculation.finalBalance());
    }

    /**
     * Calculates the gross employer payment from the net amount received.
     *
     * <p>Since the employee receives only 80% (net) of the actual amount,
     * the gross is calculated as: {@code gross = net / 0.80}</p>
     *
     * @param amountReceived the net amount received by the employee
     * @return the gross amount, or zero if no payment was received
     */
    public BigDecimal grossEmployerPayment(BigDecimal amountReceived) {
        BigDecimal direct = value(amountReceived);
        if (direct.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return money(direct.divide(DIRECT_PAYMENT_NET_RATE, 2, RoundingMode.HALF_UP));
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
     * @param actualEarnings              the computed actual earnings (hours × rate)
     * @param directEmployerGrossAmount   the grossed-up employer payment (net / 0.80)
     * @param finalBalance                the final monthly balance after all adjustments
     */
    public record MonthlyCalculation(
            BigDecimal actualEarnings,
            BigDecimal directEmployerGrossAmount,
            BigDecimal finalBalance
    ) {}
}

package com.payroll.reconciliation.service;

import com.payroll.reconciliation.entity.MonthlyEntry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service responsible for calculating monthly payroll balances.
 *
 * <p>Calculation: Actual Earnings − Paystub (LCA) − Insurance</p>
 *
 * <p>Direct payments, fees, and personal expenses are recorded in the
 * direct_payments table and deducted from the total running balance on
 * the dashboard — not per monthly entry.</p>
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class BalanceCalculationService {

    /**
     * Calculates the monthly balance.
     *
     * @param hoursWorked        total hours worked
     * @param employeeHourlyRate the hourly rate
     * @param paystubAmount      the LCA/paystub amount
     * @param insuranceDeduction insurance deduction (auto-populated from insurance_rates)
     * @return a {@link MonthlyCalculation} with actual earnings and final balance
     */
    public MonthlyCalculation calculate(BigDecimal hoursWorked,
                                        BigDecimal employeeHourlyRate,
                                        BigDecimal paystubAmount,
                                        BigDecimal insuranceDeduction) {
        BigDecimal hours     = value(hoursWorked);
        BigDecimal rate      = value(employeeHourlyRate);
        BigDecimal paystub   = value(paystubAmount);
        BigDecimal insurance = value(insuranceDeduction);

        // Actual = Hours × Rate
        BigDecimal actual = money(hours.multiply(rate));

        // Balance = Actual − Paystub − Insurance
        BigDecimal balance = actual.subtract(paystub).subtract(insurance);

        return new MonthlyCalculation(actual, money(balance));
    }

    /**
     * Applies the calculation to a {@link MonthlyEntry} entity.
     *
     * @param entry the entry to update
     */
    public void apply(MonthlyEntry entry) {
        MonthlyCalculation calc = calculate(
                entry.getHoursWorked(),
                entry.getEmployeeHourlyRate(),
                entry.getPaystubAmountReceived(),
                entry.getInsuranceDeduction()
        );
        entry.setActualEarnings(calc.actualEarnings());
        entry.setMonthlyBalance(calc.finalBalance());
    }

    private BigDecimal value(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Holds the results of a monthly balance calculation.
     *
     * @param actualEarnings computed actual earnings (hours × rate)
     * @param finalBalance   final monthly balance after all deductions
     */
    public record MonthlyCalculation(BigDecimal actualEarnings, BigDecimal finalBalance) {}
}

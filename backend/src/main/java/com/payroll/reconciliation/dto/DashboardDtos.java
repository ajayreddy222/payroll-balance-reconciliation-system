package com.payroll.reconciliation.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Objects for dashboard summary API responses.
 *
 * @author Payroll Reconciliation Team
 */
public class DashboardDtos {

    /**
     * Aggregated dashboard summary containing balance totals and breakdowns.
     *
     * @param currentBalance         the cumulative balance across all entries
     * @param totalHours             the total hours worked across all entries
     * @param totalActualEarnings    the total actual earnings computed from hours and rates
     * @param totalPaystubAmount     the total paystub amounts received
     * @param totalEmployerPayments  the total direct employer payments
     * @param totalInsuranceDeductions the total insurance deductions
     * @param balanceByYear          balance breakdown grouped by year
     * @param balanceByProject       balance breakdown grouped by project
     */
    public record DashboardSummary(
            BigDecimal currentBalance,
            BigDecimal totalHours,
            BigDecimal totalActualEarnings,
            BigDecimal totalPaystubAmount,
            BigDecimal totalEmployerPayments,
            BigDecimal totalInsuranceDeductions,
            List<NamedAmount> balanceByYear,
            List<NamedAmount> balanceByProject
    ) {}

    /**
     * A simple name-amount pair used for grouped balance data.
     *
     * @param name   the group name (e.g., year or project name)
     * @param amount the aggregated amount for the group
     */
    public record NamedAmount(String name, BigDecimal amount) {}
}

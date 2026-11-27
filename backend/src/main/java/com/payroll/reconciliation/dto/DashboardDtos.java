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
     * Aggregated dashboard summary containing balance totals, vendor fee,
     * employer margin analytics, and breakdowns by year and project.
     */
    public record DashboardSummary(
            BigDecimal currentBalance,
            BigDecimal totalHours,
            BigDecimal totalActualEarnings,
            BigDecimal totalPaystubAmount,
            BigDecimal totalEmployerPayments,
            BigDecimal totalInsuranceDeductions,
            BigDecimal totalVendorFee,
            BigDecimal totalEmployerMargin,
            List<NamedAmount> balanceByYear,
            List<NamedAmount> balanceByProject,
            List<NamedAmount> vendorFeeByYear,
            List<NamedAmount> employerMarginByYear,
            List<ProjectMarginSummary> marginByProject
    ) {}

    /**
     * A simple name-amount pair used for grouped data.
     */
    public record NamedAmount(String name, BigDecimal amount) {}

    /**
     * Per-project margin summary showing vendor fee and employer margin totals.
     */
    public record ProjectMarginSummary(
            Long projectId,
            String projectName,
            String clientName,
            String vendorName,
            BigDecimal totalHours,
            BigDecimal totalVendorFee,
            BigDecimal totalEmployerMargin,
            BigDecimal vendorFeePerHour,
            BigDecimal employerMarginPerHour,
            List<MonthlyMarginDetail> monthlyBreakdown
    ) {}

    /**
     * Monthly breakdown of vendor fee and employer margin for a project.
     */
    public record MonthlyMarginDetail(
            String month,
            BigDecimal hoursWorked,
            BigDecimal clientPays,
            BigDecimal vendorFee,
            BigDecimal employerGets,
            BigDecimal youGet,
            BigDecimal employerMargin
    ) {}
}

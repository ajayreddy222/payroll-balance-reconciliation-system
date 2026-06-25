package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.DashboardDtos.*;
import com.payroll.reconciliation.entity.MonthlyEntry;
import com.payroll.reconciliation.entity.Project;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating dashboard summary data including vendor fee
 * and employer margin analytics.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class DashboardService {
    private final MonthlyEntryRepository entries;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    public DashboardService(MonthlyEntryRepository entries) {
        this.entries = entries;
    }

    /**
     * Computes and returns the dashboard summary with balance totals,
     * vendor fee analytics, and employer margin breakdowns.
     */
    public DashboardSummary summary() {
        List<MonthlyEntry> all = entries.findAllWithProject();

        // Existing balance metrics
        BigDecimal currentBalance = sum(all, MonthlyEntry::getMonthlyBalance);
        BigDecimal totalHours = sum(all, MonthlyEntry::getHoursWorked);
        BigDecimal totalActual = sum(all, MonthlyEntry::getActualEarnings);
        BigDecimal totalPaystubs = sum(all, MonthlyEntry::getPaystubAmountReceived);
        BigDecimal totalEmployerPayments = sum(all, MonthlyEntry::getDirectEmployerPayment);
        BigDecimal totalInsurance = sum(all, MonthlyEntry::getInsuranceDeduction);

        // Vendor fee and employer margin totals
        BigDecimal totalVendorFee = BigDecimal.ZERO;
        BigDecimal totalEmployerMargin = BigDecimal.ZERO;

        // By year aggregation
        Map<String, BigDecimal> balanceByYearMap = new TreeMap<>();
        Map<String, BigDecimal> vendorFeeByYearMap = new TreeMap<>();
        Map<String, BigDecimal> employerMarginByYearMap = new TreeMap<>();

        // By project aggregation
        Map<String, BigDecimal> balanceByProjectMap = new TreeMap<>();
        Map<Long, List<MonthlyEntry>> entriesByProject = all.stream()
                .collect(Collectors.groupingBy(e -> e.getProject().getId()));

        for (MonthlyEntry entry : all) {
            Project project = entry.getProject();
            String year = String.valueOf(entry.getMonth().getYear());
            BigDecimal hours = value(entry.getHoursWorked());
            BigDecimal clientRate = value(project.getEmployeeHourlyRate());
            BigDecimal vendorFeePct = value(project.getVendorFeePercentage());
            BigDecimal eightyTwentyRate = value(project.getEightyTwentyRate());

            // Client pays per hour = employeeHourlyRate (this IS the billing rate)
            BigDecimal clientPays = hours.multiply(clientRate);

            // Vendor fee = hours × clientRate × (vendorFeePercentage / 100)
            BigDecimal vendorFee = money(hours.multiply(clientRate)
                    .multiply(vendorFeePct).divide(HUNDRED, 2, RoundingMode.HALF_UP));

            // Employer gets = clientPays - vendorFee
            BigDecimal employerGets = clientPays.subtract(vendorFee);

            // You get = hours × eightyTwentyRate
            BigDecimal youGet = hours.multiply(eightyTwentyRate);

            // Employer margin = employerGets - youGet
            BigDecimal employerMargin = money(employerGets.subtract(youGet));

            totalVendorFee = totalVendorFee.add(vendorFee);
            totalEmployerMargin = totalEmployerMargin.add(employerMargin);

            // Year aggregation
            balanceByYearMap.merge(year, entry.getMonthlyBalance(), BigDecimal::add);
            vendorFeeByYearMap.merge(year, vendorFee, BigDecimal::add);
            employerMarginByYearMap.merge(year, employerMargin, BigDecimal::add);

            // Project aggregation
            balanceByProjectMap.merge(project.getProjectName(), entry.getMonthlyBalance(), BigDecimal::add);
        }

        // Convert maps to lists
        List<NamedAmount> balanceByYear = toNamedAmounts(balanceByYearMap);
        List<NamedAmount> balanceByProject = toNamedAmounts(balanceByProjectMap);
        List<NamedAmount> vendorFeeByYear = toNamedAmounts(vendorFeeByYearMap);
        List<NamedAmount> employerMarginByYear = toNamedAmounts(employerMarginByYearMap);

        // Per-project margin summaries with monthly breakdown
        List<ProjectMarginSummary> marginByProject = entriesByProject.entrySet().stream()
                .map(e -> buildProjectMarginSummary(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(ProjectMarginSummary::projectName))
                .toList();

        return new DashboardSummary(
                currentBalance, totalHours, totalActual, totalPaystubs,
                totalEmployerPayments, totalInsurance,
                totalVendorFee, totalEmployerMargin,
                balanceByYear, balanceByProject,
                vendorFeeByYear, employerMarginByYear,
                marginByProject
        );
    }

    private ProjectMarginSummary buildProjectMarginSummary(Long projectId, List<MonthlyEntry> projectEntries) {
        if (projectEntries.isEmpty()) {
            return new ProjectMarginSummary(projectId, "", "", "", BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
        }

        Project project = projectEntries.get(0).getProject();
        BigDecimal clientRate = value(project.getEmployeeHourlyRate());
        BigDecimal vendorFeePct = value(project.getVendorFeePercentage());
        BigDecimal eightyTwentyRate = value(project.getEightyTwentyRate());

        BigDecimal projectTotalHours = BigDecimal.ZERO;
        BigDecimal projectTotalVendorFee = BigDecimal.ZERO;
        BigDecimal projectTotalEmployerMargin = BigDecimal.ZERO;
        List<MonthlyMarginDetail> monthlyBreakdown = new ArrayList<>();

        List<MonthlyEntry> sorted = projectEntries.stream()
                .sorted(Comparator.comparing(MonthlyEntry::getMonth))
                .toList();

        for (MonthlyEntry entry : sorted) {
            BigDecimal hours = value(entry.getHoursWorked());
            BigDecimal clientPays = money(hours.multiply(clientRate));
            BigDecimal vendorFee = money(hours.multiply(clientRate)
                    .multiply(vendorFeePct).divide(HUNDRED, 2, RoundingMode.HALF_UP));
            BigDecimal employerGets = money(clientPays.subtract(vendorFee));
            BigDecimal youGet = money(hours.multiply(eightyTwentyRate));
            BigDecimal employerMargin = money(employerGets.subtract(youGet));

            projectTotalHours = projectTotalHours.add(hours);
            projectTotalVendorFee = projectTotalVendorFee.add(vendorFee);
            projectTotalEmployerMargin = projectTotalEmployerMargin.add(employerMargin);

            monthlyBreakdown.add(new MonthlyMarginDetail(
                    entry.getMonth().format(MONTH_FMT),
                    hours, clientPays, vendorFee, employerGets, youGet, employerMargin
            ));
        }

        BigDecimal vendorFeePerHour = projectTotalHours.signum() > 0
                ? money(projectTotalVendorFee.divide(projectTotalHours, 2, RoundingMode.HALF_UP))
                : BigDecimal.ZERO;
        BigDecimal employerMarginPerHour = projectTotalHours.signum() > 0
                ? money(projectTotalEmployerMargin.divide(projectTotalHours, 2, RoundingMode.HALF_UP))
                : BigDecimal.ZERO;

        return new ProjectMarginSummary(
                projectId, project.getProjectName(), project.getClientName(), project.getVendorName(),
                projectTotalHours, projectTotalVendorFee, projectTotalEmployerMargin,
                vendorFeePerHour, employerMarginPerHour, monthlyBreakdown
        );
    }

    private List<NamedAmount> toNamedAmounts(Map<String, BigDecimal> map) {
        return map.entrySet().stream()
                .map(e -> new NamedAmount(e.getKey(), e.getValue()))
                .toList();
    }

    private BigDecimal sum(List<MonthlyEntry> entries, java.util.function.Function<MonthlyEntry, BigDecimal> extractor) {
        return entries.stream().map(extractor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal value(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}

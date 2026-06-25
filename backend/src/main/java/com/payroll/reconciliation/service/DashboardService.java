package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.DashboardDtos.DashboardSummary;
import com.payroll.reconciliation.dto.DashboardDtos.NamedAmount;
import com.payroll.reconciliation.entity.MonthlyEntry;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating dashboard summary data.
 * Aggregates monthly entry data into totals and breakdowns by year and project.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class DashboardService {
    private final MonthlyEntryRepository entries;

    /**
     * Constructs the service with the required monthly entry repository.
     *
     * @param entries the monthly entry repository
     */
    public DashboardService(MonthlyEntryRepository entries) {
        this.entries = entries;
    }

    /**
     * Computes and returns the dashboard summary with balance totals
     * and breakdowns by year and project.
     *
     * @return the aggregated dashboard summary
     */
    public DashboardSummary summary() {
        List<MonthlyEntry> all = entries.findAllWithProject();
        BigDecimal currentBalance = sum(all.stream().map(MonthlyEntry::getMonthlyBalance).toList());
        BigDecimal totalHours = sum(all.stream().map(MonthlyEntry::getHoursWorked).toList());
        BigDecimal totalActual = sum(all.stream().map(MonthlyEntry::getActualEarnings).toList());
        BigDecimal totalPaystubs = sum(all.stream().map(MonthlyEntry::getPaystubAmountReceived).toList());
        BigDecimal totalEmployerPayments = sum(all.stream().map(MonthlyEntry::getDirectEmployerPayment).toList());
        BigDecimal totalInsurance = sum(all.stream().map(MonthlyEntry::getInsuranceDeduction).toList());

        List<NamedAmount> byYear = all.stream()
                .collect(Collectors.groupingBy(e -> String.valueOf(e.getMonth().getYear()), Collectors.reducing(BigDecimal.ZERO, MonthlyEntry::getMonthlyBalance, BigDecimal::add)))
                .entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> new NamedAmount(e.getKey(), e.getValue())).toList();

        List<NamedAmount> byProject = all.stream()
                .collect(Collectors.groupingBy(e -> e.getProject().getProjectName(), Collectors.reducing(BigDecimal.ZERO, MonthlyEntry::getMonthlyBalance, BigDecimal::add)))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(e -> new NamedAmount(e.getKey(), e.getValue())).toList();

        return new DashboardSummary(currentBalance, totalHours, totalActual, totalPaystubs, totalEmployerPayments, totalInsurance, byYear, byProject);
    }

    /**
     * Sums a list of BigDecimal values.
     *
     * @param values the values to sum
     * @return the total sum
     */
    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

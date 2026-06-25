package com.payroll.reconciliation.service;

import com.payroll.reconciliation.entity.MonthlyEntry;
import com.payroll.reconciliation.entity.YearlyBalance;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import com.payroll.reconciliation.repository.YearlyBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service responsible for calculating and persisting yearly balance summaries.
 *
 * <p>Implements the carry-forward rule (Rule 9): the ending balance of the previous
 * year becomes the opening balance of the next year. This ensures cumulative tracking
 * of payroll balances across years.</p>
 *
 * <p>The yearly balance is recalculated whenever a monthly entry is created, updated,
 * or deleted, ensuring the {@code yearly_balances} table is always in sync.</p>
 *
 * <p>Calculation logic:
 * <ul>
 *   <li>Earned Balance = Sum of all monthly balances for that year</li>
 *   <li>Opening Balance = Previous year's ending balance (0 for the first year)</li>
 *   <li>Ending Balance = Opening Balance + Earned Balance</li>
 * </ul>
 * </p>
 *
 * @author PayrollReconciliation
 */
@Service
public class YearlyBalanceService {

    private final MonthlyEntryRepository entries;
    private final YearlyBalanceRepository yearlyBalances;

    /**
     * Constructs a new YearlyBalanceService with required repositories.
     *
     * @param entries        repository for querying monthly entries
     * @param yearlyBalances repository for yearly balance persistence
     */
    public YearlyBalanceService(MonthlyEntryRepository entries, YearlyBalanceRepository yearlyBalances) {
        this.entries = entries;
        this.yearlyBalances = yearlyBalances;
    }

    /**
     * Recalculates all yearly balances based on current monthly entry data.
     *
     * <p>This method:
     * <ol>
     *   <li>Groups all monthly entries by year</li>
     *   <li>Sums the monthly balances for each year to get the earned balance</li>
     *   <li>Applies carry-forward: each year's opening balance is the previous year's ending balance</li>
     *   <li>Persists the calculated yearly balance records (creates or updates)</li>
     * </ol>
     * </p>
     *
     * <p>Uses a {@link TreeMap} to ensure years are processed in ascending order,
     * which is required for the carry-forward calculation to work correctly.</p>
     *
     * @return list of all yearly balance records sorted by year ascending
     */
    @Transactional
    public List<YearlyBalance> recalculate() {
        // Group monthly balances by year, using TreeMap for natural ordering
        Map<Integer, BigDecimal> earnedByYear = entries.findAllWithProject().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getMonth().getYear(),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, MonthlyEntry::getMonthlyBalance, BigDecimal::add)
                ));

        // Calculate carry-forward balances
        BigDecimal opening = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> item : earnedByYear.entrySet()) {
            YearlyBalance balance = yearlyBalances.findByBalanceYear(item.getKey())
                    .orElseGet(YearlyBalance::new);
            balance.setBalanceYear(item.getKey());
            balance.setOpeningBalance(opening);
            balance.setEarnedBalance(item.getValue());
            balance.setEndingBalance(opening.add(item.getValue()));
            yearlyBalances.save(balance);

            // Next year's opening = this year's ending
            opening = balance.getEndingBalance();
        }

        return yearlyBalances.findAll().stream()
                .sorted((a, b) -> a.getBalanceYear().compareTo(b.getBalanceYear()))
                .toList();
    }
}

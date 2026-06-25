package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.entity.MonthlyEntry;
import com.payroll.reconciliation.entity.YearlyBalance;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import com.payroll.reconciliation.service.DetailRecordService;
import com.payroll.reconciliation.service.YearlyBalanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Administrative controller for one-time maintenance operations.
 *
 * <p>Provides endpoints for backfilling detail/audit tables and
 * recalculating derived data for existing records.</p>
 *
 * @author PayrollReconciliation
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MonthlyEntryRepository monthlyEntryRepository;
    private final DetailRecordService detailRecordService;
    private final YearlyBalanceService yearlyBalanceService;

    /**
     * Constructs the AdminController with required dependencies.
     *
     * @param monthlyEntryRepository repository for fetching all monthly entries
     * @param detailRecordService    service for syncing detail tables
     * @param yearlyBalanceService   service for recalculating yearly balances
     */
    public AdminController(MonthlyEntryRepository monthlyEntryRepository,
                           DetailRecordService detailRecordService,
                           YearlyBalanceService yearlyBalanceService) {
        this.monthlyEntryRepository = monthlyEntryRepository;
        this.detailRecordService = detailRecordService;
        this.yearlyBalanceService = yearlyBalanceService;
    }

    /**
     * Backfills all detail/audit tables (paystubs, employer_payments, adjustments)
     * and recalculates yearly_balances for ALL existing monthly entries.
     *
     * <p>This is a one-time operation to populate the detail tables for entries
     * that were created before the detail record sync was implemented.</p>
     *
     * <p>Safe to run multiple times — it will update existing detail records
     * rather than creating duplicates.</p>
     *
     * @return a summary map with the count of entries processed and yearly balances calculated
     */
    @PostMapping("/backfill")
    public Map<String, Object> backfillDetailRecords() {
        List<MonthlyEntry> allEntries = monthlyEntryRepository.findAllWithProject();

        int processed = 0;
        for (MonthlyEntry entry : allEntries) {
            detailRecordService.syncDetailRecords(entry);
            processed++;
        }

        // Recalculate all yearly balances
        List<YearlyBalance> yearlyBalances = yearlyBalanceService.recalculate();

        return Map.of(
                "entriesProcessed", processed,
                "yearlyBalancesCalculated", yearlyBalances.size(),
                "message", "Backfill completed successfully. All detail tables are now in sync."
        );
    }
}

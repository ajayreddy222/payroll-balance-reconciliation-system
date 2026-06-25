package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.entity.YearlyBalance;
import com.payroll.reconciliation.service.YearlyBalanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for yearly balance operations.
 * Provides an endpoint to trigger balance recalculation across all years.
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/yearly-balances")
public class YearlyBalanceController {
    private final YearlyBalanceService yearlyBalanceService;

    /**
     * Constructs the controller with the required yearly balance service.
     *
     * @param yearlyBalanceService the service handling yearly balance calculations
     */
    public YearlyBalanceController(YearlyBalanceService yearlyBalanceService) {
        this.yearlyBalanceService = yearlyBalanceService;
    }

    /**
     * Triggers a full recalculation of yearly balances.
     *
     * @return list of all recalculated yearly balance records
     */
    @PostMapping("/recalculate")
    public List<YearlyBalance> recalculate() {
        return yearlyBalanceService.recalculate();
    }
}

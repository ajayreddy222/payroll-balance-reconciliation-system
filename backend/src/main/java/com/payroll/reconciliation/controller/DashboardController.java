package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.DashboardDtos.DashboardSummary;
import com.payroll.reconciliation.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the dashboard summary endpoint.
 * Provides aggregated financial data for the UI dashboard.
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    /**
     * Constructs the controller with the required dashboard service.
     *
     * @param dashboardService the service handling dashboard aggregation logic
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retrieves the dashboard summary with balance totals and breakdowns.
     *
     * @return the aggregated dashboard summary
     */
    @GetMapping
    public DashboardSummary summary() {
        return dashboardService.summary();
    }
}

package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.MonthlyEntryDtos.MonthlyEntryRequest;
import com.payroll.reconciliation.dto.MonthlyEntryDtos.MonthlyEntryResponse;
import com.payroll.reconciliation.service.MonthlyEntryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing monthly payroll entries.
 * Provides CRUD endpoints for monthly entry records.
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/monthly-entries")
public class MonthlyEntryController {
    private final MonthlyEntryService monthlyEntryService;

    /**
     * Constructs the controller with the required monthly entry service.
     *
     * @param monthlyEntryService the service handling monthly entry business logic
     */
    public MonthlyEntryController(MonthlyEntryService monthlyEntryService) {
        this.monthlyEntryService = monthlyEntryService;
    }

    /**
     * Retrieves all monthly entries.
     *
     * @return list of all monthly entry responses
     */
    @GetMapping
    public List<MonthlyEntryResponse> list() {
        return monthlyEntryService.list();
    }

    /**
     * Retrieves a single monthly entry by its ID.
     *
     * @param id the monthly entry ID
     * @return the monthly entry response
     */
    @GetMapping("/{id}")
    public MonthlyEntryResponse get(@PathVariable Long id) {
        return monthlyEntryService.get(id);
    }

    /**
     * Creates or updates a monthly entry (upsert by project and month).
     *
     * @param request the monthly entry request payload
     * @return the created or updated monthly entry response
     */
    @PostMapping
    public MonthlyEntryResponse upsert(@Valid @RequestBody MonthlyEntryRequest request) {
        return monthlyEntryService.upsert(request);
    }

    /**
     * Updates an existing monthly entry by ID.
     *
     * @param id      the monthly entry ID to update
     * @param request the updated monthly entry data
     * @return the updated monthly entry response
     */
    @PutMapping("/{id}")
    public MonthlyEntryResponse update(@PathVariable Long id, @Valid @RequestBody MonthlyEntryRequest request) {
        return monthlyEntryService.update(id, request);
    }

    /**
     * Deletes a monthly entry by ID.
     *
     * @param id the monthly entry ID to delete
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        monthlyEntryService.delete(id);
    }
}

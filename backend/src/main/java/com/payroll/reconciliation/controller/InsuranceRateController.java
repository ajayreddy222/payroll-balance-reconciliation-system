package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.InsuranceRateDtos.InsuranceRateRequest;
import com.payroll.reconciliation.dto.InsuranceRateDtos.InsuranceRateResponse;
import com.payroll.reconciliation.service.InsuranceRateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing insurance rates.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/insurance-rates               — list all rates</li>
 *   <li>POST   /api/insurance-rates               — create a rate (auto-closes previous open rate)</li>
 *   <li>PUT    /api/insurance-rates/{id}          — update a rate</li>
 *   <li>DELETE /api/insurance-rates/{id}          — delete a rate</li>
 *   <li>POST   /api/insurance-rates/recalculate   — re-apply insurance to all monthly entries</li>
 * </ul>
 * </p>
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/insurance-rates")
public class InsuranceRateController {

    private final InsuranceRateService service;

    /**
     * Constructs the controller with the required service.
     *
     * @param service the insurance rate service
     */
    public InsuranceRateController(InsuranceRateService service) {
        this.service = service;
    }

    /**
     * Returns all insurance rates ordered by effective_from ascending.
     *
     * @return list of all rate responses
     */
    @GetMapping
    public List<InsuranceRateResponse> list() {
        return service.list();
    }

    /**
     * Creates a new insurance rate. Auto-closes the previous open rate.
     *
     * @param request the rate request
     * @return the created rate response
     */
    @PostMapping
    public InsuranceRateResponse create(@Valid @RequestBody InsuranceRateRequest request) {
        return service.create(request);
    }

    /**
     * Updates an existing insurance rate by ID.
     *
     * @param id      the rate ID to update
     * @param request the updated rate data
     * @return the updated rate response
     */
    @PutMapping("/{id}")
    public InsuranceRateResponse update(@PathVariable Long id,
                                        @Valid @RequestBody InsuranceRateRequest request) {
        return service.update(id, request);
    }

    /**
     * Deletes an insurance rate by ID.
     *
     * @param id the rate ID to delete
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /**
     * Re-applies insurance rates to all existing monthly entries and
     * recalculates yearly balances.
     *
     * @return map with count of entries updated
     */
    @PostMapping("/recalculate")
    public Map<String, Object> recalculate() {
        int count = service.recalculateAll();
        return Map.of(
                "entriesUpdated", count,
                "message", "Insurance recalculated for all monthly entries."
        );
    }
}

package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentBatchRequest;
import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentRequest;
import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentResponse;
import com.payroll.reconciliation.service.DirectPaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing the payment ledger.
 *
 * <p>Handles direct payments (cash/check), fee deductions (perm/legal fees),
 * and insurance deductions. All entries are deducted from the total running balance.</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/direct-payments            — list all entries</li>
 *   <li>POST   /api/direct-payments            — create a single entry</li>
 *   <li>POST   /api/direct-payments/batch      — create multiple entries at once</li>
 *   <li>PUT    /api/direct-payments/{id}       — update an entry</li>
 *   <li>DELETE /api/direct-payments/{id}       — delete an entry</li>
 * </ul>
 * </p>
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/direct-payments")
public class DirectPaymentController {

    private final DirectPaymentService service;

    /**
     * Constructs the controller with the required service.
     *
     * @param service the direct payment service
     */
    public DirectPaymentController(DirectPaymentService service) {
        this.service = service;
    }

    /**
     * Returns all payment entries ordered by date ascending.
     *
     * @return list of all payment responses
     */
    @GetMapping
    public List<DirectPaymentResponse> list() {
        return service.list();
    }

    /**
     * Creates a single payment entry.
     *
     * @param request the payment request
     * @return the created payment response
     */
    @PostMapping
    public DirectPaymentResponse create(@Valid @RequestBody DirectPaymentRequest request) {
        return service.create(request);
    }

    /**
     * Creates multiple payment entries in a single request.
     *
     * @param batchRequest the batch request containing a list of payments
     * @return list of created payment responses
     */
    @PostMapping("/batch")
    public List<DirectPaymentResponse> createBatch(@Valid @RequestBody DirectPaymentBatchRequest batchRequest) {
        return service.createBatch(batchRequest);
    }

    /**
     * Updates an existing payment entry by ID.
     *
     * @param id      the payment ID to update
     * @param request the updated payment data
     * @return the updated payment response
     */
    @PutMapping("/{id}")
    public DirectPaymentResponse update(@PathVariable Long id,
                                        @Valid @RequestBody DirectPaymentRequest request) {
        return service.update(id, request);
    }

    /**
     * Deletes a payment entry by ID.
     *
     * @param id the payment ID to delete
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

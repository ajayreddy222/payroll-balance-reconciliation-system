package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentRequest;
import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentResponse;
import com.payroll.reconciliation.service.DirectPaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing standalone direct payments.
 *
 * <p>Direct payments are cash or check amounts received from the employer
 * outside of the regular payroll system. They are deducted from the total
 * running balance using the 80-20 gross-up rule.</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/direct-payments       — list all payments</li>
 *   <li>POST   /api/direct-payments       — create a payment</li>
 *   <li>PUT    /api/direct-payments/{id}  — update a payment</li>
 *   <li>DELETE /api/direct-payments/{id}  — delete a payment</li>
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
     * Returns all direct payments ordered by date ascending.
     *
     * @return list of all direct payment responses
     */
    @GetMapping
    public List<DirectPaymentResponse> list() {
        return service.list();
    }

    /**
     * Creates a new direct payment.
     * Enter the net amount received in hand — the system calculates the gross.
     *
     * @param request the payment request
     * @return the created direct payment response
     */
    @PostMapping
    public DirectPaymentResponse create(@Valid @RequestBody DirectPaymentRequest request) {
        return service.create(request);
    }

    /**
     * Updates an existing direct payment by ID.
     *
     * @param id      the payment ID to update
     * @param request the updated payment data
     * @return the updated direct payment response
     */
    @PutMapping("/{id}")
    public DirectPaymentResponse update(@PathVariable Long id,
                                        @Valid @RequestBody DirectPaymentRequest request) {
        return service.update(id, request);
    }

    /**
     * Deletes a direct payment by ID.
     *
     * @param id the payment ID to delete
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

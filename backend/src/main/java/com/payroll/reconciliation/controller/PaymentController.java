package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.PaymentDtos.EmployerPaymentRequest;
import com.payroll.reconciliation.dto.PaymentDtos.EmployerPaymentResponse;
import com.payroll.reconciliation.dto.PaymentDtos.PaystubRequest;
import com.payroll.reconciliation.dto.PaymentDtos.PaystubResponse;
import com.payroll.reconciliation.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing paystubs and employer payments.
 * Provides endpoints to list and create payment records.
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService paymentService;

    /**
     * Constructs the controller with the required payment service.
     *
     * @param paymentService the service handling payment business logic
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Retrieves all paystub records.
     *
     * @return list of all paystub responses
     */
    @GetMapping("/paystubs")
    public List<PaystubResponse> paystubs() {
        return paymentService.paystubs();
    }

    /**
     * Creates a new paystub record.
     *
     * @param request the paystub creation request
     * @return the created paystub response
     */
    @PostMapping("/paystubs")
    public PaystubResponse createPaystub(@Valid @RequestBody PaystubRequest request) {
        return paymentService.createPaystub(request);
    }

    /**
     * Retrieves all employer payment records.
     *
     * @return list of all employer payment responses
     */
    @GetMapping("/employer-payments")
    public List<EmployerPaymentResponse> employerPayments() {
        return paymentService.employerPayments();
    }

    /**
     * Creates a new employer payment record.
     *
     * @param request the employer payment creation request
     * @return the created employer payment response
     */
    @PostMapping("/employer-payments")
    public EmployerPaymentResponse createEmployerPayment(@Valid @RequestBody EmployerPaymentRequest request) {
        return paymentService.createEmployerPayment(request);
    }
}

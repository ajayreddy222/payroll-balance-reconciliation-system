package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.PaymentDtos.EmployerPaymentRequest;
import com.payroll.reconciliation.dto.PaymentDtos.EmployerPaymentResponse;
import com.payroll.reconciliation.dto.PaymentDtos.PaystubRequest;
import com.payroll.reconciliation.dto.PaymentDtos.PaystubResponse;
import com.payroll.reconciliation.entity.EmployerPayment;
import com.payroll.reconciliation.entity.Paystub;
import com.payroll.reconciliation.repository.EmployerPaymentRepository;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import com.payroll.reconciliation.repository.PaystubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing paystubs and employer payments.
 * Handles creation and retrieval of payment records with gross amount calculations.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class PaymentService {
    private final PaystubRepository paystubs;
    private final EmployerPaymentRepository employerPayments;
    private final MonthlyEntryRepository entries;
    private final BalanceCalculationService calculator;

    /**
     * Constructs the service with required repositories and calculation service.
     *
     * @param paystubs         the paystub repository
     * @param employerPayments the employer payment repository
     * @param entries          the monthly entry repository
     * @param calculator       the balance calculation service for gross-up computations
     */
    public PaymentService(PaystubRepository paystubs, EmployerPaymentRepository employerPayments, MonthlyEntryRepository entries, BalanceCalculationService calculator) {
        this.paystubs = paystubs;
        this.employerPayments = employerPayments;
        this.entries = entries;
        this.calculator = calculator;
    }

    /**
     * Retrieves all paystub records.
     *
     * @return list of all paystubs as response DTOs
     */
    public List<PaystubResponse> paystubs() {
        return paystubs.findAll().stream().map(this::toPaystub).toList();
    }

    /**
     * Creates a new paystub record linked to a monthly entry.
     *
     * @param request the paystub creation request
     * @return the created paystub as a response DTO
     */
    @Transactional
    public PaystubResponse createPaystub(PaystubRequest request) {
        Paystub paystub = new Paystub();
        paystub.setMonthlyEntry(entries.findById(request.monthlyEntryId()).orElseThrow());
        paystub.setPayDate(request.payDate());
        paystub.setAmount(request.amount());
        paystub.setDocumentReference(request.documentReference());
        paystub.setNotes(request.notes());
        return toPaystub(paystubs.save(paystub));
    }

    /**
     * Retrieves all employer payment records.
     *
     * @return list of all employer payments as response DTOs
     */
    public List<EmployerPaymentResponse> employerPayments() {
        return employerPayments.findAll().stream().map(this::toEmployerPayment).toList();
    }

    /**
     * Creates a new employer payment record with a computed gross amount.
     *
     * @param request the employer payment creation request
     * @return the created employer payment as a response DTO
     */
    @Transactional
    public EmployerPaymentResponse createEmployerPayment(EmployerPaymentRequest request) {
        EmployerPayment payment = new EmployerPayment();
        payment.setMonthlyEntry(entries.findById(request.monthlyEntryId()).orElseThrow());
        payment.setPaymentDate(request.paymentDate());
        payment.setAmountReceived(request.amountReceived());
        payment.setGrossAmount(calculator.grossEmployerPayment(request.amountReceived()));
        payment.setNotes(request.notes());
        return toEmployerPayment(employerPayments.save(payment));
    }

    /**
     * Converts a Paystub entity to its response DTO.
     *
     * @param paystub the paystub entity
     * @return the response DTO
     */
    private PaystubResponse toPaystub(Paystub paystub) {
        return new PaystubResponse(paystub.getId(), paystub.getMonthlyEntry().getId(), paystub.getPayDate(), paystub.getAmount(), paystub.getDocumentReference(), paystub.getNotes());
    }

    /**
     * Converts an EmployerPayment entity to its response DTO.
     *
     * @param payment the employer payment entity
     * @return the response DTO
     */
    private EmployerPaymentResponse toEmployerPayment(EmployerPayment payment) {
        return new EmployerPaymentResponse(payment.getId(), payment.getMonthlyEntry().getId(), payment.getPaymentDate(), payment.getAmountReceived(), payment.getGrossAmount(), payment.getNotes());
    }
}

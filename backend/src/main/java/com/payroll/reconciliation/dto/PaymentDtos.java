package com.payroll.reconciliation.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Objects for paystub and employer payment API operations.
 *
 * @author Payroll Reconciliation Team
 */
public class PaymentDtos {

    /**
     * Request DTO for creating a paystub record.
     *
     * @param monthlyEntryId    the associated monthly entry ID
     * @param payDate           the payment date
     * @param amount            the paystub amount
     * @param documentReference optional document reference identifier
     * @param notes             optional notes
     */
    public record PaystubRequest(@NotNull Long monthlyEntryId, @NotNull LocalDate payDate, @NotNull BigDecimal amount, String documentReference, String notes) {}

    /**
     * Response DTO representing a paystub record.
     *
     * @param id                the paystub ID
     * @param monthlyEntryId    the associated monthly entry ID
     * @param payDate           the payment date
     * @param amount            the paystub amount
     * @param documentReference the document reference identifier
     * @param notes             optional notes
     */
    public record PaystubResponse(Long id, Long monthlyEntryId, LocalDate payDate, BigDecimal amount, String documentReference, String notes) {}

    /**
     * Request DTO for creating an employer payment record.
     *
     * @param monthlyEntryId the associated monthly entry ID
     * @param paymentDate    the payment date
     * @param amountReceived the net amount received
     * @param notes          optional notes
     */
    public record EmployerPaymentRequest(@NotNull Long monthlyEntryId, @NotNull LocalDate paymentDate, @NotNull BigDecimal amountReceived, String notes) {}

    /**
     * Response DTO representing an employer payment record.
     *
     * @param id             the payment ID
     * @param monthlyEntryId the associated monthly entry ID
     * @param paymentDate    the payment date
     * @param amountReceived the net amount received
     * @param grossAmount    the computed gross amount before deductions
     * @param notes          optional notes
     */
    public record EmployerPaymentResponse(Long id, Long monthlyEntryId, LocalDate paymentDate, BigDecimal amountReceived, BigDecimal grossAmount, String notes) {}
}

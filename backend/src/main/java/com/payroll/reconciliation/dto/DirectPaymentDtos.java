package com.payroll.reconciliation.dto;

import com.payroll.reconciliation.entity.PaymentMode;
import com.payroll.reconciliation.entity.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Objects for direct payment API operations.
 *
 * @author Payroll Reconciliation Team
 */
public class DirectPaymentDtos {

    /**
     * Request DTO for creating or updating a payment ledger entry.
     *
     * @param paymentDate    the date the payment/deduction occurred
     * @param paymentType    DIRECT_PAYMENT, FEE_DEDUCTION, or PERSONAL_EXPENSE
     * @param paymentMode    how payment was received (required for DIRECT_PAYMENT and PERSONAL_EXPENSE, optional for FEE_DEDUCTION)
     * @param amountReceived the net amount received or fee amount
     * @param notes          optional notes
     */
    public record DirectPaymentRequest(
            @NotNull LocalDate paymentDate,
            @NotNull PaymentType paymentType,
            PaymentMode paymentMode,
            @NotNull @Positive BigDecimal amountReceived,
            String notes
    ) {}

    /**
     * Request DTO for creating multiple payment entries at once.
     *
     * @param payments list of payment requests
     */
    public record DirectPaymentBatchRequest(
            @NotNull @Valid List<DirectPaymentRequest> payments
    ) {}

    /**
     * Response DTO for a payment ledger entry.
     *
     * @param id             the payment ID
     * @param paymentDate    the date occurred
     * @param paymentType    the type
     * @param paymentMode    how it was received (null for non-direct-payment types)
     * @param amountReceived the net amount entered
     * @param grossAmount    the gross amount (÷0.80 for DIRECT_PAYMENT, as-is for others)
     * @param affectsBalance whether this entry is deducted from the running balance
     * @param notes          optional notes
     */
    public record DirectPaymentResponse(
            Long id,
            LocalDate paymentDate,
            PaymentType paymentType,
            PaymentMode paymentMode,
            BigDecimal amountReceived,
            BigDecimal grossAmount,
            boolean affectsBalance,
            String notes
    ) {}
}

package com.payroll.reconciliation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Objects for direct payment API operations.
 *
 * @author Payroll Reconciliation Team
 */
public class DirectPaymentDtos {

    /**
     * Request DTO for creating or updating a direct payment.
     *
     * @param paymentDate    the date the payment was received
     * @param amountReceived the net amount received in hand (system grosses up via ÷ 0.80)
     * @param notes          optional notes (e.g., "10L@80")
     */
    public record DirectPaymentRequest(
            @NotNull LocalDate paymentDate,
            @NotNull @Positive BigDecimal amountReceived,
            String notes
    ) {}

    /**
     * Response DTO for a direct payment record.
     *
     * @param id             the payment ID
     * @param paymentDate    the date the payment was received
     * @param amountReceived the net amount received in hand
     * @param grossAmount    the gross amount deducted from balance (amountReceived ÷ 0.80)
     * @param notes          optional notes
     */
    public record DirectPaymentResponse(
            Long id,
            LocalDate paymentDate,
            BigDecimal amountReceived,
            BigDecimal grossAmount,
            String notes
    ) {}
}

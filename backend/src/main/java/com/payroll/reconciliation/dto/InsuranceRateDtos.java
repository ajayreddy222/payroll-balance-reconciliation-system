package com.payroll.reconciliation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Objects for insurance rate API operations.
 *
 * @author Payroll Reconciliation Team
 */
public class InsuranceRateDtos {

    /**
     * Request DTO for creating or updating an insurance rate.
     *
     * @param effectiveFrom  the first month this rate is active (required)
     * @param effectiveTo    the last month this rate is active (null = still active)
     * @param monthlyAmount  the monthly insurance deduction amount
     * @param notes          optional notes (e.g. "Health only", "Health + Dental")
     */
    public record InsuranceRateRequest(
            @NotNull LocalDate effectiveFrom,
            LocalDate effectiveTo,
            @NotNull @Positive BigDecimal monthlyAmount,
            String notes
    ) {}

    /**
     * Response DTO for an insurance rate record.
     *
     * @param id             the rate ID
     * @param effectiveFrom  the first month this rate is active
     * @param effectiveTo    the last month this rate is active (null = still active)
     * @param monthlyAmount  the monthly insurance deduction amount
     * @param notes          optional notes
     */
    public record InsuranceRateResponse(
            Long id,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            BigDecimal monthlyAmount,
            String notes
    ) {}
}

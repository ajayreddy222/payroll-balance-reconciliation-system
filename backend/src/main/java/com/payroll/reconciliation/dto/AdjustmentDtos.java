package com.payroll.reconciliation.dto;

import com.payroll.reconciliation.entity.AdjustmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Objects for adjustment-related API operations.
 *
 * @author Payroll Reconciliation Team
 */
public class AdjustmentDtos {

    /**
     * Request DTO for creating a new adjustment.
     *
     * @param monthlyEntryId the optional associated monthly entry ID
     * @param projectId      the optional associated project ID
     * @param adjustmentDate the date of the adjustment
     * @param amount         the adjustment amount
     * @param reason         the reason for the adjustment
     * @param adjustmentType the type/category of adjustment
     */
    public record AdjustmentRequest(
            Long monthlyEntryId,
            Long projectId,
            @NotNull LocalDate adjustmentDate,
            @NotNull BigDecimal amount,
            @NotBlank String reason,
            @NotNull AdjustmentType adjustmentType
    ) {}

    /**
     * Response DTO representing an adjustment record.
     *
     * @param id     the adjustment ID
     * @param date   the adjustment date
     * @param amount the adjustment amount
     * @param reason the reason for the adjustment
     * @param type   the adjustment type
     * @param user   the full name of the user who created the adjustment
     */
    public record AdjustmentResponse(Long id, LocalDate date, BigDecimal amount, String reason, AdjustmentType type, String user) {}
}

package com.payroll.reconciliation.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Objects for monthly entry API operations.
 *
 * @author Payroll Reconciliation Team
 */
public class MonthlyEntryDtos {

    /**
     * Request DTO for creating or updating a monthly entry.
     * Insurance is auto-populated from the insurance_rates table — not required here.
     *
     * @param projectId             the associated project ID
     * @param month                 the entry month (first day of month)
     * @param hoursWorked           the number of hours worked
     * @param employeeHourlyRate    the employee's hourly rate
     * @param paystubAmountReceived the LCA/paystub amount received
     * @param notes                 optional notes
     */
    public record MonthlyEntryRequest(
            @NotNull Long projectId,
            @NotNull LocalDate month,
            @NotNull BigDecimal hoursWorked,
            @NotNull BigDecimal employeeHourlyRate,
            @NotNull BigDecimal paystubAmountReceived,
            String notes
    ) {}

    /**
     * Response DTO representing a monthly entry with computed fields.
     *
     * @param id                    the entry ID
     * @param projectId             the associated project ID
     * @param projectName           the project name
     * @param clientName            the client name
     * @param vendorName            the vendor name
     * @param month                 the entry month
     * @param hoursWorked           the hours worked
     * @param employeeHourlyRate    the employee hourly rate
     * @param actualEarnings        computed actual earnings (hours × rate)
     * @param paystubAmountReceived the paystub amount received
     * @param insuranceDeduction    auto-applied insurance deduction
     * @param monthlyBalance        the computed monthly balance
     * @param notes                 optional notes
     */
    public record MonthlyEntryResponse(
            Long id,
            Long projectId,
            String projectName,
            String clientName,
            String vendorName,
            LocalDate month,
            BigDecimal hoursWorked,
            BigDecimal employeeHourlyRate,
            BigDecimal actualEarnings,
            BigDecimal paystubAmountReceived,
            BigDecimal insuranceDeduction,
            BigDecimal monthlyBalance,
            String notes
    ) {}
}

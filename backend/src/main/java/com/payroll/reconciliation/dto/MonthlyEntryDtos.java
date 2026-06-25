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
     *
     * @param projectId              the associated project ID
     * @param month                  the entry month (first day of month)
     * @param hoursWorked            the number of hours worked
     * @param employeeHourlyRate     the employee's hourly rate
     * @param paystubAmountReceived  the amount received via paystub
     * @param directEmployerPayment  optional direct employer payment amount
     * @param insuranceDeduction     optional insurance deduction amount
     * @param otherAdjustment        optional other adjustment amount
     * @param notes                  optional notes for the entry
     */
    public record MonthlyEntryRequest(
            @NotNull Long projectId,
            @NotNull LocalDate month,
            @NotNull BigDecimal hoursWorked,
            @NotNull BigDecimal employeeHourlyRate,
            @NotNull BigDecimal paystubAmountReceived,
            BigDecimal directEmployerPayment,
            BigDecimal insuranceDeduction,
            BigDecimal otherAdjustment,
            String notes
    ) {}

    /**
     * Response DTO representing a monthly entry with computed fields.
     *
     * @param id                        the entry ID
     * @param projectId                 the associated project ID
     * @param projectName               the project name
     * @param clientName                the client name
     * @param vendorName                the vendor name
     * @param month                     the entry month
     * @param hoursWorked               the hours worked
     * @param employeeHourlyRate        the employee hourly rate
     * @param actualEarnings            the computed actual earnings (hours * rate)
     * @param paystubAmountReceived     the paystub amount received
     * @param directEmployerPayment     the direct employer payment amount
     * @param directEmployerGrossAmount the gross amount of direct employer payment
     * @param insuranceDeduction        the insurance deduction amount
     * @param otherAdjustment           other adjustment amount
     * @param monthlyBalance            the computed monthly balance
     * @param notes                     optional notes
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
            BigDecimal directEmployerPayment,
            BigDecimal directEmployerGrossAmount,
            BigDecimal insuranceDeduction,
            BigDecimal otherAdjustment,
            BigDecimal monthlyBalance,
            String notes
    ) {}
}

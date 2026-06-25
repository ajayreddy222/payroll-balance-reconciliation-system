package com.payroll.reconciliation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Objects for project-related API operations.
 *
 * @author Payroll Reconciliation Team
 */
public class ProjectDtos {

    /**
     * Request DTO for creating or updating a project.
     *
     * @param clientName          the client name
     * @param vendorName          the vendor/staffing agency name
     * @param projectName         the project name
     * @param employeeHourlyRate  the employee's hourly bill rate
     * @param vendorFeePercentage optional vendor fee percentage
     * @param eightyTwentyRate    optional 80/20 split rate
     * @param lcaAmount           optional LCA (Labor Condition Application) amount
     * @param startDate           the project start date
     * @param endDate             optional project end date
     * @param active              whether the project is active (defaults to true)
     */
    public record ProjectRequest(
            @NotBlank String clientName,
            @NotBlank String vendorName,
            @NotBlank String projectName,
            @NotNull BigDecimal employeeHourlyRate,
            BigDecimal vendorFeePercentage,
            BigDecimal eightyTwentyRate,
            BigDecimal lcaAmount,
            @NotNull LocalDate startDate,
            LocalDate endDate,
            Boolean active
    ) {}

    /**
     * Response DTO representing a project record.
     *
     * @param id                  the project ID
     * @param clientName          the client name
     * @param vendorName          the vendor/staffing agency name
     * @param projectName         the project name
     * @param employeeHourlyRate  the employee's hourly bill rate
     * @param vendorFeePercentage the vendor fee percentage
     * @param eightyTwentyRate    the 80/20 split rate
     * @param lcaAmount           the LCA amount
     * @param startDate           the project start date
     * @param endDate             the project end date (null if ongoing)
     * @param active              whether the project is currently active
     */
    public record ProjectResponse(
            Long id,
            String clientName,
            String vendorName,
            String projectName,
            BigDecimal employeeHourlyRate,
            BigDecimal vendorFeePercentage,
            BigDecimal eightyTwentyRate,
            BigDecimal lcaAmount,
            LocalDate startDate,
            LocalDate endDate,
            boolean active
    ) {}
}

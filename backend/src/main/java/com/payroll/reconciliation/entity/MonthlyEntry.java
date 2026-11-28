package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * JPA entity representing a monthly payroll reconciliation entry.
 * Contains hours worked, earnings, and the computed monthly balance.
 * Unique per project and month combination.
 *
 * <p>Insurance is auto-populated from the {@code insurance_rates} table on save.</p>
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "monthly_entries", uniqueConstraints = @UniqueConstraint(
        name = "uq_monthly_entry_project_month", columnNames = {"project_id", "entry_month"}))
public class MonthlyEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The project this entry belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    /** The month this entry represents (stored as first day of the month). */
    @Column(name = "entry_month", nullable = false)
    private LocalDate month;

    /** Total hours worked during this month. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hoursWorked = BigDecimal.ZERO;

    /** The employee's hourly rate for this month. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal employeeHourlyRate = BigDecimal.ZERO;

    /** Computed actual earnings (hours × rate). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal actualEarnings = BigDecimal.ZERO;

    /** The LCA/paystub amount received from payroll. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal paystubAmountReceived = BigDecimal.ZERO;

    /** Insurance deduction auto-populated from the insurance_rates table. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal insuranceDeduction = BigDecimal.ZERO;

    /** The computed monthly balance (actual − paystub − insurance). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyBalance = BigDecimal.ZERO;

    /** Optional notes for this entry. */
    @Column(columnDefinition = "text")
    private String notes;

    /** The user who created this entry. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    /** Timestamp of when this record was created (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Timestamp of when this record was last updated (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}

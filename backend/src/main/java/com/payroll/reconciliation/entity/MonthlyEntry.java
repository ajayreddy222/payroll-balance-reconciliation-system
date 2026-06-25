package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a monthly payroll reconciliation entry.
 * Contains hours worked, earnings, payments, deductions, and the computed monthly balance.
 * Unique per project and month combination.
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "monthly_entries", uniqueConstraints = @UniqueConstraint(name = "uq_monthly_entry_project_month", columnNames = {"project_id", "entry_month"}))
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

    /** Computed actual earnings (hours * rate). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal actualEarnings = BigDecimal.ZERO;

    /** The total amount received via paystubs. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal paystubAmountReceived = BigDecimal.ZERO;

    /** The total direct employer payment amount. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal directEmployerPayment = BigDecimal.ZERO;

    /** The total insurance deduction for this month. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal insuranceDeduction = BigDecimal.ZERO;

    /** Any other adjustment amount for this month. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal otherAdjustment = BigDecimal.ZERO;

    /** The computed monthly balance (actual earnings - paystub - deductions + payments). */
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

    /** Adjustments associated with this monthly entry. */
    @OneToMany(mappedBy = "monthlyEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Adjustment> adjustments = new ArrayList<>();

    /** Paystub records associated with this monthly entry. */
    @OneToMany(mappedBy = "monthlyEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paystub> paystubs = new ArrayList<>();

    /** Employer payment records associated with this monthly entry. */
    @OneToMany(mappedBy = "monthlyEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployerPayment> employerPayments = new ArrayList<>();
}

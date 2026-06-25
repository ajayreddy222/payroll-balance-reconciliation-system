package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * JPA entity representing a payroll adjustment record.
 * Adjustments can be linked to a specific monthly entry and/or project.
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "adjustments")
public class Adjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The monthly entry this adjustment is associated with (optional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_entry_id")
    private MonthlyEntry monthlyEntry;

    /** The project this adjustment is associated with (optional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /** The date the adjustment applies to. */
    @Column(nullable = false)
    private LocalDate adjustmentDate;

    /** The adjustment amount (positive or negative). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    /** The reason or description for this adjustment. */
    @Column(nullable = false, length = 500)
    private String reason;

    /** The category/type of this adjustment. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdjustmentType adjustmentType;

    /** The user who created this adjustment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    /** Timestamp of when this record was created (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}

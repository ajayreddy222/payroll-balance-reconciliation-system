package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * JPA entity representing an insurance rate for a given period.
 *
 * <p>Insurance rates typically run Sep–Aug each year. When a monthly entry
 * is saved, the system looks up the active rate for that month and
 * auto-populates the {@code insuranceDeduction} field.</p>
 *
 * <p>Lookup rule: find the rate where
 * {@code effectiveFrom <= entryMonth AND (effectiveTo >= entryMonth OR effectiveTo IS NULL)}.</p>
 *
 * <p>When a new rate is saved, the system auto-closes any currently open rate
 * by setting its {@code effectiveTo} to the month before the new rate starts.</p>
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "insurance_rates")
public class InsuranceRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The first month this rate is effective (first day of the month). */
    @Column(nullable = false)
    private LocalDate effectiveFrom;

    /**
     * The last month this rate is effective (last day of the month).
     * Null means the rate is still active.
     */
    @Column
    private LocalDate effectiveTo;

    /** The monthly insurance deduction amount. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyAmount;

    /** Optional notes (e.g. "Health only", "Health + Dental"). */
    @Column(columnDefinition = "text")
    private String notes;

    /** Timestamp of when this record was created (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}

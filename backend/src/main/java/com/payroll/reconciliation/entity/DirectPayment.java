package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * JPA entity representing a direct cash/check payment received from the employer.
 *
 * <p>These payments are independent of any specific project or monthly entry.
 * They represent money the employer hands directly to the employee (outside payroll)
 * and are deducted from the total running balance using the 80-20 gross-up rule:
 * {@code grossAmount = amountReceived / 0.80}.</p>
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "direct_payments")
public class DirectPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The date the payment was received. */
    @Column(nullable = false)
    private LocalDate paymentDate;

    /** The net amount received in hand by the employee. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amountReceived;

    /**
     * The gross amount deducted from the running balance.
     * Calculated as: {@code amountReceived / 0.80}.
     */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossAmount;

    /** Optional notes (e.g., "10L@80", "13L@81"). */
    @Column(columnDefinition = "text")
    private String notes;

    /** Timestamp of when this record was created (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}

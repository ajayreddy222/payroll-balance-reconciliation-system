package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * JPA entity representing a payment ledger entry.
 *
 * <p>Three types:
 * <ul>
 *   <li>{@link PaymentType#DIRECT_PAYMENT} — cash/check from employer, 80-20 gross-up applies,
 *       payment mode required.</li>
 *   <li>{@link PaymentType#FEE_DEDUCTION} — perm/legal fee, deducted as-is, affects balance.</li>
 *   <li>{@link PaymentType#PERSONAL_EXPENSE} — personal expense, tracked only, no balance impact.</li>
 * </ul>
 * </p>
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

    /** The date the payment/deduction occurred. */
    @Column(nullable = false)
    private LocalDate paymentDate;

    /** The type of this entry — determines gross-up and balance impact. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType = PaymentType.DIRECT_PAYMENT;

    /**
     * How the payment was received.
     * Required for DIRECT_PAYMENT, optional for FEE_DEDUCTION and PERSONAL_EXPENSE.
     */
    @Enumerated(EnumType.STRING)
    @Column
    private PaymentMode paymentMode;

    /** The net amount received or the fee/expense amount. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amountReceived;

    /**
     * The gross amount deducted from the running balance.
     * DIRECT_PAYMENT: amountReceived / 0.80.
     * FEE_DEDUCTION: same as amountReceived.
     * PERSONAL_EXPENSE: same as amountReceived (but excluded from balance calc).
     */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossAmount;

    /** Optional notes (e.g. "10L@80", "Perm fee", "Green Card filing"). */
    @Column(columnDefinition = "text")
    private String notes;

    /** Timestamp of when this record was created (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}

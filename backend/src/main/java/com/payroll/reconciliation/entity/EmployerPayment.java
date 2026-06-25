package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a direct payment received from an employer.
 * Each payment is linked to a specific monthly entry.
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "employer_payments")
public class EmployerPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The monthly entry this payment is associated with. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monthly_entry_id")
    private MonthlyEntry monthlyEntry;

    /** The date the payment was received. */
    @Column(nullable = false)
    private LocalDate paymentDate;

    /** The net amount received from the employer. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amountReceived;

    /** The computed gross amount before deductions/fees. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossAmount;

    /** Optional notes about this payment. */
    @Column(columnDefinition = "text")
    private String notes;
}

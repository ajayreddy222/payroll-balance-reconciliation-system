package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a paystub record.
 * Each paystub is linked to a specific monthly entry and records the payment received.
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "paystubs")
public class Paystub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The monthly entry this paystub belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monthly_entry_id")
    private MonthlyEntry monthlyEntry;

    /** The date payment was received. */
    @Column(nullable = false)
    private LocalDate payDate;

    /** The paystub payment amount. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    /** Optional reference to the paystub document. */
    private String documentReference;

    /** Optional notes about this paystub. */
    @Column(columnDefinition = "text")
    private String notes;
}

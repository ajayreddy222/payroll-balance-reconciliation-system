package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * JPA entity representing the aggregated yearly balance.
 * Tracks opening balance, earned balance, and ending balance for each year.
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "yearly_balances")
public class YearlyBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The calendar year this balance represents. */
    @Column(nullable = false, unique = true)
    private Integer balanceYear;

    /** The opening balance carried forward from the previous year. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    /** The balance earned during this year. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal earnedBalance = BigDecimal.ZERO;

    /** The ending balance (opening + earned). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal endingBalance = BigDecimal.ZERO;

    /** Timestamp of when this balance was last calculated (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime calculatedAt;
}

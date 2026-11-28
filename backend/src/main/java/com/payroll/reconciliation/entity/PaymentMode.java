package com.payroll.reconciliation.entity;

/**
 * Enum representing how a direct payment was received.
 * Only applies to {@link PaymentType#DIRECT_PAYMENT} entries.
 *
 * @author Payroll Reconciliation Team
 */
public enum PaymentMode {
    /** Received via Zelle transfer. */
    ZELLE,
    /** Received as physical cash. */
    CASH,
    /** Received as a check. */
    CHECK,
    /** Received via online bank transfer or wire. */
    ONLINE,
    /** Received through regular payroll paystub. */
    PAYSTUB
}

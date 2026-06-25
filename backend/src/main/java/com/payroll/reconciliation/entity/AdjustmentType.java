package com.payroll.reconciliation.entity;

/**
 * Enumeration of supported adjustment types for payroll reconciliation.
 *
 * @author Payroll Reconciliation Team
 */
public enum AdjustmentType {
    /** A negative deduction from the balance. */
    NEGATIVE_DEDUCTION,
    /** A positive adjustment to the balance. */
    POSITIVE_ADJUSTMENT,
    /** An insurance-related adjustment. */
    INSURANCE,
    /** A direct payment from the employer. */
    EMPLOYER_DIRECT_PAYMENT,
    /** A manually entered adjustment. */
    MANUAL
}

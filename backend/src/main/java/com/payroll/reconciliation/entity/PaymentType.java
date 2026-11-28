package com.payroll.reconciliation.entity;

/**
 * Enum representing the type of a payment ledger entry.
 *
 * <ul>
 *   <li>{@link #DIRECT_PAYMENT} — Cash or check received directly from the employer.
 *       The 80-20 gross-up rule applies: gross = amountReceived ÷ 0.80.
 *       Payment mode is required.</li>
 *   <li>{@link #FEE_DEDUCTION} — Perm fee, legal fee, or other employer deduction.
 *       Deducted as-is — no gross-up. Affects balance.</li>
 *   <li>{@link #PERSONAL_EXPENSE} — Perm/Green Card fees paid by the employee personally.
 *       Tracked for record-keeping only — does NOT affect balance.</li>
 * </ul>
 *
 * @author Payroll Reconciliation Team
 */
public enum PaymentType {
    /** Cash or check from employer — 80-20 gross-up applies, affects balance. */
    DIRECT_PAYMENT,
    /** Employer fee deduction — no gross-up, affects balance. */
    FEE_DEDUCTION,
    /** Personal out-of-pocket expense — tracked only, does NOT affect balance. */
    PERSONAL_EXPENSE
}

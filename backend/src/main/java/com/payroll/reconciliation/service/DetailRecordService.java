package com.payroll.reconciliation.service;

import com.payroll.reconciliation.entity.*;
import com.payroll.reconciliation.repository.AdjustmentRepository;
import com.payroll.reconciliation.repository.EmployerPaymentRepository;
import com.payroll.reconciliation.repository.PaystubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service responsible for synchronizing detail/audit records in the
 * {@code paystubs}, {@code employer_payments}, and {@code adjustments} tables
 * whenever a monthly entry is created or updated.
 *
 * <p>These detail tables mirror the values stored on {@link MonthlyEntry} and
 * serve as an audit trail. The {@code monthly_entries} table remains the
 * single source of truth for balance calculations.</p>
 *
 * <p>Business rules applied:
 * <ul>
 *   <li>If {@code paystubAmountReceived > 0}, a paystub record is created/updated.</li>
 *   <li>If {@code directEmployerPayment > 0}, an employer payment record is created/updated
 *       with both the net amount received and the gross amount (net / 0.80).</li>
 *   <li>If {@code insuranceDeduction > 0}, an adjustment record of type INSURANCE is created/updated.</li>
 *   <li>If {@code otherAdjustment != 0}, an adjustment record of type NEGATIVE_DEDUCTION
 *       or POSITIVE_ADJUSTMENT is created/updated depending on the sign.</li>
 *   <li>If a value becomes 0 on update, the corresponding detail record is removed.</li>
 * </ul>
 * </p>
 *
 * @author PayrollReconciliation
 */
@Service
public class DetailRecordService {

    private static final BigDecimal DIRECT_PAYMENT_NET_RATE = new BigDecimal("0.80");

    private final PaystubRepository paystubRepository;
    private final EmployerPaymentRepository employerPaymentRepository;
    private final AdjustmentRepository adjustmentRepository;

    /**
     * Constructs a new DetailRecordService with required repositories.
     *
     * @param paystubRepository        repository for paystub persistence
     * @param employerPaymentRepository repository for employer payment persistence
     * @param adjustmentRepository     repository for adjustment persistence
     */
    public DetailRecordService(PaystubRepository paystubRepository,
                               EmployerPaymentRepository employerPaymentRepository,
                               AdjustmentRepository adjustmentRepository) {
        this.paystubRepository = paystubRepository;
        this.employerPaymentRepository = employerPaymentRepository;
        this.adjustmentRepository = adjustmentRepository;
    }

    /**
     * Synchronizes all detail/audit tables based on the current state of a monthly entry.
     *
     * <p>This method should be called after a monthly entry is saved (both create and update).
     * It will create, update, or delete records in the detail tables as needed.</p>
     *
     * @param entry the saved monthly entry whose detail records should be synchronized
     */
    @Transactional
    public void syncDetailRecords(MonthlyEntry entry) {
        syncPaystub(entry);
        syncEmployerPayment(entry);
        syncInsuranceAdjustment(entry);
        syncOtherAdjustment(entry);
    }

    /**
     * Removes all detail records associated with a monthly entry.
     *
     * <p>Called before deleting a monthly entry to ensure referential integrity
     * and clean audit trail.</p>
     *
     * @param entryId the ID of the monthly entry being deleted
     */
    @Transactional
    public void removeDetailRecords(Long entryId) {
        paystubRepository.findByMonthlyEntryId(entryId).ifPresent(paystubRepository::delete);
        employerPaymentRepository.findByMonthlyEntryId(entryId).ifPresent(employerPaymentRepository::delete);
        adjustmentRepository.findByMonthlyEntryIdOrderByAdjustmentDateDesc(entryId)
                .forEach(adjustmentRepository::delete);
    }

    /**
     * Syncs the paystub record for a monthly entry.
     *
     * <p>If {@code paystubAmountReceived > 0}, creates or updates the paystub record.
     * If the amount is 0, removes any existing paystub record.</p>
     *
     * @param entry the monthly entry to sync
     */
    private void syncPaystub(MonthlyEntry entry) {
        BigDecimal amount = entry.getPaystubAmountReceived();

        if (amount != null && amount.signum() > 0) {
            Paystub paystub = paystubRepository.findByMonthlyEntryId(entry.getId())
                    .orElseGet(() -> {
                        Paystub p = new Paystub();
                        p.setMonthlyEntry(entry);
                        return p;
                    });
            paystub.setPayDate(entry.getMonth());
            paystub.setAmount(amount);
            paystub.setNotes(entry.getNotes());
            paystubRepository.save(paystub);
        } else {
            paystubRepository.findByMonthlyEntryId(entry.getId())
                    .ifPresent(paystubRepository::delete);
        }
    }

    /**
     * Syncs the employer payment record for a monthly entry.
     *
     * <p>If {@code directEmployerPayment > 0}, creates or updates the employer payment
     * record with both the net amount and the grossed-up amount (net / 0.80).
     * If the amount is 0, removes any existing employer payment record.</p>
     *
     * @param entry the monthly entry to sync
     */
    private void syncEmployerPayment(MonthlyEntry entry) {
        BigDecimal netAmount = entry.getDirectEmployerPayment();

        if (netAmount != null && netAmount.signum() > 0) {
            BigDecimal grossAmount = netAmount.divide(DIRECT_PAYMENT_NET_RATE, 2, RoundingMode.HALF_UP);

            EmployerPayment payment = employerPaymentRepository.findByMonthlyEntryId(entry.getId())
                    .orElseGet(() -> {
                        EmployerPayment ep = new EmployerPayment();
                        ep.setMonthlyEntry(entry);
                        return ep;
                    });
            payment.setPaymentDate(entry.getMonth());
            payment.setAmountReceived(netAmount);
            payment.setGrossAmount(grossAmount);
            payment.setNotes(entry.getNotes());
            employerPaymentRepository.save(payment);
        } else {
            employerPaymentRepository.findByMonthlyEntryId(entry.getId())
                    .ifPresent(employerPaymentRepository::delete);
        }
    }

    /**
     * Syncs the insurance deduction adjustment record for a monthly entry.
     *
     * <p>If {@code insuranceDeduction > 0}, creates or updates an adjustment record
     * of type {@link AdjustmentType#INSURANCE}. If the amount is 0, removes any
     * existing insurance adjustment.</p>
     *
     * @param entry the monthly entry to sync
     */
    private void syncInsuranceAdjustment(MonthlyEntry entry) {
        BigDecimal insurance = entry.getInsuranceDeduction();

        Adjustment existing = findAdjustmentByType(entry.getId(), AdjustmentType.INSURANCE);

        if (insurance != null && insurance.signum() > 0) {
            Adjustment adj = existing != null ? existing : new Adjustment();
            adj.setMonthlyEntry(entry);
            adj.setProject(entry.getProject());
            adj.setAdjustmentDate(entry.getMonth());
            adj.setAmount(insurance.negate()); // Store as negative since it's a deduction
            adj.setReason("Insurance Deduction");
            adj.setAdjustmentType(AdjustmentType.INSURANCE);
            adjustmentRepository.save(adj);
        } else if (existing != null) {
            adjustmentRepository.delete(existing);
        }
    }

    /**
     * Syncs the other adjustment record for a monthly entry.
     *
     * <p>If {@code otherAdjustment != 0}, creates or updates an adjustment record.
     * Negative values are stored as {@link AdjustmentType#NEGATIVE_DEDUCTION}
     * (e.g., fees, perm payments). Positive values are stored as
     * {@link AdjustmentType#POSITIVE_ADJUSTMENT}.</p>
     *
     * <p>If the amount becomes 0 on update, removes any existing adjustment record.</p>
     *
     * @param entry the monthly entry to sync
     */
    private void syncOtherAdjustment(MonthlyEntry entry) {
        BigDecimal adjustment = entry.getOtherAdjustment();

        // Find existing non-insurance adjustment for this entry
        Adjustment existingNeg = findAdjustmentByType(entry.getId(), AdjustmentType.NEGATIVE_DEDUCTION);
        Adjustment existingPos = findAdjustmentByType(entry.getId(), AdjustmentType.POSITIVE_ADJUSTMENT);

        if (adjustment != null && adjustment.signum() != 0) {
            AdjustmentType type = adjustment.signum() < 0
                    ? AdjustmentType.NEGATIVE_DEDUCTION
                    : AdjustmentType.POSITIVE_ADJUSTMENT;

            // Remove the opposite type if it exists
            if (adjustment.signum() < 0 && existingPos != null) {
                adjustmentRepository.delete(existingPos);
            } else if (adjustment.signum() > 0 && existingNeg != null) {
                adjustmentRepository.delete(existingNeg);
            }

            Adjustment adj = (type == AdjustmentType.NEGATIVE_DEDUCTION ? existingNeg : existingPos);
            if (adj == null) {
                adj = new Adjustment();
            }
            adj.setMonthlyEntry(entry);
            adj.setProject(entry.getProject());
            adj.setAdjustmentDate(entry.getMonth());
            adj.setAmount(adjustment);
            adj.setReason(entry.getNotes() != null && !entry.getNotes().isBlank()
                    ? entry.getNotes()
                    : (type == AdjustmentType.NEGATIVE_DEDUCTION ? "Fee/Deduction" : "Positive Adjustment"));
            adj.setAdjustmentType(type);
            adjustmentRepository.save(adj);
        } else {
            // Amount is 0, remove any existing adjustment records
            if (existingNeg != null) adjustmentRepository.delete(existingNeg);
            if (existingPos != null) adjustmentRepository.delete(existingPos);
        }
    }

    /**
     * Finds an adjustment record by monthly entry ID and type.
     *
     * @param entryId the monthly entry ID
     * @param type    the adjustment type to search for
     * @return the matching adjustment, or {@code null} if not found
     */
    private Adjustment findAdjustmentByType(Long entryId, AdjustmentType type) {
        return adjustmentRepository.findByMonthlyEntryIdOrderByAdjustmentDateDesc(entryId).stream()
                .filter(a -> a.getAdjustmentType() == type)
                .findFirst()
                .orElse(null);
    }
}

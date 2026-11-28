package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.DirectPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for {@link DirectPayment} entities.
 *
 * @author Payroll Reconciliation Team
 */
public interface DirectPaymentRepository extends JpaRepository<DirectPayment, Long> {

    /**
     * Returns all payment entries ordered by payment date ascending.
     *
     * @return list of all payments sorted by date
     */
    List<DirectPayment> findAllByOrderByPaymentDateAsc();

    /**
     * Returns the total gross amount that affects the running balance.
     * Excludes PERSONAL_EXPENSE entries — those are tracked only.
     *
     * @return sum of gross amounts for DIRECT_PAYMENT and FEE_DEDUCTION
     */
    @Query("SELECT COALESCE(SUM(dp.grossAmount), 0) FROM DirectPayment dp WHERE dp.paymentType <> 'PERSONAL_EXPENSE'")
    BigDecimal sumGrossAmount();
}

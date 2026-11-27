package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.DirectPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for {@link DirectPayment} entities.
 *
 * @author Payroll Reconciliation Team
 */
public interface DirectPaymentRepository extends JpaRepository<DirectPayment, Long> {

    /**
     * Returns all direct payments ordered by payment date ascending.
     *
     * @return list of all direct payments sorted by date
     */
    List<DirectPayment> findAllByOrderByPaymentDateAsc();

    /**
     * Returns the sum of all gross amounts across all direct payments.
     * Used by the dashboard to deduct from the total earned balance.
     *
     * @return total gross amount paid out, or zero if no payments exist
     */
    @Query("SELECT COALESCE(SUM(dp.grossAmount), 0) FROM DirectPayment dp")
    BigDecimal sumGrossAmount();
}

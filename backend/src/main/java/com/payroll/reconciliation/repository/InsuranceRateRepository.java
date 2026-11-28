package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.InsuranceRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link InsuranceRate} entities.
 *
 * @author Payroll Reconciliation Team
 */
public interface InsuranceRateRepository extends JpaRepository<InsuranceRate, Long> {

    /**
     * Returns all insurance rates ordered by effective_from ascending.
     *
     * @return all rates sorted by start date
     */
    List<InsuranceRate> findAllByOrderByEffectiveFromAsc();

    /**
     * Finds the active insurance rate for a given month.
     * Matches where effectiveFrom <= month AND (effectiveTo >= month OR effectiveTo IS NULL).
     *
     * @param month the month to look up (first day of month)
     * @return the matching rate, or empty if none found
     */
    @Query("SELECT r FROM InsuranceRate r WHERE r.effectiveFrom <= :month AND (r.effectiveTo IS NULL OR r.effectiveTo >= :month) ORDER BY r.effectiveFrom DESC")
    List<InsuranceRate> findRatesForMonth(@Param("month") LocalDate month);

    /**
     * Finds the currently open rate (effectiveTo IS NULL).
     *
     * @return the open-ended rate if one exists
     */
    Optional<InsuranceRate> findByEffectiveToIsNull();
}

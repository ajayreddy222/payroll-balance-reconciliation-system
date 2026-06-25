package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.Paystub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaystubRepository extends JpaRepository<Paystub, Long> {

    /**
     * Finds the paystub record associated with a specific monthly entry.
     *
     * @param monthlyEntryId the ID of the monthly entry
     * @return an optional containing the paystub if found
     */
    Optional<Paystub> findByMonthlyEntryId(Long monthlyEntryId);
}

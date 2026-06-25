package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.EmployerPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployerPaymentRepository extends JpaRepository<EmployerPayment, Long> {

    /**
     * Finds the employer payment record associated with a specific monthly entry.
     *
     * @param monthlyEntryId the ID of the monthly entry
     * @return an optional containing the employer payment if found
     */
    Optional<EmployerPayment> findByMonthlyEntryId(Long monthlyEntryId);
}

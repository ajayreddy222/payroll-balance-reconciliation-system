package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.YearlyBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link YearlyBalance} entities.
 * Provides CRUD operations and custom query methods for yearly balances.
 *
 * @author Payroll Reconciliation Team
 */
public interface YearlyBalanceRepository extends JpaRepository<YearlyBalance, Long> {

    /**
     * Finds the yearly balance record for a specific year.
     *
     * @param balanceYear the calendar year to look up
     * @return an optional containing the yearly balance if found
     */
    Optional<YearlyBalance> findByBalanceYear(Integer balanceYear);
}

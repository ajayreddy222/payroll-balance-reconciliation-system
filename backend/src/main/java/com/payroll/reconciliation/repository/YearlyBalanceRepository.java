package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.YearlyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    /**
     * Atomically inserts or updates the yearly balance for the given year.
     * Uses PostgreSQL ON CONFLICT DO UPDATE to prevent duplicate-key errors
     * when multiple requests recalculate balances concurrently.
     *
     * @param balanceYear    the calendar year
     * @param openingBalance carry-forward from the previous year
     * @param earnedBalance  sum of monthly balances for this year
     * @param endingBalance  opening + earned
     */
    @Modifying
    @Query(value = """
            INSERT INTO yearly_balances (balance_year, opening_balance, earned_balance, ending_balance)
            VALUES (:year, :opening, :earned, :ending)
            ON CONFLICT (balance_year) DO UPDATE
              SET opening_balance = EXCLUDED.opening_balance,
                  earned_balance  = EXCLUDED.earned_balance,
                  ending_balance  = EXCLUDED.ending_balance
            """, nativeQuery = true)
    void upsertByYear(
            @Param("year")    Integer year,
            @Param("opening") BigDecimal openingBalance,
            @Param("earned")  BigDecimal earnedBalance,
            @Param("ending")  BigDecimal endingBalance
    );
}

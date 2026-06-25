package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.MonthlyEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link MonthlyEntry} entities.
 * Provides CRUD operations and custom query methods for monthly entries.
 *
 * @author Payroll Reconciliation Team
 */
public interface MonthlyEntryRepository extends JpaRepository<MonthlyEntry, Long> {

    /**
     * Finds monthly entries within a date range, ordered by month ascending.
     *
     * @param start the start date (inclusive)
     * @param end   the end date (inclusive)
     * @return list of entries within the date range
     */
    List<MonthlyEntry> findByMonthBetweenOrderByMonthAsc(LocalDate start, LocalDate end);

    /**
     * Finds all monthly entries for a given project, ordered by month ascending.
     *
     * @param projectId the project ID to filter by
     * @return list of entries for the project
     */
    List<MonthlyEntry> findByProjectIdOrderByMonthAsc(Long projectId);

    /**
     * Finds a monthly entry by its project and month combination.
     *
     * @param projectId the project ID
     * @param month     the entry month
     * @return an optional containing the entry if found
     */
    Optional<MonthlyEntry> findByProjectIdAndMonth(Long projectId, LocalDate month);

    /**
     * Fetches all monthly entries with their associated project eagerly loaded.
     *
     * @return list of all entries with project data, ordered by month ascending
     */
    @Query("select e from MonthlyEntry e join fetch e.project order by e.month asc")
    List<MonthlyEntry> findAllWithProject();

    /**
     * Fetches monthly entries for a specific year with their associated project eagerly loaded.
     *
     * @param year the calendar year to filter by
     * @return list of entries for the year with project data, ordered by month ascending
     */
    @Query("select e from MonthlyEntry e join fetch e.project where year(e.month) = :year order by e.month asc")
    List<MonthlyEntry> findByYearWithProject(@Param("year") int year);
}

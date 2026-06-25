package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.Adjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Adjustment} entities.
 * Provides CRUD operations and custom query methods for adjustments.
 *
 * @author Payroll Reconciliation Team
 */
public interface AdjustmentRepository extends JpaRepository<Adjustment, Long> {

    /**
     * Finds all adjustments for a given monthly entry, ordered by date descending.
     *
     * @param monthlyEntryId the monthly entry ID to filter by
     * @return list of adjustments sorted by adjustment date descending
     */
    List<Adjustment> findByMonthlyEntryIdOrderByAdjustmentDateDesc(Long monthlyEntryId);

    /**
     * Finds all adjustments for a given project, ordered by date descending.
     *
     * @param projectId the project ID to filter by
     * @return list of adjustments sorted by adjustment date descending
     */
    List<Adjustment> findByProjectIdOrderByAdjustmentDateDesc(Long projectId);
}

package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Project} entities.
 * Provides CRUD operations and custom query methods for projects.
 *
 * @author Payroll Reconciliation Team
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Finds all active projects, ordered by client name and project name.
     *
     * @return list of active projects sorted alphabetically
     */
    List<Project> findByActiveTrueOrderByClientNameAscProjectNameAsc();
}

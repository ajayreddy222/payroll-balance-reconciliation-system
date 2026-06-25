package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.ProjectDtos.ProjectRequest;
import com.payroll.reconciliation.dto.ProjectDtos.ProjectResponse;
import com.payroll.reconciliation.entity.Project;
import com.payroll.reconciliation.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing projects.
 * Handles creation, updating, and retrieval of project records.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class ProjectService {
    private final ProjectRepository projects;

    /**
     * Constructs the service with the required project repository.
     *
     * @param projects the project repository
     */
    public ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    /**
     * Retrieves all projects.
     *
     * @return list of all projects as response DTOs
     */
    public List<ProjectResponse> list() {
        return projects.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Creates a new project from the given request.
     *
     * @param request the project creation request
     * @return the created project as a response DTO
     */
    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Project project = new Project();
        apply(project, request);
        return toResponse(projects.save(project));
    }

    /**
     * Updates an existing project by ID with the given request data.
     *
     * @param id      the project ID to update
     * @param request the updated project data
     * @return the updated project as a response DTO
     */
    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = projects.findById(id).orElseThrow();
        apply(project, request);
        return toResponse(project);
    }

    /**
     * Applies request data to a project entity, using defaults for null optional fields.
     *
     * @param project the project entity to update
     * @param request the request containing new values
     */
    private void apply(Project project, ProjectRequest request) {
        project.setClientName(request.clientName());
        project.setVendorName(request.vendorName());
        project.setProjectName(request.projectName());
        project.setEmployeeHourlyRate(request.employeeHourlyRate());
        project.setVendorFeePercentage(request.vendorFeePercentage() == null ? BigDecimal.ZERO : request.vendorFeePercentage());
        project.setEightyTwentyRate(request.eightyTwentyRate() == null ? BigDecimal.ZERO : request.eightyTwentyRate());
        project.setLcaAmount(request.lcaAmount() == null ? BigDecimal.ZERO : request.lcaAmount());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setActive(request.active() == null || request.active());
    }

    /**
     * Converts a Project entity to its response DTO.
     *
     * @param project the project entity
     * @return the response DTO
     */
    ProjectResponse toResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getClientName(), project.getVendorName(), project.getProjectName(),
                project.getEmployeeHourlyRate(), project.getVendorFeePercentage(), project.getEightyTwentyRate(), project.getLcaAmount(),
                project.getStartDate(), project.getEndDate(), project.isActive());
    }
}

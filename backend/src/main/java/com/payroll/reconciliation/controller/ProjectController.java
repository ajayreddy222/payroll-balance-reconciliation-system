package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.ProjectDtos.ProjectRequest;
import com.payroll.reconciliation.dto.ProjectDtos.ProjectResponse;
import com.payroll.reconciliation.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing projects.
 * Provides endpoints to list, create, and update project records.
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    /**
     * Constructs the controller with the required project service.
     *
     * @param projectService the service handling project business logic
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Retrieves all projects.
     *
     * @return list of all project responses
     */
    @GetMapping
    public List<ProjectResponse> list() {
        return projectService.list();
    }

    /**
     * Creates a new project.
     *
     * @param request the project creation request
     * @return the created project response
     */
    @PostMapping
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    /**
     * Updates an existing project by ID.
     *
     * @param id      the project ID to update
     * @param request the updated project data
     * @return the updated project response
     */
    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return projectService.update(id, request);
    }
}

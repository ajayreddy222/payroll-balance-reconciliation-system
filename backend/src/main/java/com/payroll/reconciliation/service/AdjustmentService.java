package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.AdjustmentDtos.AdjustmentRequest;
import com.payroll.reconciliation.dto.AdjustmentDtos.AdjustmentResponse;
import com.payroll.reconciliation.entity.Adjustment;
import com.payroll.reconciliation.repository.AdjustmentRepository;
import com.payroll.reconciliation.repository.AppUserRepository;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import com.payroll.reconciliation.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing payroll adjustments.
 * Handles creation and retrieval of adjustment records.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class AdjustmentService {
    private final AdjustmentRepository adjustments;
    private final MonthlyEntryRepository entries;
    private final ProjectRepository projects;
    private final AppUserRepository users;

    /**
     * Constructs the service with required repositories.
     *
     * @param adjustments the adjustment repository
     * @param entries     the monthly entry repository
     * @param projects    the project repository
     * @param users       the user repository
     */
    public AdjustmentService(AdjustmentRepository adjustments, MonthlyEntryRepository entries, ProjectRepository projects, AppUserRepository users) {
        this.adjustments = adjustments;
        this.entries = entries;
        this.projects = projects;
        this.users = users;
    }

    /**
     * Retrieves all adjustment records.
     *
     * @return list of all adjustments as response DTOs
     */
    public List<AdjustmentResponse> list() {
        return adjustments.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Creates a new adjustment record, optionally linking it to a monthly entry, project, and user.
     *
     * @param request        the adjustment creation request
     * @param authentication the current authentication context (for associating the creating user)
     * @return the created adjustment as a response DTO
     */
    @Transactional
    public AdjustmentResponse create(AdjustmentRequest request, Authentication authentication) {
        Adjustment adjustment = new Adjustment();
        if (request.monthlyEntryId() != null) {
            adjustment.setMonthlyEntry(entries.findById(request.monthlyEntryId()).orElseThrow());
        }
        if (request.projectId() != null) {
            adjustment.setProject(projects.findById(request.projectId()).orElseThrow());
        }
        adjustment.setAdjustmentDate(request.adjustmentDate());
        adjustment.setAmount(request.amount());
        adjustment.setReason(request.reason());
        adjustment.setAdjustmentType(request.adjustmentType());
        if (authentication != null) {
            users.findByEmail(authentication.getName()).ifPresent(adjustment::setUser);
        }
        return toResponse(adjustments.save(adjustment));
    }

    /**
     * Converts an Adjustment entity to its response DTO.
     *
     * @param adjustment the adjustment entity
     * @return the response DTO
     */
    private AdjustmentResponse toResponse(Adjustment adjustment) {
        String user = adjustment.getUser() == null ? null : adjustment.getUser().getFullName();
        return new AdjustmentResponse(adjustment.getId(), adjustment.getAdjustmentDate(), adjustment.getAmount(), adjustment.getReason(), adjustment.getAdjustmentType(), user);
    }
}

package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.AdjustmentDtos.AdjustmentRequest;
import com.payroll.reconciliation.dto.AdjustmentDtos.AdjustmentResponse;
import com.payroll.reconciliation.service.AdjustmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing payroll adjustments.
 * Provides endpoints to list and create adjustments.
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/adjustments")
public class AdjustmentController {
    private final AdjustmentService adjustmentService;

    /**
     * Constructs the controller with the required adjustment service.
     *
     * @param adjustmentService the service handling adjustment business logic
     */
    public AdjustmentController(AdjustmentService adjustmentService) {
        this.adjustmentService = adjustmentService;
    }

    /**
     * Retrieves all adjustments.
     *
     * @return list of all adjustment responses
     */
    @GetMapping
    public List<AdjustmentResponse> list() {
        return adjustmentService.list();
    }

    /**
     * Creates a new adjustment record.
     *
     * @param request        the adjustment request payload
     * @param authentication the current authenticated user
     * @return the created adjustment response
     */
    @PostMapping
    public AdjustmentResponse create(@Valid @RequestBody AdjustmentRequest request, Authentication authentication) {
        return adjustmentService.create(request, authentication);
    }
}

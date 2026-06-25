package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.dto.AuthDtos.AuthResponse;
import com.payroll.reconciliation.dto.AuthDtos.LoginRequest;
import com.payroll.reconciliation.dto.AuthDtos.RegisterRequest;
import com.payroll.reconciliation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Provides login and registration endpoints.
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Constructs the controller with the required authentication service.
     *
     * @param authService the service handling authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login credentials
     * @return authentication response containing the JWT token and user info
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Registers a new user and returns a JWT token.
     *
     * @param request the registration details
     * @return authentication response containing the JWT token and user info
     */
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}

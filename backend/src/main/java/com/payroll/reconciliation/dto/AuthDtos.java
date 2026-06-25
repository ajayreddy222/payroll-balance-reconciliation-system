package com.payroll.reconciliation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Objects for authentication-related API operations.
 *
 * @author Payroll Reconciliation Team
 */
public class AuthDtos {

    /**
     * Request DTO for user login.
     *
     * @param email    the user's email address
     * @param password the user's password
     */
    public record LoginRequest(@Email String email, @NotBlank String password) {}

    /**
     * Request DTO for user registration.
     *
     * @param fullName the user's full name
     * @param email    the user's email address
     * @param password the desired password
     */
    public record RegisterRequest(@NotBlank String fullName, @Email String email, @NotBlank String password) {}

    /**
     * Response DTO returned after successful authentication.
     *
     * @param token    the JWT authentication token
     * @param email    the authenticated user's email
     * @param fullName the authenticated user's full name
     * @param role     the user's role (e.g., ROLE_ADMIN, ROLE_USER)
     */
    public record AuthResponse(String token, String email, String fullName, String role) {}
}

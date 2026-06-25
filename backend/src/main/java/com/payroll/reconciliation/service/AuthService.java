package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.AuthDtos.AuthResponse;
import com.payroll.reconciliation.dto.AuthDtos.LoginRequest;
import com.payroll.reconciliation.dto.AuthDtos.RegisterRequest;
import com.payroll.reconciliation.entity.AppUser;
import com.payroll.reconciliation.entity.UserRole;
import com.payroll.reconciliation.repository.AppUserRepository;
import com.payroll.reconciliation.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user authentication and registration.
 * Handles login credential validation and new user creation with JWT token generation.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param users                 the user repository
     * @param passwordEncoder       the password encoder for hashing passwords
     * @param authenticationManager the Spring Security authentication manager
     * @param jwtService            the JWT token service
     */
    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates a user with their email and password, returning a JWT token on success.
     *
     * @param request the login request containing email and password
     * @return authentication response with JWT token and user details
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        AppUser user = users.findByEmail(request.email()).orElseThrow();
        return response(user);
    }

    /**
     * Registers a new user with the given details and returns a JWT token.
     *
     * @param request the registration request containing full name, email, and password
     * @return authentication response with JWT token and user details
     * @throws IllegalArgumentException if the email is already registered
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.ROLE_USER);
        return response(users.save(user));
    }

    /**
     * Builds an authentication response with a generated JWT token.
     *
     * @param user the authenticated or newly registered user
     * @return the authentication response DTO
     */
    private AuthResponse response(AppUser user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }
}

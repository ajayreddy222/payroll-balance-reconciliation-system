package com.payroll.reconciliation.security;

import com.payroll.reconciliation.config.CorsProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Service for JWT token generation and parsing.
 * Uses HMAC-SHA signing with a configurable secret key and expiration time.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class JwtService {
    private final CorsProperties properties;
    private final SecretKey key;

    /**
     * Constructs the service and initializes the signing key from application properties.
     *
     * @param properties the application properties containing the JWT secret
     */
    public JwtService(CorsProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT token for the given user.
     *
     * @param email the user's email address (used as the token subject)
     * @param role  the user's role (stored as a custom claim)
     * @return the signed JWT token string
     */
    public String generateToken(String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.jwtExpirationMinutes() * 60L)))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the subject (email) from a JWT token.
     *
     * @param token the JWT token string
     * @return the subject claim (user email)
     */
    public String subject(String token) {
        return claims(token).getSubject();
    }

    /**
     * Parses and returns all claims from a JWT token.
     *
     * @param token the JWT token string
     * @return the token claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

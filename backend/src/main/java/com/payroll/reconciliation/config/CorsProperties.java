package com.payroll.reconciliation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for application settings including JWT and CORS.
 * Bound to the "app" prefix in application properties.
 *
 * @author Payroll Reconciliation Team
 * @param jwtSecret the secret key used for signing JWT tokens
 * @param jwtExpirationMinutes token expiration time in minutes
 * @param corsAllowedOrigins comma-separated list of allowed CORS origins
 */
@ConfigurationProperties(prefix = "app")
public record CorsProperties(String jwtSecret, int jwtExpirationMinutes, String corsAllowedOrigins) {
}

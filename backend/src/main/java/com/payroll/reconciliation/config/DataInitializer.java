package com.payroll.reconciliation.config;

import com.payroll.reconciliation.entity.AppUser;
import com.payroll.reconciliation.entity.UserRole;
import com.payroll.reconciliation.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class that initializes default data on application startup.
 * Creates a default admin user if one does not already exist.
 *
 * @author Payroll Reconciliation Team
 */
@Configuration
public class DataInitializer {

    /**
     * Creates a {@link CommandLineRunner} bean that ensures a default admin user exists.
     *
     * @param users   the user repository for persistence operations
     * @param encoder the password encoder for hashing the admin password
     * @return a command line runner that seeds the admin user
     */
    @Bean
    CommandLineRunner adminUser(AppUserRepository users, PasswordEncoder encoder) {
        return args -> {
            AppUser admin = users.findByEmail("admin@example.com").orElseGet(AppUser::new);
            admin.setFullName("Administrator");
            admin.setEmail("admin@example.com");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole(UserRole.ROLE_ADMIN);
            users.save(admin);
        };
    }
}

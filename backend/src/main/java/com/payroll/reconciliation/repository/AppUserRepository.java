package com.payroll.reconciliation.repository;

import com.payroll.reconciliation.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AppUser} entities.
 * Provides CRUD operations and custom query methods for users.
 *
 * @author Payroll Reconciliation Team
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an optional containing the user if found
     */
    Optional<AppUser> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}

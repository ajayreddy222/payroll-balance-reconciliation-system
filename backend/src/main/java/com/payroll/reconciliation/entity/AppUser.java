package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * JPA entity representing an application user.
 * Stores authentication credentials and role information.
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user's full display name. */
    @Column(nullable = false)
    private String fullName;

    /** The user's unique email address used for login. */
    @Column(nullable = false, unique = true)
    private String email;

    /** The BCrypt-hashed password. */
    @Column(nullable = false)
    private String passwordHash;

    /** The user's assigned role for authorization. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.ROLE_USER;

    /** Timestamp of when this user was created (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}

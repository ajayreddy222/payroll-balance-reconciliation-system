package com.payroll.reconciliation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a payroll project/engagement.
 * A project defines the client, vendor, rates, and contains associated monthly entries.
 *
 * @author Payroll Reconciliation Team
 */
@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The end-client name. */
    @Column(nullable = false)
    private String clientName;

    /** The vendor/staffing agency name. */
    @Column(nullable = false)
    private String vendorName;

    /** The project or engagement name. */
    @Column(nullable = false)
    private String projectName;

    /** The employee's hourly bill rate for this project. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal employeeHourlyRate;

    /** The vendor fee percentage applied to payments. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal vendorFeePercentage = BigDecimal.ZERO;

    /** The 80/20 split rate used for gross-up calculations. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal eightyTwentyRate = BigDecimal.ZERO;

    /** The LCA (Labor Condition Application) wage amount. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal lcaAmount = BigDecimal.ZERO;

    /** The project start date. */
    @Column(nullable = false)
    private LocalDate startDate;

    /** The project end date (null if ongoing). */
    private LocalDate endDate;

    /** Whether this project is currently active. */
    @Column(nullable = false)
    private boolean active = true;

    /** Timestamp of when this project was created (database-managed). */
    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Monthly entries associated with this project. */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonthlyEntry> monthlyEntries = new ArrayList<>();
}

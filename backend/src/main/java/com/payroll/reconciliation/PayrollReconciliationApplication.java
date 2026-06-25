package com.payroll.reconciliation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Payroll Balance Reconciliation System.
 * Bootstraps the Spring Boot application.
 *
 * @author Payroll Reconciliation Team
 */
@SpringBootApplication
public class PayrollReconciliationApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PayrollReconciliationApplication.class, args);
    }
}

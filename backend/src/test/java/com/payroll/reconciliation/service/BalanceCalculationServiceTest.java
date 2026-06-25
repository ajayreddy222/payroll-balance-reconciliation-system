package com.payroll.reconciliation.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceCalculationServiceTest {
    private final BalanceCalculationService service = new BalanceCalculationService();

    @Test
    void calculatesActualEarningsAndMonthlyBalanceWithoutDirectPayment() {
        var result = service.calculate(new BigDecimal("160"), new BigDecimal("50"), new BigDecimal("7000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThat(result.actualEarnings()).isEqualByComparingTo("8000.00");
        assertThat(result.directEmployerGrossAmount()).isEqualByComparingTo("0.00");
        assertThat(result.finalBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void appliesGrossUpOnlyWhenDirectEmployerPaymentExists() {
        var result = service.calculate(new BigDecimal("200"), new BigDecimal("50"), BigDecimal.ZERO, new BigDecimal("10000"), BigDecimal.ZERO, BigDecimal.ZERO);
        assertThat(result.actualEarnings()).isEqualByComparingTo("10000.00");
        assertThat(result.directEmployerGrossAmount()).isEqualByComparingTo("12500.00");
        assertThat(result.finalBalance()).isEqualByComparingTo("-2500.00");
    }

    @Test
    void deductsNegativeAdjustmentsBeforeOtherCalculationsAndInsuranceDirectly() {
        var result = service.calculate(new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("3000"), BigDecimal.ZERO, new BigDecimal("250"), new BigDecimal("-500"));
        assertThat(result.actualEarnings()).isEqualByComparingTo("5000.00");
        assertThat(result.finalBalance()).isEqualByComparingTo("1250.00");
    }
}

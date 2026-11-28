package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.InsuranceRateDtos.InsuranceRateRequest;
import com.payroll.reconciliation.dto.InsuranceRateDtos.InsuranceRateResponse;
import com.payroll.reconciliation.entity.InsuranceRate;
import com.payroll.reconciliation.entity.MonthlyEntry;
import com.payroll.reconciliation.repository.InsuranceRateRepository;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing insurance rates and auto-applying them to monthly entries.
 *
 * <p>Insurance rates typically run Sep–Aug each year. When a new rate is saved,
 * the system auto-closes the previous open rate by setting its effectiveTo to
 * the month before the new rate starts.</p>
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class InsuranceRateService {

    private final InsuranceRateRepository repository;
    private final MonthlyEntryRepository monthlyEntries;
    private final BalanceCalculationService calculator;
    private final YearlyBalanceService yearlyBalanceService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param repository           the insurance rate repository
     * @param monthlyEntries       the monthly entry repository (for recalculate all)
     * @param calculator           the balance calculation service
     * @param yearlyBalanceService the yearly balance service
     */
    public InsuranceRateService(InsuranceRateRepository repository,
                                MonthlyEntryRepository monthlyEntries,
                                BalanceCalculationService calculator,
                                YearlyBalanceService yearlyBalanceService) {
        this.repository = repository;
        this.monthlyEntries = monthlyEntries;
        this.calculator = calculator;
        this.yearlyBalanceService = yearlyBalanceService;
    }

    /**
     * Returns all insurance rates ordered by effective_from ascending.
     *
     * @return list of all rate responses
     */
    public List<InsuranceRateResponse> list() {
        return repository.findAllByOrderByEffectiveFromAsc()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Creates a new insurance rate.
     * Auto-closes the previous open rate by setting its effectiveTo to the
     * month before the new rate's effectiveFrom.
     *
     * @param request the rate request
     * @return the saved rate response
     */
    @Transactional
    public InsuranceRateResponse create(InsuranceRateRequest request) {
        // Auto-close the previously open rate
        repository.findByEffectiveToIsNull().ifPresent(existing -> {
            LocalDate closingDate = request.effectiveFrom().minusMonths(1).withDayOfMonth(1);
            existing.setEffectiveTo(closingDate);
            repository.save(existing);
        });

        InsuranceRate rate = new InsuranceRate();
        apply(rate, request);
        return toResponse(repository.save(rate));
    }

    /**
     * Updates an existing insurance rate by ID.
     *
     * @param id      the ID of the rate to update
     * @param request the updated rate data
     * @return the updated rate response
     */
    @Transactional
    public InsuranceRateResponse update(Long id, InsuranceRateRequest request) {
        InsuranceRate rate = repository.findById(id).orElseThrow();
        apply(rate, request);
        return toResponse(repository.save(rate));
    }

    /**
     * Deletes an insurance rate by ID.
     *
     * @param id the ID of the rate to delete
     */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Looks up the insurance amount for a given month and project.
     * Returns zero if no rate is found OR if the project has no LCA (lca_amount = 0).
     * Insurance only applies to paystub-running projects (lca_amount > 0).
     *
     * @param month   the month to look up
     * @param project the project — insurance only applied if lcaAmount > 0
     * @return the monthly insurance amount, or zero
     */
    public BigDecimal getRateForMonth(LocalDate month, com.payroll.reconciliation.entity.Project project) {
        // Only apply insurance to projects that run a paystub (lca_amount > 0)
        if (project.getLcaAmount() == null || project.getLcaAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        List<InsuranceRate> rates = repository.findRatesForMonth(month);
        if (rates.isEmpty()) return BigDecimal.ZERO;
        return rates.get(0).getMonthlyAmount();
    }

    /**
     * Recalculates insurance deductions for all existing monthly entries
     * based on the current insurance rates, then recalculates yearly balances.
     *
     * @return number of entries updated
     */
    @Transactional
    public int recalculateAll() {
        List<MonthlyEntry> all = monthlyEntries.findAllWithProject();
        for (MonthlyEntry entry : all) {
            BigDecimal insurance = getRateForMonth(entry.getMonth(), entry.getProject());
            entry.setInsuranceDeduction(insurance);
            calculator.apply(entry);
            monthlyEntries.save(entry);
        }
        yearlyBalanceService.recalculate();
        return all.size();
    }

    /**
     * Applies request fields to an InsuranceRate entity.
     *
     * @param rate    the entity to populate
     * @param request the request DTO
     */
    private void apply(InsuranceRate rate, InsuranceRateRequest request) {
        rate.setEffectiveFrom(request.effectiveFrom());
        rate.setEffectiveTo(request.effectiveTo());
        rate.setMonthlyAmount(request.monthlyAmount());
        rate.setNotes(request.notes());
    }

    /**
     * Converts an InsuranceRate entity to a response DTO.
     *
     * @param rate the entity to convert
     * @return the response DTO
     */
    private InsuranceRateResponse toResponse(InsuranceRate rate) {
        return new InsuranceRateResponse(
                rate.getId(),
                rate.getEffectiveFrom(),
                rate.getEffectiveTo(),
                rate.getMonthlyAmount(),
                rate.getNotes()
        );
    }
}

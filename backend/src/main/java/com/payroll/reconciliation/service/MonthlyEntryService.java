package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.MonthlyEntryDtos.MonthlyEntryRequest;
import com.payroll.reconciliation.dto.MonthlyEntryDtos.MonthlyEntryResponse;
import com.payroll.reconciliation.entity.MonthlyEntry;
import com.payroll.reconciliation.entity.Project;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import com.payroll.reconciliation.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing monthly payroll entries.
 *
 * <p>On save:
 * <ol>
 *   <li>Auto-looks up the insurance rate for the entry's month</li>
 *   <li>Applies balance calculation: Actual − Paystub − Insurance</li>
 *   <li>Recalculates yearly carry-forward balances</li>
 * </ol>
 * </p>
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class MonthlyEntryService {

    private final MonthlyEntryRepository entries;
    private final ProjectRepository projects;
    private final BalanceCalculationService calculator;
    private final InsuranceRateService insuranceRateService;
    private final YearlyBalanceService yearlyBalanceService;

    /**
     * Constructs the service with all required dependencies.
     *
     * @param entries              monthly entry repository
     * @param projects             project repository
     * @param calculator           balance calculation service
     * @param insuranceRateService insurance rate lookup service
     * @param yearlyBalanceService yearly balance recalculation service
     */
    public MonthlyEntryService(MonthlyEntryRepository entries,
                               ProjectRepository projects,
                               BalanceCalculationService calculator,
                               InsuranceRateService insuranceRateService,
                               YearlyBalanceService yearlyBalanceService) {
        this.entries = entries;
        this.projects = projects;
        this.calculator = calculator;
        this.insuranceRateService = insuranceRateService;
        this.yearlyBalanceService = yearlyBalanceService;
    }

    /**
     * Returns all monthly entries ordered by month ascending.
     *
     * @return list of all monthly entry responses
     */
    public List<MonthlyEntryResponse> list() {
        return entries.findAllWithProject().stream().map(this::toResponse).toList();
    }

    /**
     * Creates or updates a monthly entry for the given project + month combination.
     * Auto-applies the insurance rate for the entry's month.
     *
     * @param request the monthly entry data
     * @return the saved monthly entry response
     */
    @Transactional
    public MonthlyEntryResponse upsert(MonthlyEntryRequest request) {
        Project project = projects.findById(request.projectId()).orElseThrow();
        MonthlyEntry entry = entries.findByProjectIdAndMonth(request.projectId(), request.month())
                .orElseGet(MonthlyEntry::new);
        applyRequest(entry, project, request);
        applyInsurance(entry);
        calculator.apply(entry);
        MonthlyEntry saved = entries.save(entry);
        yearlyBalanceService.recalculate();
        return toResponse(saved);
    }

    /**
     * Updates an existing monthly entry by ID.
     * Re-applies the insurance rate for the entry's month.
     *
     * @param id      the ID of the entry to update
     * @param request the updated entry data
     * @return the updated monthly entry response
     */
    @Transactional
    public MonthlyEntryResponse update(Long id, MonthlyEntryRequest request) {
        MonthlyEntry entry = entries.findById(id).orElseThrow();
        Project project = projects.findById(request.projectId()).orElseThrow();
        applyRequest(entry, project, request);
        applyInsurance(entry);
        calculator.apply(entry);
        MonthlyEntry saved = entries.save(entry);
        yearlyBalanceService.recalculate();
        return toResponse(saved);
    }

    /**
     * Returns a single monthly entry by ID.
     *
     * @param id the entry ID
     * @return the monthly entry response
     */
    public MonthlyEntryResponse get(Long id) {
        return toResponse(entries.findById(id).orElseThrow());
    }

    /**
     * Deletes a monthly entry and recalculates yearly balances.
     *
     * @param id the entry ID to delete
     */
    @Transactional
    public void delete(Long id) {
        entries.deleteById(id);
        yearlyBalanceService.recalculate();
    }

    /**
     * Converts a {@link MonthlyEntry} to a {@link MonthlyEntryResponse}.
     *
     * @param entry the entity to convert
     * @return the response DTO
     */
    public MonthlyEntryResponse toResponse(MonthlyEntry entry) {
        return new MonthlyEntryResponse(
                entry.getId(),
                entry.getProject().getId(),
                entry.getProject().getProjectName(),
                entry.getProject().getClientName(),
                entry.getProject().getVendorName(),
                entry.getMonth(),
                entry.getHoursWorked(),
                entry.getEmployeeHourlyRate(),
                entry.getActualEarnings(),
                entry.getPaystubAmountReceived(),
                entry.getInsuranceDeduction(),
                entry.getMonthlyBalance(),
                entry.getNotes()
        );
    }

    /**
     * Auto-applies the insurance deduction for the entry's month
     * from the insurance_rates table.
     *
     * @param entry the entry to populate insurance for
     */
    private void applyInsurance(MonthlyEntry entry) {
        BigDecimal insurance = insuranceRateService.getRateForMonth(entry.getMonth(), entry.getProject());
        entry.setInsuranceDeduction(insurance);
    }

    /**
     * Applies request fields to a MonthlyEntry entity.
     *
     * @param entry   the entity to populate
     * @param project the associated project
     * @param request the request DTO
     */
    private void applyRequest(MonthlyEntry entry, Project project, MonthlyEntryRequest request) {
        entry.setProject(project);
        entry.setMonth(request.month());
        entry.setHoursWorked(nz(request.hoursWorked()));
        entry.setEmployeeHourlyRate(nz(request.employeeHourlyRate()));
        entry.setPaystubAmountReceived(nz(request.paystubAmountReceived()));
        entry.setNotes(request.notes());
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

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
 * Service responsible for managing monthly payroll entries.
 *
 * <p>Handles CRUD operations for monthly entries and orchestrates:
 * <ul>
 *   <li>Balance calculation via {@link BalanceCalculationService}</li>
 *   <li>Detail record synchronization via {@link DetailRecordService}
 *       (paystubs, employer_payments, adjustments tables)</li>
 *   <li>Yearly balance recalculation via {@link YearlyBalanceService}</li>
 * </ul>
 * </p>
 *
 * @author PayrollReconciliation
 */
@Service
public class MonthlyEntryService {

    private final MonthlyEntryRepository entries;
    private final ProjectRepository projects;
    private final BalanceCalculationService calculator;
    private final DetailRecordService detailRecordService;
    private final YearlyBalanceService yearlyBalanceService;

    /**
     * Constructs a new MonthlyEntryService with all required dependencies.
     *
     * @param entries              repository for monthly entry persistence
     * @param projects             repository for project lookups
     * @param calculator           service for balance calculations
     * @param detailRecordService  service for syncing detail/audit tables
     * @param yearlyBalanceService service for recalculating yearly balances
     */
    public MonthlyEntryService(MonthlyEntryRepository entries,
                               ProjectRepository projects,
                               BalanceCalculationService calculator,
                               DetailRecordService detailRecordService,
                               YearlyBalanceService yearlyBalanceService) {
        this.entries = entries;
        this.projects = projects;
        this.calculator = calculator;
        this.detailRecordService = detailRecordService;
        this.yearlyBalanceService = yearlyBalanceService;
    }

    /**
     * Lists all monthly entries ordered by month ascending.
     *
     * @return list of all monthly entry responses with project details
     */
    public List<MonthlyEntryResponse> list() {
        return entries.findAllWithProject().stream().map(this::toResponse).toList();
    }

    /**
     * Creates a new monthly entry or updates an existing one based on project + month combination.
     *
     * <p>After saving the entry, this method:
     * <ol>
     *   <li>Applies balance calculations (actual earnings, monthly balance)</li>
     *   <li>Syncs detail records to paystubs, employer_payments, and adjustments tables</li>
     *   <li>Recalculates yearly balances for carry-forward tracking</li>
     * </ol>
     * </p>
     *
     * @param request the monthly entry data to create or update
     * @return the saved monthly entry response
     */
    @Transactional
    public MonthlyEntryResponse upsert(MonthlyEntryRequest request) {
        Project project = projects.findById(request.projectId()).orElseThrow();
        MonthlyEntry entry = entries.findByProjectIdAndMonth(request.projectId(), request.month())
                .orElseGet(MonthlyEntry::new);
        applyRequest(entry, project, request);
        calculator.apply(entry);
        MonthlyEntry saved = entries.save(entry);

        // Sync detail/audit tables
        detailRecordService.syncDetailRecords(saved);

        // Recalculate yearly balances for carry-forward
        yearlyBalanceService.recalculate();

        return toResponse(saved);
    }

    /**
     * Updates an existing monthly entry by its ID.
     *
     * <p>After saving the entry, this method:
     * <ol>
     *   <li>Applies balance calculations (actual earnings, monthly balance)</li>
     *   <li>Syncs detail records to paystubs, employer_payments, and adjustments tables</li>
     *   <li>Recalculates yearly balances for carry-forward tracking</li>
     * </ol>
     * </p>
     *
     * @param id      the ID of the monthly entry to update
     * @param request the updated monthly entry data
     * @return the updated monthly entry response
     * @throws java.util.NoSuchElementException if no entry with the given ID exists
     */
    @Transactional
    public MonthlyEntryResponse update(Long id, MonthlyEntryRequest request) {
        MonthlyEntry entry = entries.findById(id).orElseThrow();
        Project project = projects.findById(request.projectId()).orElseThrow();
        applyRequest(entry, project, request);
        calculator.apply(entry);
        MonthlyEntry saved = entries.save(entry);

        // Sync detail/audit tables
        detailRecordService.syncDetailRecords(saved);

        // Recalculate yearly balances for carry-forward
        yearlyBalanceService.recalculate();

        return toResponse(saved);
    }

    /**
     * Retrieves a single monthly entry by its ID.
     *
     * @param id the ID of the monthly entry
     * @return the monthly entry response
     * @throws java.util.NoSuchElementException if no entry with the given ID exists
     */
    public MonthlyEntryResponse get(Long id) {
        return toResponse(entries.findById(id).orElseThrow());
    }

    /**
     * Deletes a monthly entry and all associated detail records.
     *
     * <p>Removes the entry from the database along with any paystub,
     * employer payment, and adjustment records linked to it.
     * Recalculates yearly balances after deletion.</p>
     *
     * @param id the ID of the monthly entry to delete
     */
    @Transactional
    public void delete(Long id) {
        detailRecordService.removeDetailRecords(id);
        entries.deleteById(id);

        // Recalculate yearly balances after deletion
        yearlyBalanceService.recalculate();
    }

    /**
     * Converts a {@link MonthlyEntry} entity to a {@link MonthlyEntryResponse} DTO.
     *
     * @param entry the entity to convert
     * @return the response DTO with all calculated fields
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
                entry.getDirectEmployerPayment(),
                calculator.grossEmployerPayment(entry.getDirectEmployerPayment()),
                entry.getInsuranceDeduction(),
                entry.getOtherAdjustment(),
                entry.getMonthlyBalance(),
                entry.getNotes()
        );
    }

    /**
     * Applies the request data to a monthly entry entity.
     *
     * <p>Sets all fields from the request onto the entry, using zero
     * for any null numeric values to prevent NullPointerException
     * during calculations.</p>
     *
     * @param entry   the entry entity to populate
     * @param project the associated project entity
     * @param request the request DTO containing the new values
     */
    private void applyRequest(MonthlyEntry entry, Project project, MonthlyEntryRequest request) {
        entry.setProject(project);
        entry.setMonth(request.month());
        entry.setHoursWorked(nz(request.hoursWorked()));
        entry.setEmployeeHourlyRate(nz(request.employeeHourlyRate()));
        entry.setPaystubAmountReceived(nz(request.paystubAmountReceived()));
        entry.setDirectEmployerPayment(nz(request.directEmployerPayment()));
        entry.setInsuranceDeduction(nz(request.insuranceDeduction()));
        entry.setOtherAdjustment(nz(request.otherAdjustment()));
        entry.setNotes(request.notes());
    }

    /**
     * Returns zero if the given value is null, otherwise returns the value as-is.
     *
     * @param value the BigDecimal value to null-check
     * @return the value, or {@link BigDecimal#ZERO} if null
     */
    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

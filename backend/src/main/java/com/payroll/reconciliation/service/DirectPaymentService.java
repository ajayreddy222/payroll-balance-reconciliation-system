package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentBatchRequest;
import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentRequest;
import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentResponse;
import com.payroll.reconciliation.entity.DirectPayment;
import com.payroll.reconciliation.entity.PaymentType;
import com.payroll.reconciliation.repository.DirectPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service for managing the payment ledger.
 *
 * <p>Gross-up rules:
 * <ul>
 *   <li>DIRECT_PAYMENT: grossAmount = amountReceived ÷ 0.80</li>
 *   <li>FEE_DEDUCTION: grossAmount = amountReceived (no gross-up)</li>
 *   <li>PERSONAL_EXPENSE: grossAmount = amountReceived (tracked only, excluded from balance)</li>
 * </ul>
 * </p>
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class DirectPaymentService {

    private static final BigDecimal NET_RATE = new BigDecimal("0.80");

    private final DirectPaymentRepository repository;

    /**
     * Constructs the service with the required repository.
     *
     * @param repository the direct payment repository
     */
    public DirectPaymentService(DirectPaymentRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all payment entries ordered by date ascending.
     *
     * @return list of all payment responses
     */
    public List<DirectPaymentResponse> list() {
        return repository.findAllByOrderByPaymentDateAsc()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Creates a single payment entry.
     *
     * @param request the payment request
     * @return the saved payment response
     */
    @Transactional
    public DirectPaymentResponse create(DirectPaymentRequest request) {
        DirectPayment payment = new DirectPayment();
        apply(payment, request);
        return toResponse(repository.save(payment));
    }

    /**
     * Creates multiple payment entries in a single transaction.
     *
     * @param batchRequest the batch request
     * @return list of saved payment responses
     */
    @Transactional
    public List<DirectPaymentResponse> createBatch(DirectPaymentBatchRequest batchRequest) {
        return batchRequest.payments().stream()
                .map(request -> {
                    DirectPayment payment = new DirectPayment();
                    apply(payment, request);
                    return toResponse(repository.save(payment));
                })
                .toList();
    }

    /**
     * Updates an existing payment entry by ID.
     *
     * @param id      the ID to update
     * @param request the updated data
     * @return the updated payment response
     */
    @Transactional
    public DirectPaymentResponse update(Long id, DirectPaymentRequest request) {
        DirectPayment payment = repository.findById(id).orElseThrow();
        apply(payment, request);
        return toResponse(repository.save(payment));
    }

    /**
     * Deletes a payment entry by ID.
     *
     * @param id the ID to delete
     */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Returns the total gross amount that affects the balance.
     * Excludes PERSONAL_EXPENSE entries.
     *
     * @return sum of gross amounts for DIRECT_PAYMENT and FEE_DEDUCTION only
     */
    public BigDecimal totalGrossAmount() {
        return repository.sumGrossAmount();
    }

    /**
     * Applies request fields to a DirectPayment entity.
     * Calculates grossAmount based on payment type.
     *
     * @param payment the entity to populate
     * @param request the request DTO
     */
    private void apply(DirectPayment payment, DirectPaymentRequest request) {
        payment.setPaymentDate(request.paymentDate());
        payment.setPaymentType(request.paymentType());
        payment.setPaymentMode(request.paymentMode());
        payment.setAmountReceived(request.amountReceived());
        payment.setNotes(request.notes());

        if (request.paymentType() == PaymentType.DIRECT_PAYMENT) {
            payment.setGrossAmount(
                    request.amountReceived().divide(NET_RATE, 2, RoundingMode.HALF_UP)
            );
        } else {
            payment.setGrossAmount(request.amountReceived());
        }
    }

    /**
     * Converts a DirectPayment entity to a response DTO.
     *
     * @param payment the entity to convert
     * @return the response DTO
     */
    private DirectPaymentResponse toResponse(DirectPayment payment) {
        boolean affectsBalance = payment.getPaymentType() != PaymentType.PERSONAL_EXPENSE;
        return new DirectPaymentResponse(
                payment.getId(),
                payment.getPaymentDate(),
                payment.getPaymentType(),
                payment.getPaymentMode(),
                payment.getAmountReceived(),
                payment.getGrossAmount(),
                affectsBalance,
                payment.getNotes()
        );
    }
}

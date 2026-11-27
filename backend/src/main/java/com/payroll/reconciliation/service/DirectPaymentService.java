package com.payroll.reconciliation.service;

import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentRequest;
import com.payroll.reconciliation.dto.DirectPaymentDtos.DirectPaymentResponse;
import com.payroll.reconciliation.entity.DirectPayment;
import com.payroll.reconciliation.repository.DirectPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service for managing standalone direct payments from the employer.
 *
 * <p>Direct payments are cash or check amounts received outside of the regular
 * payroll system. The 80-20 rule is applied: the employee enters the net amount
 * received in hand, and the system calculates the gross amount (net ÷ 0.80)
 * which is deducted from the total running balance.</p>
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
     * Returns all direct payments ordered by payment date ascending.
     *
     * @return list of all direct payment responses
     */
    public List<DirectPaymentResponse> list() {
        return repository.findAllByOrderByPaymentDateAsc()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Creates a new direct payment.
     * Automatically calculates grossAmount = amountReceived ÷ 0.80.
     *
     * @param request the payment request with net amount received
     * @return the saved direct payment response
     */
    @Transactional
    public DirectPaymentResponse create(DirectPaymentRequest request) {
        DirectPayment payment = new DirectPayment();
        apply(payment, request);
        return toResponse(repository.save(payment));
    }

    /**
     * Updates an existing direct payment by ID.
     * Recalculates grossAmount from the new amountReceived.
     *
     * @param id      the ID of the payment to update
     * @param request the updated payment data
     * @return the updated direct payment response
     * @throws java.util.NoSuchElementException if no payment with the given ID exists
     */
    @Transactional
    public DirectPaymentResponse update(Long id, DirectPaymentRequest request) {
        DirectPayment payment = repository.findById(id).orElseThrow();
        apply(payment, request);
        return toResponse(repository.save(payment));
    }

    /**
     * Deletes a direct payment by ID.
     *
     * @param id the ID of the payment to delete
     */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Returns the total gross amount paid out across all direct payments.
     * Used by the dashboard to compute the current balance.
     *
     * @return sum of all gross amounts
     */
    public BigDecimal totalGrossAmount() {
        return repository.sumGrossAmount();
    }

    /**
     * Applies the request fields to a DirectPayment entity and calculates gross amount.
     *
     * @param payment the entity to populate
     * @param request the request DTO
     */
    private void apply(DirectPayment payment, DirectPaymentRequest request) {
        payment.setPaymentDate(request.paymentDate());
        payment.setAmountReceived(request.amountReceived());
        payment.setGrossAmount(
                request.amountReceived().divide(NET_RATE, 2, RoundingMode.HALF_UP)
        );
        payment.setNotes(request.notes());
    }

    /**
     * Converts a DirectPayment entity to a response DTO.
     *
     * @param payment the entity to convert
     * @return the response DTO
     */
    private DirectPaymentResponse toResponse(DirectPayment payment) {
        return new DirectPaymentResponse(
                payment.getId(),
                payment.getPaymentDate(),
                payment.getAmountReceived(),
                payment.getGrossAmount(),
                payment.getNotes()
        );
    }
}

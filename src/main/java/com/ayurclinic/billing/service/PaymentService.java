package com.ayurclinic.billing.service;

import com.ayurclinic.billing.dto.PaymentCreateRequest;
import com.ayurclinic.billing.dto.PaymentResponse;
import com.ayurclinic.billing.entity.Invoice;
import com.ayurclinic.billing.entity.Payment;
import com.ayurclinic.billing.enums.InvoiceStatus;
import com.ayurclinic.billing.enums.PaymentStatus;
import com.ayurclinic.billing.repository.InvoiceRepository;
import com.ayurclinic.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public PaymentResponse createPayment(
            UUID tenantId,
            UUID invoiceId,
            PaymentCreateRequest request
    ) {
        Invoice invoice = getInvoice(
                tenantId,
                invoiceId
        );

        validateInvoiceCanReceivePayment(invoice);

        BigDecimal amount = request.getAmount();

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        BigDecimal outstandingAmount =
                calculateOutstandingAmount(invoice);

        if (amount.compareTo(outstandingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount cannot exceed outstanding invoice amount"
            );
        }

        Payment payment = new Payment();

        payment.setTenantId(tenantId);
        payment.setClinicId(invoice.getClinicId());
        payment.setPatientId(invoice.getPatientId());
        payment.setInvoiceId(invoice.getId());

        payment.setAmount(amount);

        payment.setPaymentDate(
                request.getPaymentDate() != null
                        ? request.getPaymentDate()
                        : LocalDate.now()
        );

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

        payment.setTransactionReference(
                request.getTransactionReference()
        );

        payment.setStatus(PaymentStatus.PENDING);
        payment.setNotes(request.getNotes());

        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());

        Payment saved = paymentRepository.save(payment);

        return toResponse(saved);
    }

    public PaymentResponse completePayment(
            UUID tenantId,
            UUID paymentId
    ) {
        Payment payment = getPaymentEntity(
                tenantId,
                paymentId
        );

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Payment is already completed"
            );
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending payments can be completed"
            );
        }

        Invoice invoice = getInvoice(
                tenantId,
                payment.getInvoiceId()
        );

        validateInvoiceCanReceivePayment(invoice);

        BigDecimal currentOutstanding =
                calculateOutstandingAmount(invoice);

        if (payment.getAmount().compareTo(currentOutstanding) > 0) {
            throw new IllegalStateException(
                    "Payment amount exceeds the current outstanding invoice amount"
            );
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setUpdatedAt(OffsetDateTime.now());

        Payment savedPayment =
                paymentRepository.save(payment);

        BigDecimal newPaidAmount =
                defaultAmount(invoice.getPaidAmount())
                        .add(payment.getAmount());

        invoice.setPaidAmount(newPaidAmount);

        updateInvoiceStatus(invoice);

        invoice.setUpdatedAt(OffsetDateTime.now());

        invoiceRepository.save(invoice);

        return toResponse(savedPayment);
    }

    public PaymentResponse failPayment(
            UUID tenantId,
            UUID paymentId
    ) {
        Payment payment = getPaymentEntity(
                tenantId,
                paymentId
        );

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending payments can be marked as failed"
            );
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setUpdatedAt(OffsetDateTime.now());

        return toResponse(
                paymentRepository.save(payment)
        );
    }

    public PaymentResponse refundPayment(
            UUID tenantId,
            UUID paymentId
    ) {
        Payment payment = getPaymentEntity(
                tenantId,
                paymentId
        );

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Payment is already refunded"
            );
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only completed payments can be refunded"
            );
        }

        Invoice invoice = getInvoice(
                tenantId,
                payment.getInvoiceId()
        );

        BigDecimal currentPaidAmount =
                defaultAmount(invoice.getPaidAmount());

        BigDecimal newPaidAmount =
                currentPaidAmount.subtract(
                        payment.getAmount()
                );

        if (newPaidAmount.compareTo(BigDecimal.ZERO) < 0) {
            newPaidAmount = BigDecimal.ZERO;
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(OffsetDateTime.now());

        Payment savedPayment =
                paymentRepository.save(payment);

        invoice.setPaidAmount(newPaidAmount);

        updateInvoiceStatus(invoice);

        invoice.setUpdatedAt(OffsetDateTime.now());

        invoiceRepository.save(invoice);

        return toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
            UUID tenantId,
            UUID paymentId
    ) {
        return toResponse(
                getPaymentEntity(
                        tenantId,
                        paymentId
                )
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getInvoicePayments(
            UUID tenantId,
            UUID invoiceId
    ) {
        getInvoice(
                tenantId,
                invoiceId
        );

        return paymentRepository
                .findByTenantIdAndInvoiceIdOrderByPaymentDateDesc(
                        tenantId,
                        invoiceId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPatientPayments(
            UUID tenantId,
            UUID patientId
    ) {
        return paymentRepository
                .findByTenantIdAndPatientIdOrderByPaymentDateDesc(
                        tenantId,
                        patientId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(
            UUID tenantId,
            PaymentStatus status
    ) {
        return paymentRepository
                .findByTenantIdAndStatusOrderByPaymentDateDesc(
                        tenantId,
                        status
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Invoice getInvoice(
            UUID tenantId,
            UUID invoiceId
    ) {
        return invoiceRepository
                .findByIdAndTenantId(
                        invoiceId,
                        tenantId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invoice not found"
                        )
                );
    }

    private Payment getPaymentEntity(
            UUID tenantId,
            UUID paymentId
    ) {
        return paymentRepository
                .findByIdAndTenantId(
                        paymentId,
                        tenantId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found"
                        )
                );
    }

    private void validateInvoiceCanReceivePayment(
            Invoice invoice
    ) {
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cancelled invoices cannot receive payments"
            );
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException(
                    "Invoice is already fully paid"
            );
        }
    }

    private BigDecimal calculateOutstandingAmount(
            Invoice invoice
    ) {
        BigDecimal totalAmount =
                defaultAmount(invoice.getTotalAmount());

        BigDecimal paidAmount =
                defaultAmount(invoice.getPaidAmount());

        BigDecimal outstanding =
                totalAmount.subtract(paidAmount);

        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return outstanding;
    }

    private void updateInvoiceStatus(
            Invoice invoice
    ) {
        BigDecimal totalAmount =
                defaultAmount(invoice.getTotalAmount());

        BigDecimal paidAmount =
                defaultAmount(invoice.getPaidAmount());

        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.ISSUED);
            return;
        }

        if (paidAmount.compareTo(totalAmount) >= 0) {
            invoice.setPaidAmount(totalAmount);
            invoice.setStatus(InvoiceStatus.PAID);
            return;
        }

        invoice.setStatus(
                InvoiceStatus.PARTIALLY_PAID
        );
    }

    private BigDecimal defaultAmount(
            BigDecimal amount
    ) {
        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }

    private PaymentResponse toResponse(
            Payment payment
    ) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .tenantId(payment.getTenantId())
                .clinicId(payment.getClinicId())
                .patientId(payment.getPatientId())
                .invoiceId(payment.getInvoiceId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .transactionReference(
                        payment.getTransactionReference()
                )
                .status(payment.getStatus())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
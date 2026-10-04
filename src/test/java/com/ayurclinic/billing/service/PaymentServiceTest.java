package com.ayurclinic.billing.service;

import com.ayurclinic.billing.dto.PaymentCreateRequest;
import com.ayurclinic.billing.entity.Invoice;
import com.ayurclinic.billing.entity.Payment;
import com.ayurclinic.billing.enums.InvoiceStatus;
import com.ayurclinic.billing.enums.PaymentMethod;
import com.ayurclinic.billing.enums.PaymentStatus;
import com.ayurclinic.billing.repository.InvoiceRepository;
import com.ayurclinic.billing.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID tenantId;
    private UUID invoiceId;
    private UUID paymentId;
    private UUID clinicId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        patientId = UUID.randomUUID();
    }

    // ============================================================
    // Helper
    // ============================================================

    private PaymentCreateRequest validRequest(BigDecimal amount) {
        PaymentCreateRequest request = new PaymentCreateRequest();

        request.setAmount(amount);
        request.setPaymentDate(LocalDate.now());
        request.setPaymentMethod(PaymentMethod.UPI);
        request.setTransactionReference("TXN-001");
        request.setNotes("Test payment");

        return request;
    }

    // ============================================================
    // 1. INVOICE TENANT ISOLATION
    // ============================================================

    @Test
    void shouldRejectPaymentWhenInvoiceBelongsToAnotherTenant() {

        PaymentCreateRequest request =
                validRequest(new BigDecimal("500"));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(
                        tenantId,
                        invoiceId,
                        request
                )
        );

        verify(invoiceRepository)
                .findByIdAndTenantId(
                        invoiceId,
                        tenantId
                );

        verifyNoInteractions(paymentRepository);
    }

    // ============================================================
    // 2. CANCELLED INVOICE
    // ============================================================

    @Test
    void shouldRejectPaymentForCancelledInvoice() {

        Invoice invoice = mock(Invoice.class);

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.CANCELLED);

        PaymentCreateRequest request =
                validRequest(new BigDecimal("500"));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.createPayment(
                        tenantId,
                        invoiceId,
                        request
                )
        );

        verifyNoInteractions(paymentRepository);
    }

    // ============================================================
    // 3. ALREADY PAID INVOICE
    // ============================================================

    @Test
    void shouldRejectPaymentForPaidInvoice() {

        Invoice invoice = mock(Invoice.class);

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.PAID);

        PaymentCreateRequest request =
                validRequest(new BigDecimal("100"));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.createPayment(
                        tenantId,
                        invoiceId,
                        request
                )
        );

        verifyNoInteractions(paymentRepository);
    }

    // ============================================================
    // 4. NULL PAYMENT AMOUNT
    // ============================================================

    @Test
    void shouldRejectNullPaymentAmount() {

        Invoice invoice = mock(Invoice.class);

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.ISSUED);

        PaymentCreateRequest request =
                validRequest(null);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(
                        tenantId,
                        invoiceId,
                        request
                )
        );

        verifyNoInteractions(paymentRepository);
    }

    // ============================================================
    // 5. ZERO PAYMENT AMOUNT
    // ============================================================

    @Test
    void shouldRejectZeroPaymentAmount() {

        Invoice invoice = mock(Invoice.class);

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.ISSUED);

        PaymentCreateRequest request =
                validRequest(BigDecimal.ZERO);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(
                        tenantId,
                        invoiceId,
                        request
                )
        );

        verifyNoInteractions(paymentRepository);
    }

    // ============================================================
    // 6. NEGATIVE PAYMENT AMOUNT
    // ============================================================

    @Test
    void shouldRejectNegativePaymentAmount() {

        Invoice invoice = mock(Invoice.class);

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.ISSUED);

        PaymentCreateRequest request =
                validRequest(new BigDecimal("-100"));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(
                        tenantId,
                        invoiceId,
                        request
                )
        );

        verifyNoInteractions(paymentRepository);
    }

    // ============================================================
    // 7. OVERPAYMENT
    // ============================================================

    @Test
    void shouldRejectPaymentGreaterThanOutstandingAmount() {

        Invoice invoice = mock(Invoice.class);

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.ISSUED);

        when(invoice.getTotalAmount())
                .thenReturn(new BigDecimal("1000"));

        when(invoice.getPaidAmount())
                .thenReturn(new BigDecimal("700"));

        PaymentCreateRequest request =
                validRequest(new BigDecimal("301"));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(
                        tenantId,
                        invoiceId,
                        request
                )
        );

        verifyNoInteractions(paymentRepository);
    }

    // ============================================================
    // 8. VALID PAYMENT CREATION
    // ============================================================

    @Test
    void shouldCreateValidPendingPayment() {

        Invoice invoice = mock(Invoice.class);

        when(invoice.getId())
                .thenReturn(invoiceId);

        when(invoice.getClinicId())
                .thenReturn(clinicId);

        when(invoice.getPatientId())
                .thenReturn(patientId);

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.ISSUED);

        when(invoice.getTotalAmount())
                .thenReturn(new BigDecimal("1000"));

        when(invoice.getPaidAmount())
                .thenReturn(BigDecimal.ZERO);

        PaymentCreateRequest request =
                validRequest(new BigDecimal("500"));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var response = paymentService.createPayment(
                tenantId,
                invoiceId,
                request
        );

        assertNotNull(response);

        ArgumentCaptor<Payment> captor =
                ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository)
                .save(captor.capture());

        Payment savedPayment =
                captor.getValue();

        assertEquals(
                tenantId,
                savedPayment.getTenantId()
        );

        assertEquals(
                clinicId,
                savedPayment.getClinicId()
        );

        assertEquals(
                patientId,
                savedPayment.getPatientId()
        );

        assertEquals(
                invoiceId,
                savedPayment.getInvoiceId()
        );

        assertEquals(
                new BigDecimal("500"),
                savedPayment.getAmount()
        );

        assertEquals(
                PaymentStatus.PENDING,
                savedPayment.getStatus()
        );

        assertEquals(
                PaymentMethod.UPI,
                savedPayment.getPaymentMethod()
        );
    }

    // ============================================================
    // 9. COMPLETE PAYMENT - PARTIAL
    // ============================================================

    @Test
    void shouldCompletePaymentAndMarkInvoicePartiallyPaid() {

        Payment payment = mock(Payment.class);

        Invoice invoice = new Invoice();

        invoice.setId(invoiceId);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setTotalAmount(new BigDecimal("1000"));
        invoice.setPaidAmount(BigDecimal.ZERO);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(payment.getInvoiceId())
                .thenReturn(invoiceId);

        when(payment.getAmount())
                .thenReturn(new BigDecimal("400"));

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        assertDoesNotThrow(
                () -> paymentService.completePayment(
                        tenantId,
                        paymentId
                )
        );

        verify(payment)
                .setStatus(PaymentStatus.COMPLETED);

        verify(invoiceRepository)
                .save(invoice);

        assertEquals(
                new BigDecimal("400"),
                invoice.getPaidAmount()
        );

        assertEquals(
                InvoiceStatus.PARTIALLY_PAID,
                invoice.getStatus()
        );
    }

    // ============================================================
    // 10. COMPLETE PAYMENT - FULL
    // ============================================================

    @Test
    void shouldCompletePaymentAndMarkInvoicePaid() {

        Payment payment = mock(Payment.class);

        Invoice invoice = new Invoice();

        invoice.setId(invoiceId);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setTotalAmount(new BigDecimal("1000"));
        invoice.setPaidAmount(BigDecimal.ZERO);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(payment.getInvoiceId())
                .thenReturn(invoiceId);

        when(payment.getAmount())
                .thenReturn(new BigDecimal("1000"));

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        assertDoesNotThrow(
                () -> paymentService.completePayment(
                        tenantId,
                        paymentId
                )
        );

        verify(payment)
                .setStatus(PaymentStatus.COMPLETED);

        verify(invoiceRepository)
                .save(invoice);

        assertEquals(
                new BigDecimal("1000"),
                invoice.getPaidAmount()
        );

        assertEquals(
                InvoiceStatus.PAID,
                invoice.getStatus()
        );
    }

    // ============================================================
    // 11. ALREADY COMPLETED PAYMENT
    // ============================================================

    @Test
    void shouldRejectAlreadyCompletedPayment() {

        Payment payment = mock(Payment.class);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.COMPLETED);

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.completePayment(
                        tenantId,
                        paymentId
                )
        );

        verifyNoInteractions(invoiceRepository);

        verify(paymentRepository, never())
                .save(any());
    }

    // ============================================================
    // 12. COMPLETE FAILED PAYMENT
    // ============================================================

    @Test
    void shouldRejectCompletingFailedPayment() {

        Payment payment = mock(Payment.class);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.FAILED);

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.completePayment(
                        tenantId,
                        paymentId
                )
        );

        verifyNoInteractions(invoiceRepository);

        verify(paymentRepository, never())
                .save(any());
    }

    // ============================================================
    // 13. FAIL PENDING PAYMENT
    // ============================================================

    @Test
    void shouldFailPendingPayment() {

        Payment payment = mock(Payment.class);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        assertDoesNotThrow(
                () -> paymentService.failPayment(
                        tenantId,
                        paymentId
                )
        );

        verify(payment)
                .setStatus(PaymentStatus.FAILED);

        verify(paymentRepository)
                .save(payment);
    }

    // ============================================================
    // 14. FAIL NON-PENDING PAYMENT
    // ============================================================

    @Test
    void shouldRejectFailingCompletedPayment() {

        Payment payment = mock(Payment.class);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.COMPLETED);

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.failPayment(
                        tenantId,
                        paymentId
                )
        );

        verify(paymentRepository, never())
                .save(any());
    }

    // ============================================================
    // 15. REFUND COMPLETED PAYMENT
    // ============================================================

    @Test
    void shouldRefundCompletedPayment() {

        Payment payment = mock(Payment.class);

        Invoice invoice = new Invoice();

        invoice.setId(invoiceId);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setTotalAmount(new BigDecimal("1000"));
        invoice.setPaidAmount(new BigDecimal("1000"));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.COMPLETED);

        when(payment.getInvoiceId())
                .thenReturn(invoiceId);

        when(payment.getAmount())
                .thenReturn(new BigDecimal("1000"));

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        assertDoesNotThrow(
                () -> paymentService.refundPayment(
                        tenantId,
                        paymentId
                )
        );

        verify(payment)
                .setStatus(PaymentStatus.REFUNDED);

        verify(paymentRepository)
                .save(payment);

        verify(invoiceRepository)
                .save(invoice);

        assertEquals(
                BigDecimal.ZERO,
                invoice.getPaidAmount()
        );

        assertEquals(
                InvoiceStatus.ISSUED,
                invoice.getStatus()
        );
    }

    // ============================================================
    // 16. REFUND NON-COMPLETED PAYMENT
    // ============================================================

    @Test
    void shouldRejectRefundingPendingPayment() {

        Payment payment = mock(Payment.class);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.refundPayment(
                        tenantId,
                        paymentId
                )
        );

        verifyNoInteractions(invoiceRepository);

        verify(paymentRepository, never())
                .save(any());
    }

    // ============================================================
    // 17. ALREADY REFUNDED PAYMENT
    // ============================================================

    @Test
    void shouldRejectAlreadyRefundedPayment() {

        Payment payment = mock(Payment.class);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.REFUNDED);

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.of(payment));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.refundPayment(
                        tenantId,
                        paymentId
                )
        );

        verifyNoInteractions(invoiceRepository);

        verify(paymentRepository, never())
                .save(any());
    }

    // ============================================================
    // 18. PAYMENT TENANT ISOLATION
    // ============================================================

    @Test
    void shouldRejectPaymentFromAnotherTenant() {

        when(paymentRepository.findByIdAndTenantId(
                paymentId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.getPayment(
                        tenantId,
                        paymentId
                )
        );
    }

    // ============================================================
    // 19. GET INVOICE PAYMENTS
    // ============================================================

    @Test
    void shouldGetInvoicePayments() {

        Invoice invoice = mock(Invoice.class);

        Payment payment = mock(Payment.class);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(paymentRepository
                .findByTenantIdAndInvoiceIdOrderByPaymentDateDesc(
                        tenantId,
                        invoiceId
                ))
                .thenReturn(List.of(payment));

        when(payment.getId())
                .thenReturn(paymentId);

        when(payment.getTenantId())
                .thenReturn(tenantId);

        when(payment.getClinicId())
                .thenReturn(clinicId);

        when(payment.getPatientId())
                .thenReturn(patientId);

        when(payment.getInvoiceId())
                .thenReturn(invoiceId);

        when(payment.getAmount())
                .thenReturn(new BigDecimal("500"));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.PENDING);

        var result =
                paymentService.getInvoicePayments(
                        tenantId,
                        invoiceId
                );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                paymentId,
                result.get(0).getId()
        );

        assertEquals(
                new BigDecimal("500"),
                result.get(0).getAmount()
        );

        verify(paymentRepository)
                .findByTenantIdAndInvoiceIdOrderByPaymentDateDesc(
                        tenantId,
                        invoiceId
                );
    }

    // ============================================================
    // 20. GET PAYMENTS BY STATUS
    // ============================================================

    @Test
    void shouldGetPaymentsByStatus() {

        Payment payment = mock(Payment.class);

        when(paymentRepository
                .findByTenantIdAndStatusOrderByPaymentDateDesc(
                        tenantId,
                        PaymentStatus.COMPLETED
                ))
                .thenReturn(List.of(payment));

        when(payment.getId())
                .thenReturn(paymentId);

        when(payment.getTenantId())
                .thenReturn(tenantId);

        when(payment.getClinicId())
                .thenReturn(clinicId);

        when(payment.getPatientId())
                .thenReturn(patientId);

        when(payment.getInvoiceId())
                .thenReturn(invoiceId);

        when(payment.getAmount())
                .thenReturn(new BigDecimal("500"));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.COMPLETED);

        var result =
                paymentService.getPaymentsByStatus(
                        tenantId,
                        PaymentStatus.COMPLETED
                );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                PaymentStatus.COMPLETED,
                result.get(0).getStatus()
        );

        verify(paymentRepository)
                .findByTenantIdAndStatusOrderByPaymentDateDesc(
                        tenantId,
                        PaymentStatus.COMPLETED
                );
    }
}
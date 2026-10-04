package com.ayurclinic.billing.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.billing.dto.PaymentCreateRequest;
import com.ayurclinic.billing.dto.PaymentResponse;
import com.ayurclinic.billing.enums.PaymentStatus;
import com.ayurclinic.billing.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private CustomUserPrincipal principal;

    @InjectMocks
    private PaymentController paymentController;

    private UUID tenantId;
    private UUID invoiceId;
    private UUID paymentId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        when(principal.getTenantId())
                .thenReturn(tenantId);
    }

    // ============================================================
    // 1. CREATE PAYMENT
    // ============================================================

    @Test
    void shouldCreatePayment() {

        PaymentCreateRequest request =
                new PaymentCreateRequest();

        PaymentResponse response =
                mock(PaymentResponse.class);

        when(paymentService.createPayment(
                tenantId,
                invoiceId,
                request
        )).thenReturn(response);

        ResponseEntity<PaymentResponse> result =
                paymentController.createPayment(
                        principal,
                        invoiceId,
                        request
                );

        assertEquals(
                HttpStatus.CREATED,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(paymentService)
                .createPayment(
                        tenantId,
                        invoiceId,
                        request
                );
    }

    // ============================================================
    // 2. GET PAYMENT
    // ============================================================

    @Test
    void shouldGetPayment() {

        PaymentResponse response =
                mock(PaymentResponse.class);

        when(paymentService.getPayment(
                tenantId,
                paymentId
        )).thenReturn(response);

        ResponseEntity<PaymentResponse> result =
                paymentController.getPayment(
                        principal,
                        paymentId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(paymentService)
                .getPayment(
                        tenantId,
                        paymentId
                );
    }

    // ============================================================
    // 3. GET INVOICE PAYMENTS
    // ============================================================

    @Test
    void shouldGetInvoicePayments() {

        List<PaymentResponse> payments =
                List.of(
                        mock(PaymentResponse.class),
                        mock(PaymentResponse.class)
                );

        when(paymentService.getInvoicePayments(
                tenantId,
                invoiceId
        )).thenReturn(payments);

        ResponseEntity<List<PaymentResponse>> result =
                paymentController.getInvoicePayments(
                        principal,
                        invoiceId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                payments,
                result.getBody()
        );

        verify(paymentService)
                .getInvoicePayments(
                        tenantId,
                        invoiceId
                );
    }

    // ============================================================
    // 4. GET PATIENT PAYMENTS
    // ============================================================

    @Test
    void shouldGetPatientPayments() {

        List<PaymentResponse> payments =
                List.of(mock(PaymentResponse.class));

        when(paymentService.getPatientPayments(
                tenantId,
                patientId
        )).thenReturn(payments);

        ResponseEntity<List<PaymentResponse>> result =
                paymentController.getPatientPayments(
                        principal,
                        patientId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                payments,
                result.getBody()
        );

        verify(paymentService)
                .getPatientPayments(
                        tenantId,
                        patientId
                );
    }

    // ============================================================
    // 5. GET PAYMENTS BY STATUS
    // ============================================================

    @Test
    void shouldGetPaymentsByStatus() {

        PaymentStatus status =
                PaymentStatus.COMPLETED;

        List<PaymentResponse> payments =
                List.of(mock(PaymentResponse.class));

        when(paymentService.getPaymentsByStatus(
                tenantId,
                status
        )).thenReturn(payments);

        ResponseEntity<List<PaymentResponse>> result =
                paymentController.getPaymentsByStatus(
                        principal,
                        status
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                payments,
                result.getBody()
        );

        verify(paymentService)
                .getPaymentsByStatus(
                        tenantId,
                        status
                );
    }

    // ============================================================
    // 6. COMPLETE PAYMENT
    // ============================================================

    @Test
    void shouldCompletePayment() {

        PaymentResponse response =
                mock(PaymentResponse.class);

        when(paymentService.completePayment(
                tenantId,
                paymentId
        )).thenReturn(response);

        ResponseEntity<PaymentResponse> result =
                paymentController.completePayment(
                        principal,
                        paymentId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(paymentService)
                .completePayment(
                        tenantId,
                        paymentId
                );
    }

    // ============================================================
    // 7. FAIL PAYMENT
    // ============================================================

    @Test
    void shouldFailPayment() {

        PaymentResponse response =
                mock(PaymentResponse.class);

        when(paymentService.failPayment(
                tenantId,
                paymentId
        )).thenReturn(response);

        ResponseEntity<PaymentResponse> result =
                paymentController.failPayment(
                        principal,
                        paymentId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(paymentService)
                .failPayment(
                        tenantId,
                        paymentId
                );
    }

    // ============================================================
    // 8. REFUND PAYMENT
    // ============================================================

    @Test
    void shouldRefundPayment() {

        PaymentResponse response =
                mock(PaymentResponse.class);

        when(paymentService.refundPayment(
                tenantId,
                paymentId
        )).thenReturn(response);

        ResponseEntity<PaymentResponse> result =
                paymentController.refundPayment(
                        principal,
                        paymentId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(paymentService)
                .refundPayment(
                        tenantId,
                        paymentId
                );
    }

    // ============================================================
    // 9. TENANT ID MUST COME FROM PRINCIPAL
    // ============================================================

    @Test
    void shouldUseTenantIdFromAuthenticatedPrincipal() {

        UUID authenticatedTenantId =
                UUID.randomUUID();

        when(principal.getTenantId())
                .thenReturn(authenticatedTenantId);

        PaymentResponse response =
                mock(PaymentResponse.class);

        when(paymentService.getPayment(
                authenticatedTenantId,
                paymentId
        )).thenReturn(response);

        ResponseEntity<PaymentResponse> result =
                paymentController.getPayment(
                        principal,
                        paymentId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(paymentService)
                .getPayment(
                        authenticatedTenantId,
                        paymentId
                );

        verify(paymentService, never())
                .getPayment(
                        tenantId,
                        paymentId
                );
    }

    // ============================================================
    // 10. SERVICE EXCEPTION PROPAGATION
    // ============================================================

    @Test
    void shouldPropagatePaymentServiceException() {

        when(paymentService.getPayment(
                tenantId,
                paymentId
        )).thenThrow(
                new IllegalArgumentException(
                        "Payment not found"
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentController.getPayment(
                                principal,
                                paymentId
                        )
                );

        assertEquals(
                "Payment not found",
                exception.getMessage()
        );

        verify(paymentService)
                .getPayment(
                        tenantId,
                        paymentId
                );
    }
}
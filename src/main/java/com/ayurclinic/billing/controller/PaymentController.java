package com.ayurclinic.billing.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.billing.dto.PaymentCreateRequest;
import com.ayurclinic.billing.dto.PaymentResponse;
import com.ayurclinic.billing.enums.PaymentStatus;
import com.ayurclinic.billing.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    /**
     * Create a payment for an invoice.
     */
    @PostMapping("/invoices/{invoiceId}/payments")
    public ResponseEntity<PaymentResponse> createPayment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID invoiceId,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        PaymentResponse response =
                paymentService.createPayment(
                        tenantId,
                        invoiceId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get payment by ID.
     */
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                paymentService.getPayment(
                        tenantId,
                        id
                )
        );
    }

    /**
     * Get all payments for an invoice.
     */
    @GetMapping("/invoices/{invoiceId}/payments")
    public ResponseEntity<List<PaymentResponse>> getInvoicePayments(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID invoiceId
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                paymentService.getInvoicePayments(
                        tenantId,
                        invoiceId
                )
        );
    }

    /**
     * Get all payments for a patient.
     */
    @GetMapping("/payments/patient/{patientId}")
    public ResponseEntity<List<PaymentResponse>> getPatientPayments(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID patientId
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                paymentService.getPatientPayments(
                        tenantId,
                        patientId
                )
        );
    }

    /**
     * Get payments by status.
     */
    @GetMapping("/payments/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable PaymentStatus status
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                paymentService.getPaymentsByStatus(
                        tenantId,
                        status
                )
        );
    }

    /**
     * Complete a pending payment.
     */
    @PatchMapping("/payments/{id}/complete")
    public ResponseEntity<PaymentResponse> completePayment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                paymentService.completePayment(
                        tenantId,
                        id
                )
        );
    }

    /**
     * Mark a pending payment as failed.
     */
    @PatchMapping("/payments/{id}/fail")
    public ResponseEntity<PaymentResponse> failPayment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                paymentService.failPayment(
                        tenantId,
                        id
                )
        );
    }

    /**
     * Refund a completed payment.
     */
    @PatchMapping("/payments/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                paymentService.refundPayment(
                        tenantId,
                        id
                )
        );
    }
}
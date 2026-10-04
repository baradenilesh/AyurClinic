package com.ayurclinic.billing.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.billing.dto.InvoiceCreateRequest;
import com.ayurclinic.billing.dto.InvoiceResponse;
import com.ayurclinic.billing.dto.InvoiceUpdateRequest;
import com.ayurclinic.billing.enums.InvoiceStatus;
import com.ayurclinic.billing.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(
            InvoiceService invoiceService
    ) {
        this.invoiceService = invoiceService;
    }

    /**
     * Create a new invoice.
     */
    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody InvoiceCreateRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        InvoiceResponse response =
                invoiceService.createInvoice(
                        tenantId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get invoice by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.getInvoice(
                        tenantId,
                        id
                )
        );
    }

    /**
     * Get all invoices for the authenticated tenant.
     */
    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.getAllInvoices(tenantId)
        );
    }

    /**
     * Get invoices for a patient.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<InvoiceResponse>> getPatientInvoices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID patientId
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.getPatientInvoices(
                        tenantId,
                        patientId
                )
        );
    }

    /**
     * Get invoices for an appointment.
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<InvoiceResponse>> getAppointmentInvoices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID appointmentId
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.getAppointmentInvoices(
                        tenantId,
                        appointmentId
                )
        );
    }

    /**
     * Get invoices for a consultation.
     */
    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<List<InvoiceResponse>> getConsultationInvoices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID consultationId
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.getConsultationInvoices(
                        tenantId,
                        consultationId
                )
        );
    }

    /**
     * Get invoices by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable InvoiceStatus status
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.getInvoicesByStatus(
                        tenantId,
                        status
                )
        );
    }

    /**
     * Update a draft invoice.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceUpdateRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.updateInvoice(
                        tenantId,
                        id,
                        request
                )
        );
    }

    /**
     * Issue a draft invoice.
     */
    @PatchMapping("/{id}/issue")
    public ResponseEntity<InvoiceResponse> issueInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.issueInvoice(
                        tenantId,
                        id
                )
        );
    }

    /**
     * Cancel an invoice.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponse> cancelInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return ResponseEntity.ok(
                invoiceService.cancelInvoice(
                        tenantId,
                        id
                )
        );
    }
}
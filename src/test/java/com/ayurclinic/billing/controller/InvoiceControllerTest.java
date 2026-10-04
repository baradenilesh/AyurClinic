package com.ayurclinic.billing.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.billing.dto.InvoiceCreateRequest;
import com.ayurclinic.billing.dto.InvoiceResponse;
import com.ayurclinic.billing.dto.InvoiceUpdateRequest;
import com.ayurclinic.billing.enums.InvoiceStatus;
import com.ayurclinic.billing.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private CustomUserPrincipal principal;

    @InjectMocks
    private InvoiceController invoiceController;

    private UUID tenantId;
    private UUID invoiceId;
    private UUID patientId;
    private UUID appointmentId;
    private UUID consultationId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        consultationId = UUID.randomUUID();

        when(principal.getTenantId()).thenReturn(tenantId);
    }

    // ============================================================
    // 1. CREATE INVOICE
    // ============================================================

    @Test
    void shouldCreateInvoice() {

        InvoiceCreateRequest request =
                new InvoiceCreateRequest();

        InvoiceResponse response =
                mock(InvoiceResponse.class);

        when(invoiceService.createInvoice(
                tenantId,
                request
        )).thenReturn(response);

        ResponseEntity<InvoiceResponse> result =
                invoiceController.createInvoice(
                        principal,
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

        verify(invoiceService)
                .createInvoice(
                        tenantId,
                        request
                );
    }

    // ============================================================
    // 2. GET INVOICE
    // ============================================================

    @Test
    void shouldGetInvoice() {

        InvoiceResponse response =
                mock(InvoiceResponse.class);

        when(invoiceService.getInvoice(
                tenantId,
                invoiceId
        )).thenReturn(response);

        ResponseEntity<InvoiceResponse> result =
                invoiceController.getInvoice(
                        principal,
                        invoiceId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(invoiceService)
                .getInvoice(
                        tenantId,
                        invoiceId
                );
    }

    // ============================================================
    // 3. GET ALL INVOICES
    // ============================================================

    @Test
    void shouldGetAllInvoices() {

        List<InvoiceResponse> invoices =
                List.of(
                        mock(InvoiceResponse.class),
                        mock(InvoiceResponse.class)
                );

        when(invoiceService.getAllInvoices(
                tenantId
        )).thenReturn(invoices);

        ResponseEntity<List<InvoiceResponse>> result =
                invoiceController.getInvoices(
                        principal
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                invoices,
                result.getBody()
        );

        verify(invoiceService)
                .getAllInvoices(tenantId);
    }

    // ============================================================
    // 4. GET PATIENT INVOICES
    // ============================================================

    @Test
    void shouldGetPatientInvoices() {

        List<InvoiceResponse> invoices =
                List.of(mock(InvoiceResponse.class));

        when(invoiceService.getPatientInvoices(
                tenantId,
                patientId
        )).thenReturn(invoices);

        ResponseEntity<List<InvoiceResponse>> result =
                invoiceController.getPatientInvoices(
                        principal,
                        patientId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                invoices,
                result.getBody()
        );

        verify(invoiceService)
                .getPatientInvoices(
                        tenantId,
                        patientId
                );
    }

    // ============================================================
    // 5. GET APPOINTMENT INVOICES
    // ============================================================

    @Test
    void shouldGetAppointmentInvoices() {

        List<InvoiceResponse> invoices =
                List.of(mock(InvoiceResponse.class));

        when(invoiceService.getAppointmentInvoices(
                tenantId,
                appointmentId
        )).thenReturn(invoices);

        ResponseEntity<List<InvoiceResponse>> result =
                invoiceController.getAppointmentInvoices(
                        principal,
                        appointmentId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                invoices,
                result.getBody()
        );

        verify(invoiceService)
                .getAppointmentInvoices(
                        tenantId,
                        appointmentId
                );
    }

    // ============================================================
    // 6. GET CONSULTATION INVOICES
    // ============================================================

    @Test
    void shouldGetConsultationInvoices() {

        List<InvoiceResponse> invoices =
                List.of(mock(InvoiceResponse.class));

        when(invoiceService.getConsultationInvoices(
                tenantId,
                consultationId
        )).thenReturn(invoices);

        ResponseEntity<List<InvoiceResponse>> result =
                invoiceController.getConsultationInvoices(
                        principal,
                        consultationId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                invoices,
                result.getBody()
        );

        verify(invoiceService)
                .getConsultationInvoices(
                        tenantId,
                        consultationId
                );
    }

    // ============================================================
    // 7. GET INVOICES BY STATUS
    // ============================================================

    @Test
    void shouldGetInvoicesByStatus() {

        InvoiceStatus status =
                InvoiceStatus.ISSUED;

        List<InvoiceResponse> invoices =
                List.of(mock(InvoiceResponse.class));

        when(invoiceService.getInvoicesByStatus(
                tenantId,
                status
        )).thenReturn(invoices);

        ResponseEntity<List<InvoiceResponse>> result =
                invoiceController.getInvoicesByStatus(
                        principal,
                        status
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                invoices,
                result.getBody()
        );

        verify(invoiceService)
                .getInvoicesByStatus(
                        tenantId,
                        status
                );
    }

    // ============================================================
    // 8. UPDATE INVOICE
    // ============================================================

    @Test
    void shouldUpdateInvoice() {

        InvoiceUpdateRequest request =
                new InvoiceUpdateRequest();

        InvoiceResponse response =
                mock(InvoiceResponse.class);

        when(invoiceService.updateInvoice(
                tenantId,
                invoiceId,
                request
        )).thenReturn(response);

        ResponseEntity<InvoiceResponse> result =
                invoiceController.updateInvoice(
                        principal,
                        invoiceId,
                        request
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(invoiceService)
                .updateInvoice(
                        tenantId,
                        invoiceId,
                        request
                );
    }

    // ============================================================
    // 9. ISSUE INVOICE
    // ============================================================

    @Test
    void shouldIssueInvoice() {

        InvoiceResponse response =
                mock(InvoiceResponse.class);

        when(invoiceService.issueInvoice(
                tenantId,
                invoiceId
        )).thenReturn(response);

        ResponseEntity<InvoiceResponse> result =
                invoiceController.issueInvoice(
                        principal,
                        invoiceId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(invoiceService)
                .issueInvoice(
                        tenantId,
                        invoiceId
                );
    }

    // ============================================================
    // 10. CANCEL INVOICE
    // ============================================================

    @Test
    void shouldCancelInvoice() {

        InvoiceResponse response =
                mock(InvoiceResponse.class);

        when(invoiceService.cancelInvoice(
                tenantId,
                invoiceId
        )).thenReturn(response);

        ResponseEntity<InvoiceResponse> result =
                invoiceController.cancelInvoice(
                        principal,
                        invoiceId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(invoiceService)
                .cancelInvoice(
                        tenantId,
                        invoiceId
                );
    }

    // ============================================================
    // 11. TENANT ID MUST COME FROM PRINCIPAL
    // ============================================================

    @Test
    void shouldUseTenantIdFromAuthenticatedPrincipal() {

        UUID authenticatedTenantId =
                UUID.randomUUID();

        when(principal.getTenantId())
                .thenReturn(authenticatedTenantId);

        InvoiceResponse response =
                mock(InvoiceResponse.class);

        when(invoiceService.getInvoice(
                authenticatedTenantId,
                invoiceId
        )).thenReturn(response);

        ResponseEntity<InvoiceResponse> result =
                invoiceController.getInvoice(
                        principal,
                        invoiceId
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertSame(
                response,
                result.getBody()
        );

        verify(invoiceService)
                .getInvoice(
                        authenticatedTenantId,
                        invoiceId
                );

        verify(invoiceService, never())
                .getInvoice(
                        tenantId,
                        invoiceId
                );
    }

    // ============================================================
    // 12. SERVICE EXCEPTION PROPAGATION
    // ============================================================

    @Test
    void shouldPropagateInvoiceServiceException() {

        when(invoiceService.getInvoice(
                tenantId,
                invoiceId
        )).thenThrow(
                new IllegalArgumentException(
                        "Invoice not found"
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> invoiceController.getInvoice(
                                principal,
                                invoiceId
                        )
                );

        assertEquals(
                "Invoice not found",
                exception.getMessage()
        );

        verify(invoiceService)
                .getInvoice(
                        tenantId,
                        invoiceId
                );
    }
}
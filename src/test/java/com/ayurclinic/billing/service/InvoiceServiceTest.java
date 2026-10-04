package com.ayurclinic.billing.service;

import com.ayurclinic.appointment.entity.Appointment;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.billing.dto.InvoiceCreateRequest;
import com.ayurclinic.billing.dto.InvoiceUpdateRequest;
import com.ayurclinic.billing.entity.Invoice;
import com.ayurclinic.billing.enums.InvoiceStatus;
import com.ayurclinic.billing.repository.InvoiceRepository;
import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private UUID tenantId;
    private UUID clinicId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        patientId = UUID.randomUUID();
    }

    // ============================================================
    // 1. TENANT ISOLATION - CLINIC
    // ============================================================

    @Test
    void shouldRejectClinicFromAnotherTenant() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(clinicRepository)
                .findByIdAndTenantId(
                        clinicId,
                        tenantId
                );

        verifyNoInteractions(
                patientRepository,
                appointmentRepository,
                consultationRepository,
                invoiceRepository
        );
    }

    // ============================================================
    // 2. TENANT ISOLATION - PATIENT
    // ============================================================

    @Test
    void shouldRejectPatientFromAnotherTenant() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);

        Clinic clinic = mock(Clinic.class);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(patientRepository)
                .findByIdAndTenantId(
                        patientId,
                        tenantId
                );

        verifyNoInteractions(
                appointmentRepository,
                consultationRepository,
                invoiceRepository
        );
    }

    // ============================================================
    // 3. TENANT ISOLATION - APPOINTMENT
    // ============================================================

    @Test
    void shouldRejectAppointmentFromAnotherTenant() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        UUID appointmentId = UUID.randomUUID();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setAppointmentId(appointmentId);

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        Appointment appointment = mock(Appointment.class);

        UUID anotherTenantId = UUID.randomUUID();

        when(appointment.getTenantId())
                .thenReturn(anotherTenantId);

        when(appointmentRepository.findById(
                appointmentId
        )).thenReturn(Optional.of(appointment));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(appointmentRepository)
                .findById(appointmentId);

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 4. APPOINTMENT - WRONG CLINIC
    // ============================================================

    @Test
    void shouldRejectAppointmentFromWrongClinic() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        UUID appointmentId = UUID.randomUUID();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setAppointmentId(appointmentId);

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);
        Appointment appointment = mock(Appointment.class);

        UUID anotherClinicId = UUID.randomUUID();

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(appointment.getTenantId())
                .thenReturn(tenantId);

        when(appointment.getClinicId())
                .thenReturn(anotherClinicId);

        when(appointmentRepository.findById(
                appointmentId
        )).thenReturn(Optional.of(appointment));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(appointmentRepository)
                .findById(appointmentId);

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 5. APPOINTMENT - WRONG PATIENT
    // ============================================================

    @Test
    void shouldRejectAppointmentFromWrongPatient() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        UUID appointmentId = UUID.randomUUID();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setAppointmentId(appointmentId);

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);
        Appointment appointment = mock(Appointment.class);

        UUID anotherPatientId = UUID.randomUUID();

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(appointment.getTenantId())
                .thenReturn(tenantId);

        when(appointment.getClinicId())
                .thenReturn(clinicId);

        when(appointment.getPatientId())
                .thenReturn(anotherPatientId);

        when(appointmentRepository.findById(
                appointmentId
        )).thenReturn(Optional.of(appointment));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(appointmentRepository)
                .findById(appointmentId);

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 6. CONSULTATION - ANOTHER TENANT
    // ============================================================

    @Test
    void shouldRejectConsultationFromAnotherTenant() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        UUID consultationId = UUID.randomUUID();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setConsultationId(consultationId);

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(consultationRepository.findByIdAndTenantId(
                consultationId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(consultationRepository)
                .findByIdAndTenantId(
                        consultationId,
                        tenantId
                );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 7. CONSULTATION - WRONG CLINIC
    // ============================================================

    @Test
    void shouldRejectConsultationFromWrongClinic() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        UUID consultationId = UUID.randomUUID();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setConsultationId(consultationId);

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);

        UUID anotherClinicId = UUID.randomUUID();

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(consultationRepository.findByIdAndTenantId(
                consultationId,
                tenantId
        )).thenReturn(Optional.of(consultation));

        when(consultation.getClinicId())
                .thenReturn(anotherClinicId);


        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 8. CONSULTATION - WRONG PATIENT
    // ============================================================

    @Test
    void shouldRejectConsultationFromWrongPatient() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        UUID consultationId = UUID.randomUUID();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setConsultationId(consultationId);

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);

        UUID anotherPatientId = UUID.randomUUID();

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(consultationRepository.findByIdAndTenantId(
                consultationId,
                tenantId
        )).thenReturn(Optional.of(consultation));

        when(consultation.getClinicId())
                .thenReturn(clinicId);

        when(consultation.getPatientId())
                .thenReturn(anotherPatientId);

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 9. NEGATIVE SUBTOTAL
    // ============================================================

    @Test
    void shouldRejectNegativeSubtotal() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setSubtotal(new BigDecimal("-100"));

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 10. NEGATIVE DISCOUNT
    // ============================================================

    @Test
    void shouldRejectNegativeDiscount() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setSubtotal(new BigDecimal("1000"));
        request.setDiscount(new BigDecimal("-100"));

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 11. DISCOUNT GREATER THAN SUBTOTAL
    // ============================================================

    @Test
    void shouldRejectDiscountGreaterThanSubtotal() {

        InvoiceCreateRequest request = new InvoiceCreateRequest();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setSubtotal(new BigDecimal("1000"));
        request.setDiscount(new BigDecimal("1200"));

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);

        when(clinic.getId()).thenReturn(clinicId);
        when(patient.getId()).thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 12. INVOICE NOT FOUND
    // ============================================================

    @Test
    void shouldRejectInvoiceFromAnotherTenantWhenUpdating() {

        UUID invoiceId = UUID.randomUUID();

        InvoiceUpdateRequest request =
                new InvoiceUpdateRequest();

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.updateInvoice(
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
    }

    // ============================================================
    // 13. ISSUE NON-DRAFT INVOICE
    // ============================================================

    @Test
    void shouldRejectIssuingNonDraftInvoice() {

        UUID invoiceId = UUID.randomUUID();

        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.ISSUED);

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.issueInvoice(
                        tenantId,
                        invoiceId
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 14. CANCEL PAID INVOICE
    // ============================================================

    @Test
    void shouldRejectCancellingPaidInvoice() {

        UUID invoiceId = UUID.randomUUID();

        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.PAID);

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.cancelInvoice(
                        tenantId,
                        invoiceId
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 15. CANCEL ALREADY CANCELLED INVOICE
    // ============================================================

    @Test
    void shouldRejectCancellingAlreadyCancelledInvoice() {

        UUID invoiceId = UUID.randomUUID();

        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.CANCELLED);

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.cancelInvoice(
                        tenantId,
                        invoiceId
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 16. CREATE INVOICE WITH VALID CLINIC/PATIENT
    // ============================================================

    @Test
    void shouldCreateInvoiceWithValidClinicAndPatient() {

        InvoiceCreateRequest request =
                new InvoiceCreateRequest();

        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setSubtotal(new BigDecimal("1000"));
        request.setDiscount(new BigDecimal("100"));
        request.setTax(new BigDecimal("90"));

        Clinic clinic = mock(Clinic.class);
        Patient patient = mock(Patient.class);

        when(clinic.getId())
                .thenReturn(clinicId);

        when(patient.getId())
                .thenReturn(patientId);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(invoiceRepository.findByTenantIdAndInvoiceNumber(
                eq(tenantId),
                anyString()
        )).thenReturn(Optional.empty());

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(
                () -> invoiceService.createInvoice(
                        tenantId,
                        request
                )
        );

        verify(invoiceRepository)
                .save(any(Invoice.class));
    }

    // ============================================================
    // 17. ISSUE DRAFT INVOICE WITH POSITIVE TOTAL
    // ============================================================

    @Test
    void shouldIssueDraftInvoice() {

        UUID invoiceId = UUID.randomUUID();

        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.DRAFT);

        when(invoice.getTotalAmount())
                .thenReturn(new BigDecimal("1000"));

        when(invoiceRepository.save(invoice))
                .thenReturn(invoice);

        assertDoesNotThrow(
                () -> invoiceService.issueInvoice(
                        tenantId,
                        invoiceId
                )
        );

        verify(invoice)
                .setStatus(InvoiceStatus.ISSUED);

        verify(invoiceRepository)
                .save(invoice);
    }

    // ============================================================
    // 18. REJECT ISSUING ZERO TOTAL INVOICE
    // ============================================================

    @Test
    void shouldRejectIssuingZeroTotalInvoice() {

        UUID invoiceId = UUID.randomUUID();

        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.DRAFT);

        when(invoice.getTotalAmount())
                .thenReturn(BigDecimal.ZERO);

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.issueInvoice(
                        tenantId,
                        invoiceId
                )
        );

        verify(invoiceRepository, never())
                .save(any());
    }

    // ============================================================
    // 19. CANCEL ISSUED INVOICE
    // ============================================================

    @Test
    void shouldCancelIssuedInvoice() {

        UUID invoiceId = UUID.randomUUID();

        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findByIdAndTenantId(
                invoiceId,
                tenantId
        )).thenReturn(Optional.of(invoice));

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.ISSUED);

        when(invoiceRepository.save(invoice))
                .thenReturn(invoice);

        assertDoesNotThrow(
                () -> invoiceService.cancelInvoice(
                        tenantId,
                        invoiceId
                )
        );

        verify(invoice)
                .setStatus(InvoiceStatus.CANCELLED);

        verify(invoiceRepository)
                .save(invoice);
    }
}
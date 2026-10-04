package com.ayurclinic.consultation.service;

import com.ayurclinic.appointment.entity.Appointment;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.consultation.dto.ConsultationCreateRequest;
import com.ayurclinic.consultation.dto.ConsultationResponse;
import com.ayurclinic.consultation.dto.ConsultationUpdateRequest;
import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.enums.ConsultationStatus;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private ConsultationService consultationService;

    private UUID tenantId;
    private UUID anotherTenantId;
    private UUID clinicId;
    private UUID patientId;
    private UUID doctorId;
    private UUID appointmentId;
    private UUID consultationId;

    private Appointment appointment;
    private Consultation consultation;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        anotherTenantId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        consultationId = UUID.randomUUID();

        appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setTenantId(tenantId);
        appointment.setClinicId(clinicId);
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentDate(LocalDate.of(2026, 10, 2));

        consultation = new Consultation();
        consultation.setId(consultationId);
        consultation.setTenantId(tenantId);
        consultation.setClinicId(clinicId);
        consultation.setAppointmentId(appointmentId);
        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorId);
        consultation.setConsultationDate(LocalDate.of(2026, 10, 2));
        consultation.setChiefComplaint("Headache");
        consultation.setSymptoms("Headache and fatigue");
        consultation.setClinicalFindings("Mild fatigue");
        consultation.setDiagnosis("Stress-related headache");
        consultation.setTreatmentPlan("Ayurvedic treatment");
        consultation.setDoctorNotes("Follow up after 7 days");
        consultation.setStatus(ConsultationStatus.IN_PROGRESS.name());
    }

    // -------------------------------------------------------------------------
    // CREATE CONSULTATION
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateConsultation() {
        ConsultationCreateRequest request = new ConsultationCreateRequest();
        request.setAppointmentId(appointmentId);
        request.setChiefComplaint("Headache");
        request.setSymptoms("Headache and fatigue");
        request.setClinicalFindings("Mild fatigue");
        request.setDiagnosis("Stress-related headache");
        request.setTreatmentPlan("Ayurvedic treatment");
        request.setDoctorNotes("Follow up after 7 days");

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(consultationRepository.existsByAppointmentIdAndTenantId(
                appointmentId, tenantId))
                .thenReturn(false);

        when(consultationRepository.save(any(Consultation.class)))
                .thenAnswer(invocation -> {
                    Consultation saved = invocation.getArgument(0);
                    saved.setId(consultationId);
                    return saved;
                });

        ConsultationResponse response =
                consultationService.createConsultation(tenantId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(consultationId);
        assertThat(response.getTenantId()).isEqualTo(tenantId);
        assertThat(response.getClinicId()).isEqualTo(clinicId);
        assertThat(response.getAppointmentId()).isEqualTo(appointmentId);
        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getDoctorId()).isEqualTo(doctorId);
        assertThat(response.getConsultationDate())
                .isEqualTo(appointment.getAppointmentDate());
        assertThat(response.getChiefComplaint()).isEqualTo("Headache");
        assertThat(response.getStatus())
                .isEqualTo(ConsultationStatus.IN_PROGRESS.name());

        verify(appointmentRepository).findById(appointmentId);
        verify(consultationRepository)
                .existsByAppointmentIdAndTenantId(appointmentId, tenantId);
        verify(consultationRepository).save(any(Consultation.class));
    }

    @Test
    void shouldRejectNullTenant() {
        ConsultationCreateRequest request = new ConsultationCreateRequest();
        request.setAppointmentId(appointmentId);

        assertThatThrownBy(() ->
                consultationService.createConsultation(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID is required");

        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(consultationRepository);
    }

    @Test
    void shouldRejectNullRequest() {
        assertThatThrownBy(() ->
                consultationService.createConsultation(tenantId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Consultation request cannot be null");

        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(consultationRepository);
    }

    @Test
    void shouldRejectMissingAppointmentId() {
        ConsultationCreateRequest request = new ConsultationCreateRequest();

        assertThatThrownBy(() ->
                consultationService.createConsultation(tenantId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Appointment ID is required");

        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(consultationRepository);
    }

    @Test
    void shouldRejectAppointmentNotFound() {
        ConsultationCreateRequest request = new ConsultationCreateRequest();
        request.setAppointmentId(appointmentId);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                consultationService.createConsultation(tenantId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Appointment not found");

        verify(appointmentRepository).findById(appointmentId);
        verifyNoInteractions(consultationRepository);
    }

    @Test
    void shouldRejectCrossTenantAppointment() {
        ConsultationCreateRequest request = new ConsultationCreateRequest();
        request.setAppointmentId(appointmentId);

        appointment.setTenantId(anotherTenantId);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        assertThatThrownBy(() ->
                consultationService.createConsultation(tenantId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Appointment not found");

        verify(appointmentRepository).findById(appointmentId);
        verifyNoInteractions(consultationRepository);
    }

    @Test
    void shouldRejectDuplicateConsultation() {
        ConsultationCreateRequest request = new ConsultationCreateRequest();
        request.setAppointmentId(appointmentId);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(consultationRepository.existsByAppointmentIdAndTenantId(
                appointmentId, tenantId))
                .thenReturn(true);

        assertThatThrownBy(() ->
                consultationService.createConsultation(tenantId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Consultation already exists for this appointment");

        verify(appointmentRepository).findById(appointmentId);
        verify(consultationRepository)
                .existsByAppointmentIdAndTenantId(appointmentId, tenantId);
        verify(consultationRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldGetConsultationById() {
        when(consultationRepository.findByIdAndTenantId(
                consultationId, tenantId))
                .thenReturn(Optional.of(consultation));

        ConsultationResponse response =
                consultationService.getConsultationById(
                        tenantId, consultationId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(consultationId);
        assertThat(response.getTenantId()).isEqualTo(tenantId);
        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getDoctorId()).isEqualTo(doctorId);

        verify(consultationRepository)
                .findByIdAndTenantId(consultationId, tenantId);
    }

    @Test
    void shouldRejectConsultationFromAnotherTenant() {
        when(consultationRepository.findByIdAndTenantId(
                consultationId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                consultationService.getConsultationById(
                        tenantId, consultationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Consultation not found");

        verify(consultationRepository)
                .findByIdAndTenantId(consultationId, tenantId);
    }

    // -------------------------------------------------------------------------
    // GET BY APPOINTMENT
    // -------------------------------------------------------------------------

    @Test
    void shouldGetConsultationByAppointment() {
        when(consultationRepository.findByAppointmentIdAndTenantId(
                appointmentId, tenantId))
                .thenReturn(Optional.of(consultation));

        ConsultationResponse response =
                consultationService.getByAppointmentId(
                        tenantId, appointmentId);

        assertThat(response).isNotNull();
        assertThat(response.getAppointmentId()).isEqualTo(appointmentId);
        assertThat(response.getId()).isEqualTo(consultationId);

        verify(consultationRepository)
                .findByAppointmentIdAndTenantId(appointmentId, tenantId);
    }

    @Test
    void shouldRejectConsultationNotFoundByAppointment() {
        when(consultationRepository.findByAppointmentIdAndTenantId(
                appointmentId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                consultationService.getByAppointmentId(
                        tenantId, appointmentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Consultation not found");

        verify(consultationRepository)
                .findByAppointmentIdAndTenantId(appointmentId, tenantId);
    }

    // -------------------------------------------------------------------------
    // PATIENT HISTORY
    // -------------------------------------------------------------------------

    @Test
    void shouldGetPatientConsultations() {
        when(consultationRepository
                .findByTenantIdAndPatientIdOrderByConsultationDateDesc(
                        tenantId, patientId))
                .thenReturn(List.of(consultation));

        List<ConsultationResponse> result =
                consultationService.getPatientConsultations(
                        tenantId, patientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(consultationId);
        assertThat(result.get(0).getPatientId()).isEqualTo(patientId);

        verify(consultationRepository)
                .findByTenantIdAndPatientIdOrderByConsultationDateDesc(
                        tenantId, patientId);
    }

    // -------------------------------------------------------------------------
    // DOCTOR HISTORY
    // -------------------------------------------------------------------------

    @Test
    void shouldGetDoctorConsultations() {
        when(consultationRepository
                .findByTenantIdAndDoctorIdOrderByConsultationDateDesc(
                        tenantId, doctorId))
                .thenReturn(List.of(consultation));

        List<ConsultationResponse> result =
                consultationService.getDoctorConsultations(
                        tenantId, doctorId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(consultationId);
        assertThat(result.get(0).getDoctorId()).isEqualTo(doctorId);

        verify(consultationRepository)
                .findByTenantIdAndDoctorIdOrderByConsultationDateDesc(
                        tenantId, doctorId);
    }

    // -------------------------------------------------------------------------
    // CLINIC HISTORY
    // -------------------------------------------------------------------------

    @Test
    void shouldGetClinicConsultations() {
        when(consultationRepository
                .findByTenantIdAndClinicIdOrderByConsultationDateDesc(
                        tenantId, clinicId))
                .thenReturn(List.of(consultation));

        List<ConsultationResponse> result =
                consultationService.getClinicConsultations(
                        tenantId, clinicId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(consultationId);
        assertThat(result.get(0).getClinicId()).isEqualTo(clinicId);

        verify(consultationRepository)
                .findByTenantIdAndClinicIdOrderByConsultationDateDesc(
                        tenantId, clinicId);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateConsultation() {
        ConsultationUpdateRequest request =
                new ConsultationUpdateRequest();

        request.setChiefComplaint("Updated headache");
        request.setSymptoms("Updated symptoms");
        request.setClinicalFindings("Updated findings");
        request.setDiagnosis("Updated diagnosis");
        request.setTreatmentPlan("Updated treatment plan");
        request.setDoctorNotes("Updated notes");

        when(consultationRepository.findByIdAndTenantId(
                consultationId, tenantId))
                .thenReturn(Optional.of(consultation));

        when(consultationRepository.save(consultation))
                .thenReturn(consultation);

        ConsultationResponse response =
                consultationService.updateConsultation(
                        tenantId, consultationId, request);

        assertThat(response).isNotNull();
        assertThat(response.getChiefComplaint())
                .isEqualTo("Updated headache");
        assertThat(response.getSymptoms())
                .isEqualTo("Updated symptoms");
        assertThat(response.getClinicalFindings())
                .isEqualTo("Updated findings");
        assertThat(response.getDiagnosis())
                .isEqualTo("Updated diagnosis");
        assertThat(response.getTreatmentPlan())
                .isEqualTo("Updated treatment plan");
        assertThat(response.getDoctorNotes())
                .isEqualTo("Updated notes");

        verify(consultationRepository)
                .findByIdAndTenantId(consultationId, tenantId);
        verify(consultationRepository).save(consultation);
    }

    @Test
    void shouldRejectUpdateAfterCompletion() {
        consultation.setStatus(ConsultationStatus.COMPLETED.name());

        ConsultationUpdateRequest request =
                new ConsultationUpdateRequest();

        request.setChiefComplaint("Updated complaint");

        when(consultationRepository.findByIdAndTenantId(
                consultationId, tenantId))
                .thenReturn(Optional.of(consultation));

        assertThatThrownBy(() ->
                consultationService.updateConsultation(
                        tenantId, consultationId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Completed consultation cannot be modified");

        verify(consultationRepository)
                .findByIdAndTenantId(consultationId, tenantId);
        verify(consultationRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // COMPLETE
    // -------------------------------------------------------------------------

    @Test
    void shouldCompleteConsultation() {
        consultation.setStatus(ConsultationStatus.IN_PROGRESS.name());

        when(consultationRepository.findByIdAndTenantId(
                consultationId, tenantId))
                .thenReturn(Optional.of(consultation));

        when(consultationRepository.save(consultation))
                .thenReturn(consultation);

        ConsultationResponse response =
                consultationService.completeConsultation(
                        tenantId, consultationId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus())
                .isEqualTo(ConsultationStatus.COMPLETED.name());

        verify(consultationRepository)
                .findByIdAndTenantId(consultationId, tenantId);
        verify(consultationRepository).save(consultation);
    }

    @Test
    void shouldRejectAlreadyCompletedConsultation() {
        consultation.setStatus(ConsultationStatus.COMPLETED.name());

        when(consultationRepository.findByIdAndTenantId(
                consultationId, tenantId))
                .thenReturn(Optional.of(consultation));

        assertThatThrownBy(() ->
                consultationService.completeConsultation(
                        tenantId, consultationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Consultation is already completed");

        verify(consultationRepository)
                .findByIdAndTenantId(consultationId, tenantId);
        verify(consultationRepository, never()).save(any());
    }
}

package com.ayurclinic.consultation.service;

import com.ayurclinic.appointment.entity.Appointment;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.consultation.dto.ConsultationCreateRequest;
import com.ayurclinic.consultation.dto.ConsultationResponse;
import com.ayurclinic.consultation.dto.ConsultationUpdateRequest;
import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.enums.ConsultationStatus;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationService {

    private final ConsultationRepository consultationRepository;

    private final AppointmentRepository appointmentRepository;

    public ConsultationResponse createConsultation(
            UUID tenantId,
            ConsultationCreateRequest request) {

        validateTenant(tenantId);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Consultation request cannot be null"
            );
        }

        if (request.getAppointmentId() == null) {
            throw new IllegalArgumentException(
                    "Appointment ID is required"
            );
        }

        Appointment appointment =
                appointmentRepository
                        .findById(request.getAppointmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Appointment not found"
                                )
                        );

        if (!tenantId.equals(appointment.getTenantId())) {
            throw new IllegalArgumentException(
                    "Appointment not found"
            );
        }

        if (consultationRepository
                .existsByAppointmentIdAndTenantId(
                        request.getAppointmentId(),
                        tenantId)) {

            throw new IllegalStateException(
                    "Consultation already exists for this appointment"
            );
        }

        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException(
                    "Appointment date is required"
            );
        }

        Consultation consultation = new Consultation();

        consultation.setTenantId(tenantId);
        consultation.setClinicId(appointment.getClinicId());
        consultation.setAppointmentId(appointment.getId());
        consultation.setPatientId(appointment.getPatientId());
        consultation.setDoctorId(appointment.getDoctorId());
        consultation.setConsultationDate(
                appointment.getAppointmentDate()
        );

        consultation.setChiefComplaint(
                request.getChiefComplaint()
        );

        consultation.setSymptoms(
                request.getSymptoms()
        );

        consultation.setClinicalFindings(
                request.getClinicalFindings()
        );

        consultation.setDiagnosis(
                request.getDiagnosis()
        );

        consultation.setTreatmentPlan(
                request.getTreatmentPlan()
        );

        consultation.setDoctorNotes(
                request.getDoctorNotes()
        );

        consultation.setStatus(
                ConsultationStatus.IN_PROGRESS.name()
        );

        Consultation saved =
                consultationRepository.save(consultation);

        return ConsultationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationById(
            UUID tenantId,
            UUID consultationId) {

        validateTenant(tenantId);

        Consultation consultation =
                consultationRepository
                        .findByIdAndTenantId(
                                consultationId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Consultation not found"
                                )
                        );

        return ConsultationResponse.fromEntity(consultation);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getByAppointmentId(
            UUID tenantId,
            UUID appointmentId) {

        validateTenant(tenantId);

        Consultation consultation =
                consultationRepository
                        .findByAppointmentIdAndTenantId(
                                appointmentId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Consultation not found"
                                )
                        );

        return ConsultationResponse.fromEntity(consultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getPatientConsultations(
            UUID tenantId,
            UUID patientId) {

        validateTenant(tenantId);

        return consultationRepository
                .findByTenantIdAndPatientIdOrderByConsultationDateDesc(
                        tenantId,
                        patientId
                )
                .stream()
                .map(ConsultationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getDoctorConsultations(
            UUID tenantId,
            UUID doctorId) {

        validateTenant(tenantId);

        return consultationRepository
                .findByTenantIdAndDoctorIdOrderByConsultationDateDesc(
                        tenantId,
                        doctorId
                )
                .stream()
                .map(ConsultationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getClinicConsultations(
            UUID tenantId,
            UUID clinicId) {

        validateTenant(tenantId);

        return consultationRepository
                .findByTenantIdAndClinicIdOrderByConsultationDateDesc(
                        tenantId,
                        clinicId
                )
                .stream()
                .map(ConsultationResponse::fromEntity)
                .toList();
    }

    public ConsultationResponse updateConsultation(
            UUID tenantId,
            UUID consultationId,
            ConsultationUpdateRequest request) {

        validateTenant(tenantId);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Consultation update request cannot be null"
            );
        }

        Consultation consultation =
                consultationRepository
                        .findByIdAndTenantId(
                                consultationId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Consultation not found"
                                )
                        );

        if (ConsultationStatus.COMPLETED.name()
                .equals(consultation.getStatus())) {

            throw new IllegalStateException(
                    "Completed consultation cannot be modified"
            );
        }

        consultation.setChiefComplaint(
                request.getChiefComplaint()
        );

        consultation.setSymptoms(
                request.getSymptoms()
        );

        consultation.setClinicalFindings(
                request.getClinicalFindings()
        );

        consultation.setDiagnosis(
                request.getDiagnosis()
        );

        consultation.setTreatmentPlan(
                request.getTreatmentPlan()
        );

        consultation.setDoctorNotes(
                request.getDoctorNotes()
        );

        Consultation saved =
                consultationRepository.save(consultation);

        return ConsultationResponse.fromEntity(saved);
    }

    public ConsultationResponse completeConsultation(
            UUID tenantId,
            UUID consultationId) {

        validateTenant(tenantId);

        Consultation consultation =
                consultationRepository
                        .findByIdAndTenantId(
                                consultationId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Consultation not found"
                                )
                        );

        if (ConsultationStatus.COMPLETED.name()
                .equals(consultation.getStatus())) {

            throw new IllegalStateException(
                    "Consultation is already completed"
            );
        }

        consultation.setStatus(
                ConsultationStatus.COMPLETED.name()
        );

        Consultation saved =
                consultationRepository.save(consultation);

        return ConsultationResponse.fromEntity(saved);
    }

    private void validateTenant(UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }
    }
}
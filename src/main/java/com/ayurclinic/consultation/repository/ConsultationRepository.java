package com.ayurclinic.consultation.repository;

import com.ayurclinic.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository
        extends JpaRepository<Consultation, UUID> {

    Optional<Consultation> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<Consultation> findByAppointmentIdAndTenantId(
            UUID appointmentId,
            UUID tenantId
    );

    List<Consultation> findByTenantIdAndPatientIdOrderByConsultationDateDesc(
            UUID tenantId,
            UUID patientId
    );

    List<Consultation> findByTenantIdAndDoctorIdOrderByConsultationDateDesc(
            UUID tenantId,
            UUID doctorId
    );

    List<Consultation> findByTenantIdAndClinicIdOrderByConsultationDateDesc(
            UUID tenantId,
            UUID clinicId
    );

    boolean existsByAppointmentIdAndTenantId(
            UUID appointmentId,
            UUID tenantId
    );

    long countByTenantIdAndConsultationDateAndStatus(
            UUID tenantId,
            LocalDate consultationDate,
            String status
    );
}
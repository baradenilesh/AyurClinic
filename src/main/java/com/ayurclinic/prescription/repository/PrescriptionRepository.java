package com.ayurclinic.prescription.repository;

import com.ayurclinic.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository
        extends JpaRepository<Prescription, UUID> {

    Optional<Prescription> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<Prescription> findByConsultationIdAndTenantId(
            UUID consultationId,
            UUID tenantId
    );

    List<Prescription> findByTenantIdAndPatientIdOrderByPrescriptionDateDesc(
            UUID tenantId,
            UUID patientId
    );

    List<Prescription> findByTenantIdAndDoctorIdOrderByPrescriptionDateDesc(
            UUID tenantId,
            UUID doctorId
    );

    List<Prescription> findByTenantIdAndClinicIdOrderByPrescriptionDateDesc(
            UUID tenantId,
            UUID clinicId
    );

    boolean existsByConsultationIdAndTenantId(
            UUID consultationId,
            UUID tenantId
    );
}
package com.ayurclinic.followup.repository;

import com.ayurclinic.followup.entity.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowUpRepository
        extends JpaRepository<FollowUp, UUID> {

    Optional<FollowUp> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<FollowUp>
    findByTenantIdAndPatientIdOrderByFollowUpDateAsc(
            UUID tenantId,
            UUID patientId
    );

    List<FollowUp>
    findByTenantIdAndDoctorIdOrderByFollowUpDateAsc(
            UUID tenantId,
            UUID doctorId
    );

    List<FollowUp>
    findByTenantIdAndClinicIdOrderByFollowUpDateAsc(
            UUID tenantId,
            UUID clinicId
    );

    List<FollowUp>
    findByTenantIdAndFollowUpDateOrderByFollowUpDateAsc(
            UUID tenantId,
            LocalDate followUpDate
    );

    List<FollowUp>
    findByTenantIdAndStatusOrderByFollowUpDateAsc(
            UUID tenantId,
            String status
    );

    List<FollowUp>
    findByTenantIdAndFollowUpDateAndStatus(
            UUID tenantId,
            LocalDate followUpDate,
            String status
    );

    long countByTenantIdAndFollowUpDate(
            UUID tenantId,
            LocalDate followUpDate
    );

    long countByTenantIdAndFollowUpDateAndStatus(
            UUID tenantId,
            LocalDate followUpDate,
            String status
    );
}
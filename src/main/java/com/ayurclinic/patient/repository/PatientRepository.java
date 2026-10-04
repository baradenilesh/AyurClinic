package com.ayurclinic.patient.repository;

import com.ayurclinic.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<Patient> findByPatientNumberAndTenantId(
            String patientNumber,
            UUID tenantId
    );

    boolean existsByPatientNumberAndTenantId(
            String patientNumber,
            UUID tenantId
    );

    boolean existsByMobileAndTenantId(
            String mobile,
            UUID tenantId
    );

    boolean existsByMobileAndTenantIdAndIdNot(
            String mobile,
            UUID tenantId,
            UUID id
    );

    List<Patient> findByTenantId(
            UUID tenantId
    );

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, String status);

    List<Patient> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

    List<Patient> findByTenantIdAndFirstNameContainingIgnoreCaseOrTenantIdAndLastNameContainingIgnoreCase(
            UUID tenantId1,
            String firstName,
            UUID tenantId2,
            String lastName
    );

    Optional<Patient> findByMobileAndTenantId(
            String mobile,
            UUID tenantId
    );

    List<Patient> findByTenantIdAndPatientNumberContainingIgnoreCase(
            UUID tenantId,
            String patientNumber
    );
}
package com.ayurclinic.doctor.repository;

import com.ayurclinic.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<Doctor> findByTenantId(
            UUID tenantId
    );

    List<Doctor> findByTenantIdAndClinicId(
            UUID tenantId,
            UUID clinicId
    );

    List<Doctor> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

    long countByTenantId(
            UUID tenantId
    );

    long countByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

    boolean existsByRegistrationNumberAndTenantId(
            String registrationNumber,
            UUID tenantId
    );

    boolean existsByRegistrationNumberAndTenantIdAndIdNot(
            String registrationNumber,
            UUID tenantId,
            UUID id
    );

    List<Doctor> findByClinicIdAndStatus(
            UUID clinicId,
            String status
    );

    Optional<Doctor> findByIdAndClinicIdAndStatus(
            UUID id,
            UUID clinicId,
            String status
    );
}
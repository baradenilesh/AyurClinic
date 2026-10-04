package com.ayurclinic.clinic.repository;

import com.ayurclinic.clinic.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClinicRepository extends JpaRepository<Clinic, UUID> {

    Optional<Clinic> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Clinic> findByTenantId(UUID tenantId);

    boolean existsByTenantIdAndName(UUID tenantId, String name);
}
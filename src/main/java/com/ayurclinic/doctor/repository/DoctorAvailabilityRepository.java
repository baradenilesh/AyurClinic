package com.ayurclinic.doctor.repository;

import com.ayurclinic.doctor.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorAvailabilityRepository
        extends JpaRepository<DoctorAvailability, UUID> {

    Optional<DoctorAvailability> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<DoctorAvailability> findByTenantIdAndDoctorIdOrderByDayOfWeek(
            UUID tenantId,
            UUID doctorId
    );

    List<DoctorAvailability> findByTenantIdAndDoctorIdAndStatusOrderByDayOfWeek(
            UUID tenantId,
            UUID doctorId,
            String status
    );

    boolean existsByTenantIdAndDoctorIdAndDayOfWeek(
            UUID tenantId,
            UUID doctorId,
            Short dayOfWeek
    );

    boolean existsByTenantIdAndDoctorIdAndDayOfWeekAndIdNot(
            UUID tenantId,
            UUID doctorId,
            Short dayOfWeek,
            UUID id
    );

    List<DoctorAvailability> findByDoctorIdAndStatusOrderByDayOfWeek(
            UUID doctorId,
            String status
    );
}
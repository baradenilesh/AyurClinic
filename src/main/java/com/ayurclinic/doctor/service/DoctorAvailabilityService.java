package com.ayurclinic.doctor.service;

import com.ayurclinic.doctor.dto.CreateDoctorAvailabilityRequest;
import com.ayurclinic.doctor.dto.DoctorAvailabilityResponse;
import com.ayurclinic.doctor.dto.UpdateDoctorAvailabilityRequest;
import com.ayurclinic.doctor.dto.UpdateDoctorAvailabilityStatusRequest;
import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.entity.DoctorAvailability;
import com.ayurclinic.doctor.repository.DoctorAvailabilityRepository;
import com.ayurclinic.doctor.repository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public DoctorAvailabilityResponse createAvailability(
            UUID tenantId,
            CreateDoctorAvailabilityRequest request) {
        System.out.println("JVM TimeZone = " +
                java.util.TimeZone.getDefault().getID());

        System.out.println("JVM ZoneId = " +
                java.time.ZoneId.systemDefault());

        validateTenant(tenantId);

        validateDoctorBelongsToTenant(
                tenantId,
                request.getDoctorId()
        );

        if (doctorAvailabilityRepository
                .existsByTenantIdAndDoctorIdAndDayOfWeek(
                        tenantId,
                        request.getDoctorId(),
                        request.getDayOfWeek())) {

            throw new IllegalArgumentException(
                    "Availability already exists for this doctor and day"
            );
        }

        validateTimes(
                request.getStartTime(),
                request.getEndTime()
        );

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setTenantId(tenantId);
        availability.setDoctorId(request.getDoctorId());
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setSlotDurationMinutes(
                request.getSlotDurationMinutes()
        );
        availability.setStatus("ACTIVE");
        System.out.println("=================================");
        System.out.println("BEFORE SAVE");
        System.out.println("Start Time = " + availability.getStartTime());
        System.out.println("End Time   = " + availability.getEndTime());
        System.out.println("=================================");
        DoctorAvailability saved =
                doctorAvailabilityRepository.save(
                        availability
                );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> getAvailability(
            UUID tenantId,
            UUID doctorId) {

        validateTenant(tenantId);

        validateDoctorBelongsToTenant(
                tenantId,
                doctorId
        );

        return doctorAvailabilityRepository
                .findByTenantIdAndDoctorIdOrderByDayOfWeek(
                        tenantId,
                        doctorId
                )
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilityResponse getAvailabilityById(
            UUID tenantId,
            UUID availabilityId) {

        validateTenant(tenantId);

        DoctorAvailability availability =
                doctorAvailabilityRepository
                        .findByIdAndTenantId(
                                availabilityId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Doctor availability not found"
                                )
                        );

        return toResponse(availability);
    }

    @Transactional
    public DoctorAvailabilityResponse updateAvailability(
            UUID tenantId,
            UUID availabilityId,
            UpdateDoctorAvailabilityRequest request) {

        validateTenant(tenantId);

        DoctorAvailability availability =
                doctorAvailabilityRepository
                        .findByIdAndTenantId(
                                availabilityId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Doctor availability not found"
                                )
                        );

        validateTimes(
                request.getStartTime(),
                request.getEndTime()
        );

        if (!request.getDayOfWeek()
                .equals(availability.getDayOfWeek())) {

            if (doctorAvailabilityRepository
                    .existsByTenantIdAndDoctorIdAndDayOfWeekAndIdNot(
                            tenantId,
                            availability.getDoctorId(),
                            request.getDayOfWeek(),
                            availabilityId)) {

                throw new IllegalArgumentException(
                        "Availability already exists for this doctor and day"
                );
            }
        }

        availability.setDayOfWeek(
                request.getDayOfWeek()
        );

        availability.setStartTime(
                request.getStartTime()
        );

        availability.setEndTime(
                request.getEndTime()
        );

        availability.setSlotDurationMinutes(
                request.getSlotDurationMinutes()
        );

        DoctorAvailability updated =
                doctorAvailabilityRepository.save(
                        availability
                );

        return toResponse(updated);
    }

    @Transactional
    public DoctorAvailabilityResponse updateAvailabilityStatus(
            UUID tenantId,
            UUID availabilityId,
            UpdateDoctorAvailabilityStatusRequest request) {

        validateTenant(tenantId);

        String status =
                request.getStatus()
                        .trim()
                        .toUpperCase();

        if (!status.equals("ACTIVE")
                && !status.equals("INACTIVE")) {

            throw new IllegalArgumentException(
                    "Status must be ACTIVE or INACTIVE"
            );
        }

        DoctorAvailability availability =
                doctorAvailabilityRepository
                        .findByIdAndTenantId(
                                availabilityId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Doctor availability not found"
                                )
                        );

        availability.setStatus(status);

        DoctorAvailability updated =
                doctorAvailabilityRepository.save(
                        availability
                );

        return toResponse(updated);
    }

    private void validateTenant(UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }
    }

    private void validateDoctorBelongsToTenant(
            UUID tenantId,
            UUID doctorId) {

        doctorRepository
                .findByIdAndTenantId(
                        doctorId,
                        tenantId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Doctor not found"
                        )
                );
    }

    private void validateTimes(
            java.time.LocalTime startTime,
            java.time.LocalTime endTime) {

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required"
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }
    }

    private DoctorAvailabilityResponse toResponse(
            DoctorAvailability availability) {

        DoctorAvailabilityResponse response =
                new DoctorAvailabilityResponse();

        response.setId(availability.getId());
        response.setTenantId(
                availability.getTenantId()
        );
        response.setDoctorId(
                availability.getDoctorId()
        );
        response.setDayOfWeek(
                availability.getDayOfWeek()
        );
        response.setStartTime(
                availability.getStartTime()
        );
        response.setEndTime(
                availability.getEndTime()
        );
        response.setSlotDurationMinutes(
                availability.getSlotDurationMinutes()
        );
        response.setStatus(
                availability.getStatus()
        );

        return response;
    }
}
package com.ayurclinic.publicapi.service;

import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.entity.DoctorAvailability;
import com.ayurclinic.doctor.repository.DoctorAvailabilityRepository;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.publicapi.dto.PublicAvailabilityResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicAvailabilityService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Value("${ayurclinic.public.clinic-id}")
    private UUID publicClinicId;

    @Transactional(readOnly = true)
    public List<PublicAvailabilityResponse> getDoctorAvailability(
            UUID doctorId
    ) {

        Doctor doctor = doctorRepository
                .findByIdAndClinicIdAndStatus(
                        doctorId,
                        publicClinicId,
                        "ACTIVE"
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Public doctor not found"
                        )
                );

        return doctorAvailabilityRepository
                .findByDoctorIdAndStatusOrderByDayOfWeek(
                        doctor.getId(),
                        "ACTIVE"
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PublicAvailabilityResponse toResponse(
            DoctorAvailability availability
    ) {

        return PublicAvailabilityResponse.builder()
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .slotDurationMinutes(
                        availability.getSlotDurationMinutes()
                )
                .build();
    }
}
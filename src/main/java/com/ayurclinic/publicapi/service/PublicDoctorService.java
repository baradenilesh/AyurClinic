package com.ayurclinic.publicapi.service;

import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.publicapi.dto.PublicDoctorResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicDoctorService {

    private final DoctorRepository doctorRepository;

    @Value("${ayurclinic.public.clinic-id}")
    private UUID publicClinicId;

    @Transactional(readOnly = true)
    public List<PublicDoctorResponse> getPublicDoctors() {

        return doctorRepository
                .findByClinicIdAndStatus(publicClinicId, "ACTIVE")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PublicDoctorResponse toResponse(Doctor doctor) {

        String name = doctor.getFirstName();

        if (doctor.getLastName() != null && !doctor.getLastName().isBlank()) {
            name = name + " " + doctor.getLastName();
        }

        return PublicDoctorResponse.builder()
                .id(doctor.getId())
                .name(name)
                .qualification(doctor.getQualification())
                .specialization(doctor.getSpecialization())
                .experienceYears(doctor.getExperienceYears())
                .bio(doctor.getBio())
                .photoUrl(null)
                .build();
    }

    @Transactional(readOnly = true)
    public PublicDoctorResponse getPublicDoctor(UUID doctorId) {

        Doctor doctor = doctorRepository
                .findByIdAndClinicIdAndStatus(
                        doctorId,
                        publicClinicId,
                        "ACTIVE"
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Public doctor not found")
                );

        return toResponse(doctor);
    }
}
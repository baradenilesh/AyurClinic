package com.ayurclinic.doctor.service;

import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.doctor.dto.CreateDoctorRequest;
import com.ayurclinic.doctor.dto.DoctorResponse;
import com.ayurclinic.doctor.dto.UpdateDoctorRequest;
import com.ayurclinic.doctor.dto.UpdateDoctorStatusRequest;
import com.ayurclinic.doctor.entity.Doctor;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ClinicRepository clinicRepository;

    @Transactional
    public DoctorResponse createDoctor(
            UUID tenantId,
            CreateDoctorRequest request) {

        validateTenant(tenantId);

        validateClinicBelongsToTenant(
                tenantId,
                request.getClinicId()
        );

        if (request.getRegistrationNumber() != null
                && !request.getRegistrationNumber().isBlank()
                && doctorRepository.existsByRegistrationNumberAndTenantId(
                request.getRegistrationNumber().trim(),
                tenantId)) {

            throw new IllegalArgumentException(
                    "Doctor with registration number '"
                            + request.getRegistrationNumber().trim()
                            + "' already exists"
            );
        }

        Doctor doctor = new Doctor();

        doctor.setTenantId(tenantId);
        doctor.setClinicId(request.getClinicId());
        doctor.setFirstName(request.getFirstName().trim());
        doctor.setLastName(request.getLastName());
        doctor.setQualification(request.getQualification());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setRegistrationNumber(
                request.getRegistrationNumber() != null
                        ? request.getRegistrationNumber().trim()
                        : null
        );
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());
        doctor.setPhotoS3Key(request.getPhotoS3Key());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setStatus("ACTIVE");

        Doctor savedDoctor = doctorRepository.save(doctor);

        return toResponse(savedDoctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctor(
            UUID tenantId,
            UUID doctorId) {

        validateTenant(tenantId);

        Doctor doctor = doctorRepository
                .findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Doctor not found")
                );

        return toResponse(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctors(
            UUID tenantId) {

        validateTenant(tenantId);

        return doctorRepository
                .findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctorsByClinic(
            UUID tenantId,
            UUID clinicId) {

        validateTenant(tenantId);

        validateClinicBelongsToTenant(
                tenantId,
                clinicId
        );

        return doctorRepository
                .findByTenantIdAndClinicId(
                        tenantId,
                        clinicId
                )
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponse updateDoctor(
            UUID tenantId,
            UUID doctorId,
            UpdateDoctorRequest request) {

        validateTenant(tenantId);

        Doctor doctor = doctorRepository
                .findByIdAndTenantId(
                        doctorId,
                        tenantId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Doctor not found")
                );

        if (request.getClinicId() != null
                && !request.getClinicId()
                .equals(doctor.getClinicId())) {

            validateClinicBelongsToTenant(
                    tenantId,
                    request.getClinicId()
            );

            doctor.setClinicId(request.getClinicId());
        }

        if (request.getRegistrationNumber() != null
                && !request.getRegistrationNumber().isBlank()
                && !request.getRegistrationNumber().trim()
                .equals(doctor.getRegistrationNumber())) {

            String registrationNumber =
                    request.getRegistrationNumber().trim();

            if (doctorRepository
                    .existsByRegistrationNumberAndTenantIdAndIdNot(
                            registrationNumber,
                            tenantId,
                            doctorId)) {

                throw new IllegalArgumentException(
                        "Doctor with registration number '"
                                + registrationNumber
                                + "' already exists"
                );
            }

            doctor.setRegistrationNumber(registrationNumber);
        }

        doctor.setFirstName(request.getFirstName().trim());
        doctor.setLastName(request.getLastName());
        doctor.setQualification(request.getQualification());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());
        doctor.setPhotoS3Key(request.getPhotoS3Key());
        doctor.setConsultationFee(request.getConsultationFee());

        Doctor updatedDoctor = doctorRepository.save(doctor);

        return toResponse(updatedDoctor);
    }

    @Transactional
    public DoctorResponse updateDoctorStatus(
            UUID tenantId,
            UUID doctorId,
            UpdateDoctorStatusRequest request) {

        validateTenant(tenantId);

        String status = request.getStatus().trim().toUpperCase();

        if (!status.equals("ACTIVE")
                && !status.equals("INACTIVE")) {

            throw new IllegalArgumentException(
                    "Status must be ACTIVE or INACTIVE"
            );
        }

        Doctor doctor = doctorRepository
                .findByIdAndTenantId(
                        doctorId,
                        tenantId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Doctor not found")
                );

        doctor.setStatus(status);

        Doctor updatedDoctor = doctorRepository.save(doctor);

        return toResponse(updatedDoctor);
    }

    private void validateTenant(UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }
    }

    private void validateClinicBelongsToTenant(
            UUID tenantId,
            UUID clinicId) {

        clinicRepository
                .findByIdAndTenantId(
                        clinicId,
                        tenantId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Clinic not found"
                        )
                );
    }

    private DoctorResponse toResponse(Doctor doctor) {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctor.getId());
        response.setTenantId(doctor.getTenantId());
        response.setClinicId(doctor.getClinicId());
        response.setUserId(doctor.getUserId());

        response.setFirstName(doctor.getFirstName());
        response.setLastName(doctor.getLastName());

        response.setQualification(doctor.getQualification());
        response.setSpecialization(doctor.getSpecialization());
        response.setRegistrationNumber(
                doctor.getRegistrationNumber()
        );
        response.setExperienceYears(
                doctor.getExperienceYears()
        );

        response.setBio(doctor.getBio());
        response.setPhotoS3Key(doctor.getPhotoS3Key());
        response.setConsultationFee(
                doctor.getConsultationFee()
        );

        response.setStatus(doctor.getStatus());

        response.setCreatedAt(doctor.getCreatedAt());
        response.setUpdatedAt(doctor.getUpdatedAt());

        return response;
    }
}
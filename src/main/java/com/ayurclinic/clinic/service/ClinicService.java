package com.ayurclinic.clinic.service;

import com.ayurclinic.clinic.dto.ClinicStatusUpdateRequest;
import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.clinic.entity.ClinicStatus;
import com.ayurclinic.clinic.dto.ClinicCreateRequest;
import com.ayurclinic.clinic.dto.ClinicUpdateRequest;
import com.ayurclinic.clinic.dto.ClinicResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;

    @Transactional
    public ClinicResponse createClinic(UUID tenantId, ClinicCreateRequest request) {

        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }

        if (clinicRepository.existsByTenantIdAndName(tenantId, request.getName())) {
            throw new IllegalArgumentException(
                    "Clinic with name '" + request.getName() + "' already exists"
            );
        }

        Clinic clinic = Clinic.builder()
                .tenantId(tenantId)
                .name(request.getName().trim())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .logoS3Key(request.getLogoS3Key())
                .status(ClinicStatus.ACTIVE)
                .build();

        Clinic savedClinic = clinicRepository.save(clinic);

        return toResponse(savedClinic);
    }

    @Transactional(readOnly = true)
    public ClinicResponse getClinic(UUID tenantId, UUID clinicId) {

        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }

        Clinic clinic = clinicRepository
                .findByIdAndTenantId(clinicId, tenantId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Clinic not found")
                );

        return toResponse(clinic);
    }

    @Transactional(readOnly = true)
    public ClinicResponse getCurrentTenantClinic(UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }

        Clinic clinic = clinicRepository
                .findByTenantId(tenantId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Clinic not found")
                );

        return toResponse(clinic);
    }

    private ClinicResponse toResponse(Clinic clinic) {

        return ClinicResponse.builder()
                .id(clinic.getId())
                .tenantId(clinic.getTenantId())
                .name(clinic.getName())
                .addressLine1(clinic.getAddressLine1())
                .addressLine2(clinic.getAddressLine2())
                .city(clinic.getCity())
                .state(clinic.getState())
                .postalCode(clinic.getPostalCode())
                .country(clinic.getCountry())
                .phone(clinic.getPhone())
                .email(clinic.getEmail())
                .website(clinic.getWebsite())
                .logoS3Key(clinic.getLogoS3Key())
                .status(clinic.getStatus())
                .createdAt(clinic.getCreatedAt())
                .updatedAt(clinic.getUpdatedAt())
                .build();
    }

    @Transactional
    public ClinicResponse updateClinic(
            UUID tenantId,
            UUID clinicId,
            ClinicUpdateRequest request) {

        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }

        Clinic clinic = clinicRepository
                .findByIdAndTenantId(clinicId, tenantId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Clinic not found")
                );

        if (request.getName() != null &&
                !request.getName().trim().isEmpty() &&
                !request.getName().trim().equals(clinic.getName())) {

            if (clinicRepository.existsByTenantIdAndName(
                    tenantId,
                    request.getName().trim())) {

                throw new IllegalArgumentException(
                        "Clinic with name '" +
                                request.getName().trim() +
                                "' already exists"
                );
            }

            clinic.setName(request.getName().trim());
        }

        if (request.getAddressLine1() != null) {
            clinic.setAddressLine1(request.getAddressLine1());
        }

        if (request.getAddressLine2() != null) {
            clinic.setAddressLine2(request.getAddressLine2());
        }

        if (request.getCity() != null) {
            clinic.setCity(request.getCity());
        }

        if (request.getState() != null) {
            clinic.setState(request.getState());
        }

        if (request.getPostalCode() != null) {
            clinic.setPostalCode(request.getPostalCode());
        }

        if (request.getCountry() != null) {
            clinic.setCountry(request.getCountry());
        }

        if (request.getPhone() != null) {
            clinic.setPhone(request.getPhone());
        }

        if (request.getEmail() != null) {
            clinic.setEmail(request.getEmail());
        }

        if (request.getWebsite() != null) {
            clinic.setWebsite(request.getWebsite());
        }

        if (request.getLogoS3Key() != null) {
            clinic.setLogoS3Key(request.getLogoS3Key());
        }

        Clinic updatedClinic = clinicRepository.save(clinic);

        return toResponse(updatedClinic);
    }

    @Transactional
    public ClinicResponse updateClinicStatus(
            UUID tenantId,
            UUID clinicId,
            ClinicStatusUpdateRequest request) {

        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }

        Clinic clinic = clinicRepository
                .findByIdAndTenantId(clinicId, tenantId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Clinic not found")
                );

        clinic.setStatus(request.getStatus());

        Clinic updatedClinic = clinicRepository.save(clinic);

        return toResponse(updatedClinic);
    }
}
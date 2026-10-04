package com.ayurclinic.publicapi.service;

import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.publicapi.dto.PublicClinicResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicClinicService {

    private final ClinicRepository clinicRepository;

    @Value("${ayurclinic.public.clinic-id}")
    private UUID publicClinicId;

    @Transactional(readOnly = true)
    public PublicClinicResponse getPublicClinic() {

        Clinic clinic = clinicRepository
                .findById(publicClinicId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Public clinic not found")
                );

        return PublicClinicResponse.builder()
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
                .logoUrl(null)
                .build();
    }
    @Transactional(readOnly = true)
    public Clinic getPublicClinicEntity() {

        return clinicRepository
                .findById(publicClinicId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Public clinic not found"
                        )
                );
    }


}
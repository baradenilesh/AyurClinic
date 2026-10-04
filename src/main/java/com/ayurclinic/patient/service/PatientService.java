package com.ayurclinic.patient.service;

import com.ayurclinic.common.exception.ResourceNotFoundException;
import com.ayurclinic.patient.dto.CreatePatientRequest;
import com.ayurclinic.patient.dto.PatientResponse;
import com.ayurclinic.patient.dto.UpdatePatientRequest;
import com.ayurclinic.patient.dto.UpdatePatientStatusRequest;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.repository.PatientNumberCounterRepository;
import com.ayurclinic.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ayurclinic.clinic.repository.ClinicRepository;


import java.util.UUID;



import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    private final ClinicRepository clinicRepository;
    private final PatientNumberCounterRepository patientNumberCounterRepository;
    public PatientService(
            PatientRepository patientRepository,
            ClinicRepository clinicRepository,
            PatientNumberCounterRepository patientNumberCounterRepository
    ) {
        this.patientRepository = patientRepository;
        this.clinicRepository = clinicRepository;
        this.patientNumberCounterRepository = patientNumberCounterRepository;
    }
    public PatientResponse createPatient(
            UUID tenantId,
            CreatePatientRequest request
    ) {
        validateClinicBelongsToTenant(
                request.getClinicId(),
                tenantId
        );

        if (patientRepository.existsByMobileAndTenantId(
                request.getMobile(),
                tenantId
        )) {
            throw new IllegalArgumentException(
                    "Patient with mobile number already exists"
            );
        }

        Patient patient = new Patient();

        patient.setTenantId(tenantId);
        patient.setClinicId(request.getClinicId());

        patient.setPatientNumber(generatePatientNumber(tenantId));

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setMobile(request.getMobile());
        patient.setEmail(request.getEmail());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(
                request.getEmergencyContactName()
        );
        patient.setEmergencyContactMobile(
                request.getEmergencyContactMobile()
        );

        patient.setStatus("ACTIVE");

        Patient savedPatient = patientRepository.save(patient);

        return mapToResponse(savedPatient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatient(
            UUID tenantId,
            UUID id
    ) {

        Patient patient = patientRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found: " + id
                        )
                );

        return mapToResponse(patient);
    }
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(
            UUID tenantId,
            String name
    ) {
        return searchPatients(
                tenantId,
                name,
                null,
                null
        );
    }
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(
            UUID tenantId,
            String name,
            String mobile,
            String patientNumber
    ) {

        List<Patient> patients;

        if (mobile != null && !mobile.isBlank()) {

            patients = patientRepository
                    .findByMobileAndTenantId(
                            mobile,
                            tenantId
                    )
                    .map(List::of)
                    .orElse(List.of());

        } else if (patientNumber != null && !patientNumber.isBlank()) {

            patients = patientRepository
                    .findByTenantIdAndPatientNumberContainingIgnoreCase(
                            tenantId,
                            patientNumber
                    );

        } else if (name != null && !name.isBlank()) {

            patients = patientRepository
                    .findByTenantIdAndFirstNameContainingIgnoreCaseOrTenantIdAndLastNameContainingIgnoreCase(
                            tenantId,
                            name,
                            tenantId,
                            name
                    );

        } else {

            patients = patientRepository
                    .findByTenantIdAndStatus(
                            tenantId,
                            "ACTIVE"
                    );
        }

        return patients.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PatientResponse updatePatient(
            UUID tenantId,
            UUID id,
            UpdatePatientRequest request
    ) {

        Patient patient = patientRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found: " + id
                        )
                );

        if (patientRepository.existsByMobileAndTenantIdAndIdNot(
                request.getMobile(),
                tenantId,
                id
        )) {
            throw new IllegalArgumentException(
                    "Another patient with this mobile number already exists"
            );
        }

        if (request.getClinicId() != null) {

            validateClinicBelongsToTenant(
                    request.getClinicId(),
                    tenantId
            );

            patient.setClinicId(
                    request.getClinicId()
            );
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setMobile(request.getMobile());
        patient.setEmail(request.getEmail());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(
                request.getEmergencyContactName()
        );
        patient.setEmergencyContactMobile(
                request.getEmergencyContactMobile()
        );

        Patient updatedPatient = patientRepository.save(patient);

        return mapToResponse(updatedPatient);
    }

    public PatientResponse updatePatientStatus(
            UUID tenantId,
            UUID id,
            UpdatePatientStatusRequest request
    ) {

        Patient patient = patientRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found: " + id
                        )
                );

        String status = request.getStatus().toUpperCase();

        if (!status.equals("ACTIVE")
                && !status.equals("INACTIVE")) {

            throw new IllegalArgumentException(
                    "Status must be ACTIVE or INACTIVE"
            );
        }

        patient.setStatus(status);

        return mapToResponse(
                patientRepository.save(patient)
        );
    }
    private void validateClinicBelongsToTenant(
            UUID clinicId,
            UUID tenantId
    ) {

        clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Clinic not found: " + clinicId
                )
        );
    }
    private String generatePatientNumber(UUID tenantId) {

        patientNumberCounterRepository.incrementCounter(tenantId);

        Long number =
                patientNumberCounterRepository.getCurrentNumber(tenantId);

        return String.format(
                "PAT-%06d",
                number
        );
    }

    private PatientResponse mapToResponse(
            Patient patient
    ) {

        PatientResponse response = new PatientResponse();

        response.setId(patient.getId());
        response.setPatientNumber(patient.getPatientNumber());
        response.setTenantId(patient.getTenantId());
        response.setClinicId(patient.getClinicId());

        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());

        response.setGender(patient.getGender());
        response.setDateOfBirth(patient.getDateOfBirth());

        response.setMobile(patient.getMobile());
        response.setEmail(patient.getEmail());
        response.setAddress(patient.getAddress());

        response.setStatus(patient.getStatus());

        return response;
    }

    @Transactional(readOnly = true)
    public Patient findPatientByMobile(
            UUID tenantId,
            String mobile
    ) {
        return patientRepository
                .findByMobileAndTenantId(
                        mobile,
                        tenantId
                )
                .orElse(null);
    }

    @Transactional
    public Patient createPatientForPublicBooking(
            UUID tenantId,
            UUID clinicId,
            String patientName,
            String mobile,
            String email
    ) {
        // Re-check duplicate mobile inside the transaction
        Patient existingPatient = patientRepository
                .findByMobileAndTenantId(mobile, tenantId)
                .orElse(null);

        if (existingPatient != null) {
            return existingPatient;
        }

        // Validate clinic belongs to tenant
        validateClinicBelongsToTenant(clinicId, tenantId);

        String firstName;
        String lastName = null;

        String trimmedName = patientName.trim();

        int firstSpace = trimmedName.indexOf(' ');

        if (firstSpace > 0) {
            firstName = trimmedName.substring(0, firstSpace);
            lastName = trimmedName.substring(firstSpace + 1).trim();

            if (lastName.isBlank()) {
                lastName = null;
            }
        } else {
            firstName = trimmedName;
        }

        Patient patient = new Patient();

        patient.setTenantId(tenantId);
        patient.setClinicId(clinicId);
        patient.setPatientNumber(generatePatientNumber(tenantId));
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setMobile(mobile);
        patient.setEmail(email);
        patient.setStatus("ACTIVE");

        return patientRepository.save(patient);
    }
}
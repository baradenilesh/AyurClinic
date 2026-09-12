package com.ayurclinic.patient.service;

import com.ayurclinic.common.exception.ResourceNotFoundException;
import com.ayurclinic.patient.dto.CreatePatientRequest;
import com.ayurclinic.patient.dto.PatientResponse;
import com.ayurclinic.patient.dto.UpdatePatientRequest;
import com.ayurclinic.patient.dto.UpdatePatientStatusRequest;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientResponse createPatient(CreatePatientRequest request) {

        if (patientRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException(
                    "Patient with mobile number already exists"
            );
        }

        Patient patient = new Patient();

        patient.setTenantId(request.getTenantId());
        patient.setClinicId(request.getClinicId());

        patient.setPatientNumber(generatePatientNumber());

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
    public PatientResponse getPatient(UUID id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found: " + id
                        )
                );

        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(String name) {

        List<Patient> patients;

        if (name == null || name.isBlank()) {
            patients = patientRepository.findByStatus("ACTIVE");
        } else {
            patients =
                    patientRepository
                            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                                    name,
                                    name
                            );
        }

        return patients.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PatientResponse updatePatient(
            UUID id,
            UpdatePatientRequest request
    ) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found: " + id
                        )
                );

        if (patientRepository.existsByMobileAndIdNot(
                request.getMobile(),
                id
        )) {
            throw new IllegalArgumentException(
                    "Another patient with this mobile number already exists"
            );
        }

        if (request.getClinicId() != null) {
            patient.setClinicId(request.getClinicId());
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
            UUID id,
            UpdatePatientStatusRequest request
    ) {

        Patient patient = patientRepository.findById(id)
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

        return mapToResponse(patientRepository.save(patient));
    }

    private String generatePatientNumber() {

        long count = patientRepository.count() + 1;

        return String.format(
                "PAT-%06d",
                count
        );
    }

    private PatientResponse mapToResponse(Patient patient) {

        PatientResponse response = new PatientResponse();

        response.setId(patient.getId());
        response.setPatientNumber(patient.getPatientNumber());
        response.setTenantId(patient.getTenantId());
        response.setClinicId(patient.getClinicId());
        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());
        response.setMobile(patient.getMobile());
        response.setEmail(patient.getEmail());
        response.setStatus(patient.getStatus());

        return response;
    }
}
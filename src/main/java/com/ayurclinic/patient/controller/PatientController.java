package com.ayurclinic.patient.controller;

import com.ayurclinic.patient.dto.CreatePatientRequest;
import com.ayurclinic.patient.dto.PatientResponse;
import com.ayurclinic.patient.dto.UpdatePatientRequest;
import com.ayurclinic.patient.dto.UpdatePatientStatusRequest;
import com.ayurclinic.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse createPatient(
            @Valid @RequestBody CreatePatientRequest request
    ) {
        return patientService.createPatient(request);
    }

    @GetMapping("/{id}")
    public PatientResponse getPatient(
            @PathVariable UUID id
    ) {
        return patientService.getPatient(id);
    }

    @GetMapping
    public List<PatientResponse> searchPatients(
            @RequestParam(required = false) String name
    ) {
        return patientService.searchPatients(name);
    }

    @PutMapping("/{id}")
    public PatientResponse updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        return patientService.updatePatient(id, request);
    }

    @PatchMapping("/{id}/status")
    public PatientResponse updatePatientStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientStatusRequest request
    ) {
        return patientService.updatePatientStatus(id, request);
    }
}
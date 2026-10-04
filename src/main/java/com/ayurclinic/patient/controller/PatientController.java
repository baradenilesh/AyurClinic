package com.ayurclinic.patient.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.patient.dto.CreatePatientRequest;
import com.ayurclinic.patient.dto.PatientResponse;
import com.ayurclinic.patient.dto.UpdatePatientRequest;
import com.ayurclinic.patient.dto.UpdatePatientStatusRequest;
import com.ayurclinic.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse createPatient(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreatePatientRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        return patientService.createPatient(
                tenantId,
                request
        );
    }

    @GetMapping("/{id}")
    public PatientResponse getPatient(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return patientService.getPatient(
                tenantId,
                id
        );
    }

    @GetMapping
    public List<PatientResponse> searchPatients(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String patientNumber
    ) {
        UUID tenantId = principal.getTenantId();

        return patientService.searchPatients(
                tenantId,
                name,
                mobile,
                patientNumber
        );
    }

    @PutMapping("/{id}")
    public PatientResponse updatePatient(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        return patientService.updatePatient(
                tenantId,
                id,
                request
        );
    }

    @PatchMapping("/{id}/status")
    public PatientResponse updatePatientStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientStatusRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        return patientService.updatePatientStatus(
                tenantId,
                id,
                request
        );
    }
}
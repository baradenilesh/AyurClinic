package com.ayurclinic.clinic.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.clinic.dto.ClinicCreateRequest;
import com.ayurclinic.clinic.dto.ClinicResponse;
import com.ayurclinic.clinic.dto.ClinicStatusUpdateRequest;
import com.ayurclinic.clinic.dto.ClinicUpdateRequest;
import com.ayurclinic.clinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    @PostMapping
    public ResponseEntity<ClinicResponse> createClinic(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ClinicCreateRequest request) {

        UUID tenantId = principal.getTenantId();

        ClinicResponse response =
                clinicService.createClinic(tenantId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{clinicId}")
    public ResponseEntity<ClinicResponse> getClinic(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID clinicId) {

        UUID tenantId = principal.getTenantId();

        ClinicResponse response =
                clinicService.getClinic(tenantId, clinicId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<ClinicResponse> getCurrentClinic(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        UUID tenantId = principal.getTenantId();

        ClinicResponse response =
                clinicService.getCurrentTenantClinic(tenantId);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    @PutMapping("/{clinicId}")
    public ResponseEntity<ClinicResponse> updateClinic(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID clinicId,
            @Valid @RequestBody ClinicUpdateRequest request) {

        UUID tenantId = principal.getTenantId();

        ClinicResponse response =
                clinicService.updateClinic(
                        tenantId,
                        clinicId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    @PatchMapping("/{clinicId}/status")
    public ResponseEntity<ClinicResponse> updateClinicStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID clinicId,
            @Valid @RequestBody ClinicStatusUpdateRequest request) {

        UUID tenantId = principal.getTenantId();

        ClinicResponse response =
                clinicService.updateClinicStatus(
                        tenantId,
                        clinicId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}
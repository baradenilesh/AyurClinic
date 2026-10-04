package com.ayurclinic.doctor.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.doctor.dto.CreateDoctorRequest;
import com.ayurclinic.doctor.dto.DoctorResponse;
import com.ayurclinic.doctor.dto.UpdateDoctorRequest;
import com.ayurclinic.doctor.dto.UpdateDoctorStatusRequest;
import com.ayurclinic.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse createDoctor(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateDoctorRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        return doctorService.createDoctor(
                tenantId,
                request
        );
    }

    @GetMapping("/{id}")
    public DoctorResponse getDoctor(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID tenantId = principal.getTenantId();

        return doctorService.getDoctor(
                tenantId,
                id
        );
    }

    @GetMapping
    public List<DoctorResponse> getDoctors(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) UUID clinicId
    ) {
        UUID tenantId = principal.getTenantId();

        if (clinicId != null) {
            return doctorService.getDoctorsByClinic(
                    tenantId,
                    clinicId
            );
        }

        return doctorService.getDoctors(tenantId);
    }

    @PutMapping("/{id}")
    public DoctorResponse updateDoctor(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDoctorRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        return doctorService.updateDoctor(
                tenantId,
                id,
                request
        );
    }

    @PatchMapping("/{id}/status")
    public DoctorResponse updateDoctorStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDoctorStatusRequest request
    ) {
        UUID tenantId = principal.getTenantId();

        return doctorService.updateDoctorStatus(
                tenantId,
                id,
                request
        );
    }
}
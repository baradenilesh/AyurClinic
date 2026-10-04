package com.ayurclinic.doctor.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.doctor.dto.CreateDoctorAvailabilityRequest;
import com.ayurclinic.doctor.dto.DoctorAvailabilityResponse;
import com.ayurclinic.doctor.dto.UpdateDoctorAvailabilityRequest;
import com.ayurclinic.doctor.dto.UpdateDoctorAvailabilityStatusRequest;
import com.ayurclinic.doctor.service.DoctorAvailabilityService;
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
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    @PostMapping("/{doctorId}/availability")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorAvailabilityResponse createAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID doctorId,
            @Valid @RequestBody CreateDoctorAvailabilityRequest request) {

        UUID tenantId = principal.getTenantId();

        // Ensure the path doctorId is the doctor being configured.
        request.setDoctorId(doctorId);

        return doctorAvailabilityService.createAvailability(
                tenantId,
                request
        );
    }

    @GetMapping("/{doctorId}/availability")
    public List<DoctorAvailabilityResponse> getAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID doctorId) {

        UUID tenantId = principal.getTenantId();

        return doctorAvailabilityService.getAvailability(
                tenantId,
                doctorId
        );
    }

    @GetMapping("/availability/{availabilityId}")
    public DoctorAvailabilityResponse getAvailabilityById(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID availabilityId) {

        UUID tenantId = principal.getTenantId();

        return doctorAvailabilityService.getAvailabilityById(
                tenantId,
                availabilityId
        );
    }

    @PutMapping("/availability/{availabilityId}")
    public DoctorAvailabilityResponse updateAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID availabilityId,
            @Valid @RequestBody UpdateDoctorAvailabilityRequest request) {

        UUID tenantId = principal.getTenantId();

        return doctorAvailabilityService.updateAvailability(
                tenantId,
                availabilityId,
                request
        );
    }

    @PatchMapping("/availability/{availabilityId}/status")
    public DoctorAvailabilityResponse updateAvailabilityStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID availabilityId,
            @Valid @RequestBody UpdateDoctorAvailabilityStatusRequest request) {

        UUID tenantId = principal.getTenantId();

        return doctorAvailabilityService.updateAvailabilityStatus(
                tenantId,
                availabilityId,
                request
        );
    }
}
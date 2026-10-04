package com.ayurclinic.followup.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.followup.dto.FollowUpCreateRequest;
import com.ayurclinic.followup.dto.FollowUpResponse;
import com.ayurclinic.followup.dto.FollowUpUpdateRequest;
import com.ayurclinic.followup.service.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/followups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<FollowUpResponse> createFollowUp(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody FollowUpCreateRequest request
    ) {

        FollowUpResponse response =
                followUpService.createFollowUp(
                        principal.getTenantId(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<FollowUpResponse> getFollowUp(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                followUpService.getFollowUp(
                        principal.getTenantId(),
                        id
                )
        );
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<List<FollowUpResponse>>
    getPatientFollowUps(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID patientId
    ) {

        return ResponseEntity.ok(
                followUpService.getPatientFollowUps(
                        principal.getTenantId(),
                        patientId
                )
        );
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<List<FollowUpResponse>>
    getDoctorFollowUps(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID doctorId
    ) {

        return ResponseEntity.ok(
                followUpService.getDoctorFollowUps(
                        principal.getTenantId(),
                        doctorId
                )
        );
    }

    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<List<FollowUpResponse>>
    getClinicFollowUps(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID clinicId
    ) {

        return ResponseEntity.ok(
                followUpService.getClinicFollowUps(
                        principal.getTenantId(),
                        clinicId
                )
        );
    }

    @GetMapping("/date/{date}")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<List<FollowUpResponse>>
    getFollowUpsByDate(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable LocalDate date
    ) {

        return ResponseEntity.ok(
                followUpService.getFollowUpsByDate(
                        principal.getTenantId(),
                        date
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<FollowUpResponse> updateFollowUp(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody FollowUpUpdateRequest request
    ) {

        return ResponseEntity.ok(
                followUpService.updateFollowUp(
                        principal.getTenantId(),
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<FollowUpResponse> completeFollowUp(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                followUpService.completeFollowUp(
                        principal.getTenantId(),
                        id
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<FollowUpResponse> cancelFollowUp(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                followUpService.cancelFollowUp(
                        principal.getTenantId(),
                        id
                )
        );
    }

    @PatchMapping("/{id}/missed")
    @PreAuthorize(
            "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<FollowUpResponse> markFollowUpMissed(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                followUpService.markFollowUpMissed(
                        principal.getTenantId(),
                        id
                )
        );
    }
}
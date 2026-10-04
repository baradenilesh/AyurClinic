package com.ayurclinic.patient.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.patient.dto.PatientHistoryRequest;
import com.ayurclinic.patient.dto.PatientHistoryResponse;
import com.ayurclinic.patient.service.PatientHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients/{patientId}/history")
@RequiredArgsConstructor
public class PatientHistoryController {

    private final PatientHistoryService patientHistoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientHistoryResponse addHistory(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID patientId,
            @Valid @RequestBody PatientHistoryRequest request
    ) {

        UUID tenantId = principal.getTenantId();

        return patientHistoryService.addHistory(
                tenantId,
                patientId,
                request
        );
    }

    @GetMapping
    public List<PatientHistoryResponse> getHistory(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID patientId
    ) {

        UUID tenantId = principal.getTenantId();

        return patientHistoryService.getHistory(
                tenantId,
                patientId
        );
    }
}
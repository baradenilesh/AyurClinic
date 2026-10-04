package com.ayurclinic.consultation.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.consultation.dto.ConsultationCreateRequest;
import com.ayurclinic.consultation.dto.ConsultationResponse;
import com.ayurclinic.consultation.dto.ConsultationUpdateRequest;
import com.ayurclinic.consultation.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultationResponse createConsultation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ConsultationCreateRequest request) {

        UUID tenantId = principal.getTenantId();

        return consultationService.createConsultation(
                tenantId,
                request
        );
    }

    @GetMapping("/{id}")
    public ConsultationResponse getConsultationById(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID consultationId) {

        UUID tenantId = principal.getTenantId();

        return consultationService.getConsultationById(
                tenantId,
                consultationId
        );
    }

    @GetMapping("/appointment/{appointmentId}")
    public ConsultationResponse getByAppointmentId(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID appointmentId) {

        UUID tenantId = principal.getTenantId();

        return consultationService.getByAppointmentId(
                tenantId,
                appointmentId
        );
    }

    @GetMapping("/patient/{patientId}")
    public List<ConsultationResponse> getPatientConsultations(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID patientId) {

        UUID tenantId = principal.getTenantId();

        return consultationService.getPatientConsultations(
                tenantId,
                patientId
        );
    }

    @GetMapping("/doctor/{doctorId}")
    public List<ConsultationResponse> getDoctorConsultations(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID doctorId) {

        UUID tenantId = principal.getTenantId();

        return consultationService.getDoctorConsultations(
                tenantId,
                doctorId
        );
    }

    @GetMapping("/clinic/{clinicId}")
    public List<ConsultationResponse> getClinicConsultations(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID clinicId) {

        UUID tenantId = principal.getTenantId();

        return consultationService.getClinicConsultations(
                tenantId,
                clinicId
        );
    }

    @PutMapping("/{id}")
    public ConsultationResponse updateConsultation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID consultationId,
            @Valid @RequestBody ConsultationUpdateRequest request) {

        UUID tenantId = principal.getTenantId();

        return consultationService.updateConsultation(
                tenantId,
                consultationId,
                request
        );
    }

    @PatchMapping("/{id}/complete")
    public ConsultationResponse completeConsultation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID consultationId) {

        UUID tenantId = principal.getTenantId();

        return consultationService.completeConsultation(
                tenantId,
                consultationId
        );
    }
}
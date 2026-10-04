package com.ayurclinic.prescription.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.prescription.dto.*;
import com.ayurclinic.prescription.service.PrescriptionPdfService;
import com.ayurclinic.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionPdfService prescriptionPdfService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrescriptionResponse createPrescription(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody PrescriptionCreateRequest request
    ) {
        return prescriptionService.createPrescription(
                principal.getTenantId(),
                request
        );
    }

    @GetMapping("/{id}")
    public PrescriptionResponse getPrescriptionById(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID prescriptionId
    ) {
        return prescriptionService.getPrescriptionById(
                principal.getTenantId(),
                prescriptionId
        );
    }

    @GetMapping("/consultation/{consultationId}")
    public PrescriptionResponse getByConsultationId(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID consultationId
    ) {
        return prescriptionService.getByConsultationId(
                principal.getTenantId(),
                consultationId
        );
    }

    @GetMapping("/patient/{patientId}")
    public List<PrescriptionResponse> getPatientPrescriptions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID patientId
    ) {
        return prescriptionService.getPatientPrescriptions(
                principal.getTenantId(),
                patientId
        );
    }

    @GetMapping("/doctor/{doctorId}")
    public List<PrescriptionResponse> getDoctorPrescriptions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID doctorId
    ) {
        return prescriptionService.getDoctorPrescriptions(
                principal.getTenantId(),
                doctorId
        );
    }

    @GetMapping("/clinic/{clinicId}")
    public List<PrescriptionResponse> getClinicPrescriptions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID clinicId
    ) {
        return prescriptionService.getClinicPrescriptions(
                principal.getTenantId(),
                clinicId
        );
    }

    @PutMapping("/{id}")
    public PrescriptionResponse updatePrescription(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID prescriptionId,
            @Valid @RequestBody PrescriptionUpdateRequest request
    ) {
        return prescriptionService.updatePrescription(
                principal.getTenantId(),
                prescriptionId,
                request
        );
    }

    @PatchMapping("/{id}/issue")
    public PrescriptionResponse issuePrescription(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID prescriptionId
    ) {
        return prescriptionService.issuePrescription(
                principal.getTenantId(),
                prescriptionId
        );
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID prescriptionId
    ) {
        byte[] pdf =
                prescriptionPdfService.generatePrescriptionPdf(
                        principal.getTenantId(),
                        prescriptionId
                );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(
                                "prescription-" +
                                        prescriptionId +
                                        ".pdf"
                        )
                        .build()
        );

        headers.setContentLength(
                pdf.length
        );

        return new ResponseEntity<>(
                pdf,
                headers,
                HttpStatus.OK
        );
    }
}
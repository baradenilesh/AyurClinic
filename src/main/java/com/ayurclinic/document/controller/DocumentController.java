package com.ayurclinic.document.controller;

import com.ayurclinic.document.dto.DocumentResponse;
import com.ayurclinic.document.dto.DocumentUploadRequest;
import com.ayurclinic.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload a document.
     *
     * Multipart fields:
     * - clinicId
     * - patientId
     * - consultationId (optional)
     * - followupId (optional)
     * - documentType
     * - description (optional)
     * - file
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentResponse> uploadDocument(
            Authentication authentication,
            @RequestPart("request")
            DocumentUploadRequest request,
            @RequestPart("file")
            MultipartFile file
    ) {

        UUID tenantId =
                getTenantId(authentication);

        UUID userId =
                getUserId(authentication);

        DocumentResponse response =
                documentService.uploadDocument(
                        tenantId,
                        userId,
                        request,
                        file
                );

        return ResponseEntity
                .status(201)
                .body(response);
    }

    /**
     * Get a document by ID.
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            Authentication authentication,
            @PathVariable UUID documentId
    ) {

        UUID tenantId =
                getTenantId(authentication);

        return ResponseEntity.ok(
                documentService.getDocument(
                        tenantId,
                        documentId
                )
        );
    }

    /**
     * Get all documents belonging to the current tenant.
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments(
            Authentication authentication
    ) {

        UUID tenantId =
                getTenantId(authentication);

        return ResponseEntity.ok(
                documentService.getAllDocuments(
                        tenantId
                )
        );
    }

    /**
     * Get documents belonging to a patient.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DocumentResponse>> getPatientDocuments(
            Authentication authentication,
            @PathVariable UUID patientId
    ) {

        UUID tenantId =
                getTenantId(authentication);

        return ResponseEntity.ok(
                documentService.getPatientDocuments(
                        tenantId,
                        patientId
                )
        );
    }

    /**
     * Get documents belonging to a consultation.
     */
    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<List<DocumentResponse>> getConsultationDocuments(
            Authentication authentication,
            @PathVariable UUID consultationId
    ) {

        UUID tenantId =
                getTenantId(authentication);

        return ResponseEntity.ok(
                documentService.getConsultationDocuments(
                        tenantId,
                        consultationId
                )
        );
    }

    /**
     * Get documents belonging to a follow-up.
     */
    @GetMapping("/followup/{followupId}")
    public ResponseEntity<List<DocumentResponse>> getFollowupDocuments(
            Authentication authentication,
            @PathVariable UUID followupId
    ) {

        UUID tenantId =
                getTenantId(authentication);

        return ResponseEntity.ok(
                documentService.getFollowupDocuments(
                        tenantId,
                        followupId
                )
        );
    }

    /**
     * Get documents belonging to a clinic.
     */
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<DocumentResponse>> getClinicDocuments(
            Authentication authentication,
            @PathVariable UUID clinicId
    ) {

        UUID tenantId =
                getTenantId(authentication);

        return ResponseEntity.ok(
                documentService.getClinicDocuments(
                        tenantId,
                        clinicId
                )
        );
    }

    /**
     * Download a document.
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<InputStreamResource> downloadDocument(
            Authentication authentication,
            @PathVariable UUID documentId
    ) {

        UUID tenantId =
                getTenantId(authentication);

        DocumentResponse document =
                documentService.getDocument(
                        tenantId,
                        documentId
                );

        InputStream inputStream =
                documentService.downloadDocument(
                        tenantId,
                        documentId
                );

        InputStreamResource resource =
                new InputStreamResource(
                        inputStream
                );

        MediaType mediaType =
                resolveMediaType(
                        document.getContentType()
                );

        ContentDisposition contentDisposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                document.getOriginalFileName()
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(
                        document.getFileSize()
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(resource);
    }

    /**
     * Delete a document.
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            Authentication authentication,
            @PathVariable UUID documentId
    ) {

        UUID tenantId =
                getTenantId(authentication);

        documentService.deleteDocument(
                tenantId,
                documentId
        );

        return ResponseEntity.noContent()
                .build();
    }

    private UUID getTenantId(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getPrincipal() == null) {

            throw new IllegalArgumentException(
                    "Authenticated user is required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        try {

            return (UUID)
                    principal
                            .getClass()
                            .getMethod("getTenantId")
                            .invoke(principal);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to determine tenant ID",
                    ex
            );
        }
    }

    private UUID getUserId(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getPrincipal() == null) {

            throw new IllegalArgumentException(
                    "Authenticated user is required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        try {

            return (UUID)
                    principal
                            .getClass()
                            .getMethod("getUserId")
                            .invoke(principal);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to determine user ID",
                    ex
            );
        }
    }

    private MediaType resolveMediaType(
            String contentType
    ) {

        if (contentType == null ||
                contentType.isBlank()) {

            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {

            return MediaType.parseMediaType(
                    contentType
            );

        } catch (IllegalArgumentException ex) {

            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}

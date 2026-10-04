package com.ayurclinic.document.service;

import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.document.config.DocumentStorageProperties;
import com.ayurclinic.document.dto.DocumentResponse;
import com.ayurclinic.document.dto.DocumentUploadRequest;
import com.ayurclinic.document.entity.Document;
import com.ayurclinic.document.repository.DocumentRepository;
import com.ayurclinic.document.storage.DocumentStorageService;
import com.ayurclinic.followup.entity.FollowUp;
import com.ayurclinic.followup.repository.FollowUpRepository;
import com.ayurclinic.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final DocumentStorageService documentStorageService;

    private final DocumentStorageProperties storageProperties;

    private final ClinicRepository clinicRepository;

    private final PatientRepository patientRepository;

    private final ConsultationRepository consultationRepository;

    private final FollowUpRepository followUpRepository;

    public DocumentResponse uploadDocument(
            UUID tenantId,
            UUID userId,
            DocumentUploadRequest request,
            MultipartFile file
    ) {

        validateRequest(
                tenantId,
                request,
                file
        );

        validateTenantOwnership(
                tenantId,
                request
        );

        validateFile(file);

        String originalFileName =
                sanitizeFileName(
                        file.getOriginalFilename()
                );

        String contentType =
                file.getContentType();

        if (contentType == null ||
                contentType.isBlank()) {

            contentType =
                    "application/octet-stream";
        }

        UUID documentId =
                UUID.randomUUID();

        String storageKey =
                buildStorageKey(
                        tenantId,
                        request.getPatientId(),
                        documentId,
                        originalFileName
                );

        try {

            documentStorageService.store(
                    file,
                    storageKey
            );

            Document document =
                    new Document();

            document.setId(documentId);
            document.setTenantId(tenantId);
            document.setClinicId(
                    request.getClinicId()
            );
            document.setPatientId(
                    request.getPatientId()
            );
            document.setConsultationId(
                    request.getConsultationId()
            );
            document.setFollowupId(
                    request.getFollowupId()
            );
            document.setDocumentType(
                    request.getDocumentType()
            );
            document.setOriginalFileName(
                    originalFileName
            );
            document.setStorageKey(
                    storageKey
            );
            document.setContentType(
                    contentType
            );
            document.setFileSize(
                    file.getSize()
            );
            document.setDescription(
                    request.getDescription()
            );
            document.setUploadedBy(
                    userId
            );

            Document saved =
                    documentRepository.save(
                            document
                    );

            return toResponse(saved);

        } catch (IOException ex) {

            try {
                documentStorageService.delete(
                        storageKey
                );
            } catch (IOException ignored) {
                // Original storage failure is more important.
            }

            throw new IllegalStateException(
                    "Unable to store document",
                    ex
            );
        }
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(
            UUID tenantId,
            UUID documentId
    ) {

        Document document =
                getEntity(
                        tenantId,
                        documentId
                );

        return toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments(
            UUID tenantId
    ) {

        return documentRepository
                .findByTenantIdOrderByCreatedAtDesc(
                        tenantId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getPatientDocuments(
            UUID tenantId,
            UUID patientId
    ) {

        return documentRepository
                .findByTenantIdAndPatientIdOrderByCreatedAtDesc(
                        tenantId,
                        patientId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getConsultationDocuments(
            UUID tenantId,
            UUID consultationId
    ) {

        return documentRepository
                .findByTenantIdAndConsultationIdOrderByCreatedAtDesc(
                        tenantId,
                        consultationId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getFollowupDocuments(
            UUID tenantId,
            UUID followupId
    ) {

        return documentRepository
                .findByTenantIdAndFollowupIdOrderByCreatedAtDesc(
                        tenantId,
                        followupId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getClinicDocuments(
            UUID tenantId,
            UUID clinicId
    ) {

        return documentRepository
                .findByTenantIdAndClinicIdOrderByCreatedAtDesc(
                        tenantId,
                        clinicId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InputStream downloadDocument(
            UUID tenantId,
            UUID documentId
    ) {

        Document document =
                getEntity(
                        tenantId,
                        documentId
                );

        try {

            return documentStorageService.load(
                    document.getStorageKey()
            );

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Unable to read document",
                    ex
            );
        }
    }

    public void deleteDocument(
            UUID tenantId,
            UUID documentId
    ) {

        Document document =
                getEntity(
                        tenantId,
                        documentId
                );

        try {

            documentStorageService.delete(
                    document.getStorageKey()
            );

            documentRepository.delete(
                    document
            );

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Unable to delete document",
                    ex
            );
        }
    }

    private void validateTenantOwnership(
            UUID tenantId,
            DocumentUploadRequest request
    ) {

        clinicRepository
                .findByIdAndTenantId(
                        request.getClinicId(),
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Clinic not found"
                        )
                );

        patientRepository
                .findByIdAndTenantId(
                        request.getPatientId(),
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Patient not found"
                        )
                );

        if (request.getConsultationId() != null) {

            Consultation consultation =
                    consultationRepository
                            .findByIdAndTenantId(
                                    request.getConsultationId(),
                                    tenantId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Consultation not found"
                                    )
                            );

            if (!request.getPatientId()
                    .equals(consultation.getPatientId())) {

                throw new IllegalArgumentException(
                        "Consultation does not belong to the patient"
                );
            }

            if (!request.getClinicId()
                    .equals(consultation.getClinicId())) {

                throw new IllegalArgumentException(
                        "Consultation does not belong to the clinic"
                );
            }
        }

        if (request.getFollowupId() != null) {

            FollowUp followUp =
                    followUpRepository
                            .findByIdAndTenantId(
                                    request.getFollowupId(),
                                    tenantId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Follow-up not found"
                                    )
                            );

            if (!request.getPatientId()
                    .equals(followUp.getPatientId())) {

                throw new IllegalArgumentException(
                        "Follow-up does not belong to the patient"
                );
            }

            if (!request.getClinicId()
                    .equals(followUp.getClinicId())) {

                throw new IllegalArgumentException(
                        "Follow-up does not belong to the clinic"
                );
            }
        }
    }

    private Document getEntity(
            UUID tenantId,
            UUID documentId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }

        return documentRepository
                .findByIdAndTenantId(
                        documentId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Document not found"
                        )
                );
    }

    private void validateRequest(
            UUID tenantId,
            DocumentUploadRequest request,
            MultipartFile file
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Document request is required"
            );
        }

        if (request.getClinicId() == null) {
            throw new IllegalArgumentException(
                    "Clinic ID is required"
            );
        }

        if (request.getPatientId() == null) {
            throw new IllegalArgumentException(
                    "Patient ID is required"
            );
        }

        if (request.getDocumentType() == null ||
                request.getDocumentType().isBlank()) {

            throw new IllegalArgumentException(
                    "Document type is required"
            );
        }

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "File is required"
            );
        }
    }

    private void validateFile(
            MultipartFile file
    ) {

        if (file.getSize() >
                storageProperties.getMaxFileSize()) {

            throw new IllegalArgumentException(
                    "File size exceeds the maximum allowed size of "
                            + storageProperties.getMaxFileSize()
                            + " bytes"
            );
        }
    }

    private String sanitizeFileName(
            String fileName
    ) {

        if (fileName == null ||
                fileName.isBlank()) {

            return "document";
        }

        String sanitized =
                java.nio.file.Paths
                        .get(fileName)
                        .getFileName()
                        .toString();

        return sanitized
                .replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );
    }

    private String buildStorageKey(
            UUID tenantId,
            UUID patientId,
            UUID documentId,
            String fileName
    ) {

        return tenantId
                + "/"
                + patientId
                + "/"
                + documentId
                + "-"
                + fileName;
    }

    private DocumentResponse toResponse(
            Document document
    ) {

        return DocumentResponse.builder()
                .id(document.getId())
                .tenantId(document.getTenantId())
                .clinicId(document.getClinicId())
                .patientId(document.getPatientId())
                .consultationId(
                        document.getConsultationId()
                )
                .followupId(
                        document.getFollowupId()
                )
                .documentType(
                        document.getDocumentType()
                )
                .originalFileName(
                        document.getOriginalFileName()
                )
                .contentType(
                        document.getContentType()
                )
                .fileSize(
                        document.getFileSize()
                )
                .description(
                        document.getDescription()
                )
                .uploadedBy(
                        document.getUploadedBy()
                )
                .createdAt(
                        document.getCreatedAt()
                )
                .updatedAt(
                        document.getUpdatedAt()
                )
                .build();
    }
}

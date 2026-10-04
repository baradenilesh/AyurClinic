package com.ayurclinic.document.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class DocumentResponse {

    private UUID id;

    private UUID tenantId;

    private UUID clinicId;

    private UUID patientId;

    private UUID consultationId;

    private UUID followupId;

    private String documentType;

    private String originalFileName;

    private String contentType;

    private Long fileSize;

    private String description;

    private UUID uploadedBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}

package com.ayurclinic.document.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DocumentUploadRequest {

    private UUID clinicId;

    private UUID patientId;

    private UUID consultationId;

    private UUID followupId;

    private String documentType;

    private String description;
}


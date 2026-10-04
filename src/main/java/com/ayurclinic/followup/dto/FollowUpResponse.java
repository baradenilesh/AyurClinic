package com.ayurclinic.followup.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class FollowUpResponse {

    private UUID id;

    private UUID tenantId;

    private UUID clinicId;

    private UUID patientId;

    private UUID doctorId;

    private UUID consultationId;

    private UUID appointmentId;

    private LocalDate followUpDate;

    private String reason;

    private String notes;

    private String status;

    private boolean reminderSent;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}

package com.ayurclinic.followup.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class FollowUpCreateRequest {

    @NotNull
    private UUID consultationId;

    private UUID appointmentId;

    @NotNull
    @FutureOrPresent
    private LocalDate followUpDate;

    @Size(max = 500)
    private String reason;

    private String notes;
}
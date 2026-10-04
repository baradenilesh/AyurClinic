package com.ayurclinic.followup.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FollowUpUpdateRequest {

    @FutureOrPresent
    private LocalDate followUpDate;

    @Size(max = 500)
    private String reason;

    private String notes;
}
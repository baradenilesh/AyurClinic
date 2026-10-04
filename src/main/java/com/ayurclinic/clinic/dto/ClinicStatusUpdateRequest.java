package com.ayurclinic.clinic.dto;

import com.ayurclinic.clinic.entity.ClinicStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicStatusUpdateRequest {

    @NotNull
    private ClinicStatus status;
}
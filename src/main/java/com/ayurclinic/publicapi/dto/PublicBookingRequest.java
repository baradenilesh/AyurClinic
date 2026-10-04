package com.ayurclinic.publicapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record PublicBookingRequest(

        @NotNull
        UUID doctorId,

        @NotNull
        LocalDate appointmentDate,

        @NotNull
        LocalTime startTime,

        @NotBlank
        String patientName,

        @NotBlank
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Mobile number must contain exactly 10 digits"
        )
        String mobile,

        @Email
        String email
) {
}
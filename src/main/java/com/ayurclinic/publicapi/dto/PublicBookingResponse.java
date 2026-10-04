package com.ayurclinic.publicapi.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class PublicBookingResponse {

    private String bookingReference;

    private String doctorName;

    private LocalDate appointmentDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String status;
}
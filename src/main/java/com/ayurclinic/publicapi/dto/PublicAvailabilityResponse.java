package com.ayurclinic.publicapi.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class PublicAvailabilityResponse {

    private Short dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer slotDurationMinutes;
}
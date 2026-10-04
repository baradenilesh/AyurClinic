package com.ayurclinic.publicapi.controller;

import com.ayurclinic.publicapi.dto.PublicAvailabilityResponse;
import com.ayurclinic.publicapi.service.PublicAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/doctors")
@RequiredArgsConstructor
public class PublicAvailabilityController {

    private final PublicAvailabilityService publicAvailabilityService;

    @GetMapping("/{doctorId}/availability")
    public List<PublicAvailabilityResponse> getDoctorAvailability(
            @PathVariable UUID doctorId
    ) {
        return publicAvailabilityService.getDoctorAvailability(doctorId);
    }
}
package com.ayurclinic.publicapi.controller;

import com.ayurclinic.publicapi.dto.PublicDoctorResponse;
import com.ayurclinic.publicapi.service.PublicDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/doctors")
@RequiredArgsConstructor
public class PublicDoctorController {

    private final PublicDoctorService publicDoctorService;

    @GetMapping
    public List<PublicDoctorResponse> getPublicDoctors() {
        return publicDoctorService.getPublicDoctors();
    }

    @GetMapping("/{doctorId}")
    public PublicDoctorResponse getPublicDoctor(
            @PathVariable UUID doctorId
    ) {
        return publicDoctorService.getPublicDoctor(doctorId);
    }
}
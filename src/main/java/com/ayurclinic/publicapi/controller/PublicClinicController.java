package com.ayurclinic.publicapi.controller;

import com.ayurclinic.publicapi.dto.PublicClinicResponse;
import com.ayurclinic.publicapi.service.PublicClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/clinic")
@RequiredArgsConstructor
public class PublicClinicController {

    private final PublicClinicService publicClinicService;

    @GetMapping
    public PublicClinicResponse getPublicClinic() {
        return publicClinicService.getPublicClinic();
    }
}
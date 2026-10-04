package com.ayurclinic.publicapi.controller;

import com.ayurclinic.publicapi.dto.PublicSlotResponse;
import com.ayurclinic.publicapi.service.PublicSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/doctors")
@RequiredArgsConstructor
public class PublicSlotController {

    private final PublicSlotService publicSlotService;

    @GetMapping("/{doctorId}/slots")
    public List<PublicSlotResponse> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return publicSlotService.getAvailableSlots(
                doctorId,
                date
        );
    }
}
package com.ayurclinic.publicapi.controller;

import com.ayurclinic.publicapi.dto.PublicBookingRequest;
import com.ayurclinic.publicapi.dto.PublicBookingResponse;
import com.ayurclinic.publicapi.service.PublicBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/appointments")
@RequiredArgsConstructor
public class PublicBookingController {

    private final PublicBookingService publicBookingService;

    @PostMapping
    public PublicBookingResponse createBooking(
            @Valid @RequestBody PublicBookingRequest request
    ) {
        return publicBookingService.createBooking(request);
    }

    @GetMapping("/{bookingReference}")
    public PublicBookingResponse getBooking(
            @PathVariable String bookingReference
    ) {
        return publicBookingService.getBookingByReference(
                bookingReference
        );
    }
}
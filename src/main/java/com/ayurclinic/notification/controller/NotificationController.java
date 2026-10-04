package com.ayurclinic.notification.controller;

import com.ayurclinic.notification.dto.NotificationCreateRequest;
import com.ayurclinic.notification.dto.NotificationResponse;
import com.ayurclinic.notification.dto.NotificationUpdateStatusRequest;
import com.ayurclinic.notification.service.NotificationService;
import com.ayurclinic.auth.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize(
        "hasAnyRole('CLINIC_ADMIN','DOCTOR','RECEPTIONIST')"
)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService =
                notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody NotificationCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        notificationService.create(
                                request,
                                principal
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.getAll(principal)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.getById(
                        id,
                        principal
                )
        );
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<NotificationResponse>> getByPatient(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.getByPatient(
                        patientId,
                        principal
                )
        );
    }

    @GetMapping("/followup/{followupId}")
    public ResponseEntity<List<NotificationResponse>> getByFollowup(
            @PathVariable UUID followupId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.getByFollowup(
                        followupId,
                        principal
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<NotificationResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationUpdateStatusRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.updateStatus(
                        id,
                        request,
                        principal
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<NotificationResponse> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                notificationService.cancel(
                        id,
                        principal
                )
        );
    }
}
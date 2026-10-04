package com.ayurclinic.notification.service;

import com.ayurclinic.notification.dto.NotificationCreateRequest;
import com.ayurclinic.notification.dto.NotificationResponse;
import com.ayurclinic.notification.dto.NotificationUpdateStatusRequest;
import com.ayurclinic.notification.entity.Notification;
import com.ayurclinic.notification.enums.NotificationStatus;
import com.ayurclinic.notification.repository.NotificationRepository;
import com.ayurclinic.auth.security.CustomUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse create(
            NotificationCreateRequest request,
            CustomUserPrincipal principal
    ) {
        Notification notification = new Notification();

        notification.setTenantId(principal.getTenantId());
        notification.setClinicId(request.getClinicId());
        notification.setPatientId(request.getPatientId());
        notification.setFollowupId(request.getFollowupId());

        notification.setType(request.getType());
        notification.setChannel(request.getChannel());

        notification.setRecipient(
                request.getRecipient().trim()
        );

        notification.setSubject(
                request.getSubject()
        );

        notification.setMessage(
                request.getMessage().trim()
        );

        notification.setScheduledAt(
                request.getScheduledAt()
        );

        notification.setStatus(
                NotificationStatus.PENDING
        );

        return toResponse(
                notificationRepository.save(notification)
        );
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(
            UUID id,
            CustomUserPrincipal principal
    ) {
        Notification notification =
                notificationRepository
                        .findByIdAndTenantId(
                                id,
                                principal.getTenantId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );

        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll(
            CustomUserPrincipal principal
    ) {
        return notificationRepository
                .findByTenantIdOrderByCreatedAtDesc(
                        principal.getTenantId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByPatient(
            UUID patientId,
            CustomUserPrincipal principal
    ) {
        return notificationRepository
                .findByPatientIdAndTenantIdOrderByCreatedAtDesc(
                        patientId,
                        principal.getTenantId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByFollowup(
            UUID followupId,
            CustomUserPrincipal principal
    ) {
        return notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followupId,
                        principal.getTenantId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationResponse updateStatus(
            UUID id,
            NotificationUpdateStatusRequest request,
            CustomUserPrincipal principal
    ) {
        Notification notification =
                notificationRepository
                        .findByIdAndTenantId(
                                id,
                                principal.getTenantId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );

        NotificationStatus status =
                request.getStatus();

        notification.setStatus(status);

        if (request.getProviderMessageId() != null) {
            notification.setProviderMessageId(
                    request.getProviderMessageId()
            );
        }

        if (request.getFailureReason() != null) {
            notification.setFailureReason(
                    request.getFailureReason()
            );
        }

        if (status == NotificationStatus.SENT) {
            notification.setSentAt(
                    OffsetDateTime.now()
            );
        }

        return toResponse(
                notificationRepository.save(notification)
        );
    }

    public NotificationResponse cancel(
            UUID id,
            CustomUserPrincipal principal
    ) {
        Notification notification =
                notificationRepository
                        .findByIdAndTenantId(
                                id,
                                principal.getTenantId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );

        if (notification.getStatus()
                != NotificationStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending notifications can be cancelled"
            );
        }

        notification.setStatus(
                NotificationStatus.CANCELLED
        );

        return toResponse(
                notificationRepository.save(notification)
        );
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {
        NotificationResponse response =
                new NotificationResponse();

        response.setId(notification.getId());
        response.setTenantId(
                notification.getTenantId()
        );
        response.setClinicId(
                notification.getClinicId()
        );
        response.setPatientId(
                notification.getPatientId()
        );
        response.setFollowupId(
                notification.getFollowupId()
        );

        response.setType(
                notification.getType()
        );
        response.setChannel(
                notification.getChannel()
        );

        response.setRecipient(
                notification.getRecipient()
        );
        response.setSubject(
                notification.getSubject()
        );
        response.setMessage(
                notification.getMessage()
        );

        response.setScheduledAt(
                notification.getScheduledAt()
        );
        response.setSentAt(
                notification.getSentAt()
        );

        response.setStatus(
                notification.getStatus()
        );

        response.setProviderMessageId(
                notification.getProviderMessageId()
        );

        response.setFailureReason(
                notification.getFailureReason()
        );

        response.setCreatedAt(
                notification.getCreatedAt()
        );
        response.setUpdatedAt(
                notification.getUpdatedAt()
        );

        return response;
    }
}
package com.ayurclinic.notification.repository;

import com.ayurclinic.notification.entity.Notification;
import com.ayurclinic.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<Notification> findByTenantIdOrderByCreatedAtDesc(
            UUID tenantId
    );

    List<Notification> findByPatientIdAndTenantIdOrderByCreatedAtDesc(
            UUID patientId,
            UUID tenantId
    );

    List<Notification> findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
            UUID followupId,
            UUID tenantId
    );

    List<Notification> findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
            NotificationStatus status,
            OffsetDateTime scheduledAt
    );
}
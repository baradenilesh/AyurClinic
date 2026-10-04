package com.ayurclinic.notification.service;

import com.ayurclinic.notification.entity.Notification;
import com.ayurclinic.notification.enums.NotificationChannel;
import com.ayurclinic.notification.enums.NotificationStatus;
import com.ayurclinic.notification.enums.NotificationType;
import com.ayurclinic.notification.provider.NotificationProvider;
import com.ayurclinic.notification.provider.NotificationProviderResult;
import com.ayurclinic.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationProvider notificationProvider;

    @InjectMocks
    private NotificationDispatcherService dispatcherService;

    private Notification notification;

    @BeforeEach
    void setUp() {

        notification = new Notification();

        notification.setId(UUID.randomUUID());
        notification.setTenantId(UUID.randomUUID());
        notification.setClinicId(UUID.randomUUID());
        notification.setPatientId(UUID.randomUUID());
        notification.setFollowupId(UUID.randomUUID());

        notification.setType(
                NotificationType.FOLLOW_UP_REMINDER
        );

        notification.setChannel(
                NotificationChannel.SMS
        );

        notification.setRecipient(
                "9876543210"
        );

        notification.setMessage(
                "Your follow-up is scheduled."
        );

        notification.setScheduledAt(
                OffsetDateTime.now().minusMinutes(5)
        );

        notification.setStatus(
                NotificationStatus.PENDING
        );
    }

    @Test
    void processNotification_shouldMarkAsSent() {

        NotificationProviderResult result =
                NotificationProviderResult.success(
                        "provider-123"
                );

        when(notificationProvider.send(notification))
                .thenReturn(result);

        dispatcherService.processNotification(notification);

        assertEquals(
                NotificationStatus.SENT,
                notification.getStatus()
        );

        assertEquals(
                "provider-123",
                notification.getProviderMessageId()
        );

        assertNotNull(
                notification.getSentAt()
        );

        verify(notificationProvider)
                .send(notification);

        verify(notificationRepository)
                .save(notification);
    }

    @Test
    void processNotification_shouldMarkAsFailed() {

        NotificationProviderResult result =
                NotificationProviderResult.failure(
                        "Provider unavailable"
                );

        when(notificationProvider.send(notification))
                .thenReturn(result);

        dispatcherService.processNotification(notification);

        assertEquals(
                NotificationStatus.FAILED,
                notification.getStatus()
        );

        assertEquals(
                "Provider unavailable",
                notification.getFailureReason()
        );

        verify(notificationProvider)
                .send(notification);

        verify(notificationRepository)
                .save(notification);
    }

    @Test
    void processNotification_shouldHandleProviderException() {

        when(notificationProvider.send(notification))
                .thenThrow(
                        new RuntimeException(
                                "Provider error"
                        )
                );

        dispatcherService.processNotification(notification);

        assertEquals(
                NotificationStatus.FAILED,
                notification.getStatus()
        );

        assertEquals(
                "Provider error",
                notification.getFailureReason()
        );

        verify(notificationRepository)
                .save(notification);
    }

    @Test
    void processNotification_shouldIgnoreNonPendingNotification() {

        notification.setStatus(
                NotificationStatus.SENT
        );

        dispatcherService.processNotification(notification);

        verify(
                notificationProvider,
                never()
        ).send(any(Notification.class));

        verify(
                notificationRepository,
                never()
        ).save(any(Notification.class));
    }

    @Test
    void processDueNotifications_shouldProcessDueNotifications() {

        Notification notification2 = new Notification();

        notification2.setId(UUID.randomUUID());
        notification2.setTenantId(notification.getTenantId());
        notification2.setClinicId(notification.getClinicId());
        notification2.setPatientId(notification.getPatientId());
        notification2.setType(
                NotificationType.FOLLOW_UP_REMINDER
        );
        notification2.setChannel(
                NotificationChannel.EMAIL
        );
        notification2.setRecipient(
                "patient@example.com"
        );
        notification2.setMessage(
                "Follow-up reminder"
        );
        notification2.setScheduledAt(
                OffsetDateTime.now().minusMinutes(10)
        );
        notification2.setStatus(
                NotificationStatus.PENDING
        );

        when(notificationRepository
                .findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                        eq(NotificationStatus.PENDING),
                        any(OffsetDateTime.class)
                ))
                .thenReturn(
                        List.of(
                                notification,
                                notification2
                        )
                );

        when(notificationProvider.send(any(Notification.class)))
                .thenReturn(
                        NotificationProviderResult.success(
                                "provider-123"
                        )
                );

        dispatcherService.processDueNotifications();

        verify(notificationProvider)
                .send(notification);

        verify(notificationProvider)
                .send(notification2);

        verify(notificationRepository, times(2))
                .save(any(Notification.class));

        assertEquals(
                NotificationStatus.SENT,
                notification.getStatus()
        );

        assertEquals(
                NotificationStatus.SENT,
                notification2.getStatus()
        );
    }
}
package com.ayurclinic.notification.service;

import com.ayurclinic.notification.entity.Notification;
import com.ayurclinic.notification.enums.NotificationChannel;
import com.ayurclinic.notification.enums.NotificationStatus;
import com.ayurclinic.notification.enums.NotificationType;
import com.ayurclinic.notification.repository.NotificationRepository;
import com.ayurclinic.notification.dto.NotificationCreateRequest;
import com.ayurclinic.notification.dto.NotificationResponse;
import com.ayurclinic.notification.dto.NotificationUpdateStatusRequest;
import com.ayurclinic.auth.security.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID tenantId;
    private UUID clinicId;
    private UUID patientId;
    private UUID followupId;
    private UUID notificationId;

    private CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        followupId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        principal = mock(CustomUserPrincipal.class);
        when(principal.getTenantId()).thenReturn(tenantId);
    }

    @Test
    void create_shouldCreatePendingNotification() {

        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setClinicId(clinicId);
        request.setPatientId(patientId);
        request.setFollowupId(followupId);
        request.setType(NotificationType.FOLLOW_UP_REMINDER);
        request.setChannel(NotificationChannel.SMS);
        request.setRecipient("9876543210");
        request.setSubject("Follow-up Reminder");
        request.setMessage("Your follow-up is scheduled.");
        request.setScheduledAt(
                OffsetDateTime.now().plusDays(1)
        );

        Notification savedNotification = new Notification();
        savedNotification.setId(notificationId);
        savedNotification.setTenantId(tenantId);
        savedNotification.setClinicId(clinicId);
        savedNotification.setPatientId(patientId);
        savedNotification.setFollowupId(followupId);
        savedNotification.setType(NotificationType.FOLLOW_UP_REMINDER);
        savedNotification.setChannel(NotificationChannel.SMS);
        savedNotification.setRecipient("9876543210");
        savedNotification.setSubject("Follow-up Reminder");
        savedNotification.setMessage("Your follow-up is scheduled.");
        savedNotification.setScheduledAt(request.getScheduledAt());
        savedNotification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(savedNotification);

        NotificationResponse response =
                notificationService.create(request, principal);

        assertNotNull(response);
        assertEquals(notificationId, response.getId());
        assertEquals(tenantId, response.getTenantId());
        assertEquals(NotificationStatus.PENDING, response.getStatus());
        assertEquals(NotificationChannel.SMS, response.getChannel());

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification captured = captor.getValue();

        assertEquals(tenantId, captured.getTenantId());
        assertEquals(clinicId, captured.getClinicId());
        assertEquals(patientId, captured.getPatientId());
        assertEquals(followupId, captured.getFollowupId());
        assertEquals(NotificationStatus.PENDING, captured.getStatus());
    }

    @Test
    void getById_shouldReturnNotificationForSameTenant() {

        Notification notification = createNotification();

        when(notificationRepository.findByIdAndTenantId(
                notificationId,
                tenantId
        )).thenReturn(Optional.of(notification));

        NotificationResponse response =
                notificationService.getById(notificationId, principal);

        assertNotNull(response);
        assertEquals(notificationId, response.getId());
        assertEquals(tenantId, response.getTenantId());

        verify(notificationRepository)
                .findByIdAndTenantId(notificationId, tenantId);
    }

    @Test
    void getById_shouldThrowWhenNotificationDoesNotExist() {

        when(notificationRepository.findByIdAndTenantId(
                notificationId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> notificationService.getById(
                        notificationId,
                        principal
                )
        );

        verify(notificationRepository)
                .findByIdAndTenantId(notificationId, tenantId);
    }

    @Test
    void getAll_shouldReturnOnlyTenantNotifications() {

        Notification notification = createNotification();

        when(notificationRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                notificationService.getAll(principal);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(notificationId, response.get(0).getId());

        verify(notificationRepository)
                .findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Test
    void getByPatient_shouldReturnPatientNotifications() {

        Notification notification = createNotification();

        when(notificationRepository
                .findByPatientIdAndTenantIdOrderByCreatedAtDesc(
                        patientId,
                        tenantId
                ))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                notificationService.getByPatient(
                        patientId,
                        principal
                );

        assertEquals(1, response.size());
        assertEquals(patientId, response.get(0).getPatientId());

        verify(notificationRepository)
                .findByPatientIdAndTenantIdOrderByCreatedAtDesc(
                        patientId,
                        tenantId
                );
    }

    @Test
    void getByFollowup_shouldReturnFollowupNotifications() {

        Notification notification = createNotification();

        when(notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followupId,
                        tenantId
                ))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                notificationService.getByFollowup(
                        followupId,
                        principal
                );

        assertEquals(1, response.size());
        assertEquals(
                followupId,
                response.get(0).getFollowupId()
        );

        verify(notificationRepository)
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followupId,
                        tenantId
                );
    }

    @Test
    void updateStatus_shouldMarkNotificationAsSent() {

        Notification notification = createNotification();
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.findByIdAndTenantId(
                notificationId,
                tenantId
        )).thenReturn(Optional.of(notification));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationUpdateStatusRequest request =
                new NotificationUpdateStatusRequest();

        request.setStatus(NotificationStatus.SENT);
        request.setProviderMessageId("provider-123");

        NotificationResponse response =
                notificationService.updateStatus(
                        notificationId,
                        request,
                        principal
                );

        assertEquals(NotificationStatus.SENT, response.getStatus());
        assertEquals(
                "provider-123",
                response.getProviderMessageId()
        );
        assertNotNull(response.getSentAt());

        verify(notificationRepository).save(notification);
    }

    @Test
    void cancel_shouldCancelPendingNotification() {

        Notification notification = createNotification();
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.findByIdAndTenantId(
                notificationId,
                tenantId
        )).thenReturn(Optional.of(notification));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response =
                notificationService.cancel(
                        notificationId,
                        principal
                );

        assertEquals(
                NotificationStatus.CANCELLED,
                response.getStatus()
        );

        verify(notificationRepository).save(notification);
    }

    @Test
    void cancel_shouldNotCancelAlreadySentNotification() {

        Notification notification = createNotification();
        notification.setStatus(NotificationStatus.SENT);

        when(notificationRepository.findByIdAndTenantId(
                notificationId,
                tenantId
        )).thenReturn(Optional.of(notification));

        assertThrows(
                RuntimeException.class,
                () -> notificationService.cancel(
                        notificationId,
                        principal
                )
        );

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    private Notification createNotification() {

        Notification notification = new Notification();

        notification.setId(notificationId);
        notification.setTenantId(tenantId);
        notification.setClinicId(clinicId);
        notification.setPatientId(patientId);
        notification.setFollowupId(followupId);
        notification.setType(NotificationType.FOLLOW_UP_REMINDER);
        notification.setChannel(NotificationChannel.SMS);
        notification.setRecipient("9876543210");
        notification.setSubject("Follow-up Reminder");
        notification.setMessage("Your follow-up is scheduled.");
        notification.setScheduledAt(
                OffsetDateTime.now().plusDays(1)
        );
        notification.setStatus(NotificationStatus.PENDING);

        return notification;
    }
}
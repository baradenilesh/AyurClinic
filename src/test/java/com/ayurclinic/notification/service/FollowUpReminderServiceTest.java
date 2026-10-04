package com.ayurclinic.notification.service;

import com.ayurclinic.followup.entity.FollowUp;
import com.ayurclinic.notification.config.NotificationProperties;
import com.ayurclinic.notification.entity.Notification;
import com.ayurclinic.notification.enums.NotificationChannel;
import com.ayurclinic.notification.enums.NotificationStatus;
import com.ayurclinic.notification.enums.NotificationType;
import com.ayurclinic.notification.repository.NotificationRepository;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUpReminderServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PatientRepository patientRepository;

    private NotificationProperties notificationProperties;

    private FollowUpReminderService reminderService;

    private UUID tenantId;
    private UUID clinicId;
    private UUID patientId;
    private UUID doctorId;
    private UUID followUpId;

    @BeforeEach
    void setUp() {
        notificationProperties = new NotificationProperties();
        notificationProperties.setReminderDaysBefore(1);
        notificationProperties.setReminderHour(9);
        notificationProperties.setReminderMinute(0);

        reminderService = new FollowUpReminderService(
                notificationRepository,
                patientRepository,
                notificationProperties
        );

        tenantId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        followUpId = UUID.randomUUID();
    }

    @Test
    void createReminderForFollowUp_shouldCreatePendingSmsReminder() {

        LocalDate followUpDate = LocalDate.of(2026, 10, 10);

        FollowUp followUp = createScheduledFollowUp(followUpDate);

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);
        patient.setMobile("9876543210");

        when(notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUpId,
                        tenantId
                ))
                .thenReturn(List.of());

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(patient));

        reminderService.createReminderForFollowUp(followUp);

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();

        assertEquals(tenantId, notification.getTenantId());
        assertEquals(clinicId, notification.getClinicId());
        assertEquals(patientId, notification.getPatientId());
        assertEquals(followUpId, notification.getFollowupId());

        assertEquals(
                NotificationType.FOLLOW_UP_REMINDER,
                notification.getType()
        );

        assertEquals(
                NotificationChannel.SMS,
                notification.getChannel()
        );

        assertEquals(
                NotificationStatus.PENDING,
                notification.getStatus()
        );

        assertEquals(
                "9876543210",
                notification.getRecipient()
        );

        assertEquals(
                "AyurClinic Follow-up Reminder",
                notification.getSubject()
        );

        assertTrue(
                notification.getMessage().contains("2026-10-10")
        );

        OffsetDateTime expectedScheduledAt =
                OffsetDateTime.of(
                        LocalDate.of(2026, 10, 9),
                        LocalTime.of(9, 0),
                        ZoneOffset.ofHoursMinutes(5, 30)
                );

        assertEquals(
                expectedScheduledAt,
                notification.getScheduledAt()
        );
    }

    @Test
    void createReminderForFollowUp_shouldNotCreateDuplicateReminder() {

        FollowUp followUp =
                createScheduledFollowUp(LocalDate.of(2026, 10, 10));

        Notification existingNotification = new Notification();
        existingNotification.setType(
                NotificationType.FOLLOW_UP_REMINDER
        );

        when(notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUpId,
                        tenantId
                ))
                .thenReturn(List.of(existingNotification));

        reminderService.createReminderForFollowUp(followUp);

        verify(notificationRepository, never())
                .save(any(Notification.class));

        verifyNoInteractions(patientRepository);
    }

    @Test
    void createReminderForFollowUp_shouldNotCreateWhenFollowUpIsNotScheduled() {

        FollowUp followUp =
                createScheduledFollowUp(LocalDate.of(2026, 10, 10));

        followUp.setStatus("COMPLETED");

        reminderService.createReminderForFollowUp(followUp);

        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(patientRepository);
    }

    @Test
    void createReminderForFollowUp_shouldNotCreateWhenPatientHasNoMobile() {

        FollowUp followUp =
                createScheduledFollowUp(LocalDate.of(2026, 10, 10));

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);
        patient.setMobile(null);

        when(notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUpId,
                        tenantId
                ))
                .thenReturn(List.of());

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(patient));

        reminderService.createReminderForFollowUp(followUp);

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void createReminderForFollowUp_shouldNotCreateWhenPatientMobileIsBlank() {

        FollowUp followUp =
                createScheduledFollowUp(LocalDate.of(2026, 10, 10));

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);
        patient.setMobile("   ");

        when(notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUpId,
                        tenantId
                ))
                .thenReturn(List.of());

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(patient));

        reminderService.createReminderForFollowUp(followUp);

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void createReminderForFollowUp_shouldRejectMissingPatient() {

        FollowUp followUp =
                createScheduledFollowUp(LocalDate.of(2026, 10, 10));

        when(notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUpId,
                        tenantId
                ))
                .thenReturn(List.of());

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reminderService.createReminderForFollowUp(followUp)
        );

        assertEquals("Patient not found", exception.getMessage());

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void createReminderForFollowUp_shouldIgnoreNullFollowUp() {

        reminderService.createReminderForFollowUp(null);

        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(patientRepository);
    }

    private FollowUp createScheduledFollowUp(LocalDate followUpDate) {

        FollowUp followUp = new FollowUp();

        followUp.setId(followUpId);
        followUp.setTenantId(tenantId);
        followUp.setClinicId(clinicId);
        followUp.setPatientId(patientId);
        followUp.setDoctorId(doctorId);
        followUp.setFollowUpDate(followUpDate);
        followUp.setStatus("SCHEDULED");
        followUp.setReason("Review treatment");
        followUp.setNotes("Continue treatment");

        return followUp;
    }

    @Test
    void rescheduleReminderForFollowUp_shouldUpdatePendingReminder() {

        FollowUp followUp =
                createScheduledFollowUp(LocalDate.of(2026, 10, 15));

        Notification notification = new Notification();
        notification.setType(NotificationType.FOLLOW_UP_REMINDER);
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUpId,
                        tenantId
                ))
                .thenReturn(List.of(notification));

        reminderService.rescheduleReminderForFollowUp(followUp);

        OffsetDateTime expectedScheduledAt =
                OffsetDateTime.of(
                        LocalDate.of(2026, 10, 14),
                        LocalTime.of(9, 0),
                        ZoneOffset.ofHoursMinutes(5, 30)
                );

        assertEquals(
                expectedScheduledAt,
                notification.getScheduledAt()
        );

        verify(notificationRepository).save(notification);
    }

    @Test
    void cancelPendingReminderForFollowUp_shouldCancelPendingReminder() {

        FollowUp followUp =
                createScheduledFollowUp(LocalDate.of(2026, 10, 15));

        Notification notification = new Notification();
        notification.setType(NotificationType.FOLLOW_UP_REMINDER);
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUpId,
                        tenantId
                ))
                .thenReturn(List.of(notification));

        reminderService.cancelPendingReminderForFollowUp(followUp);

        assertEquals(
                NotificationStatus.CANCELLED,
                notification.getStatus()
        );

        verify(notificationRepository).save(notification);
    }
}


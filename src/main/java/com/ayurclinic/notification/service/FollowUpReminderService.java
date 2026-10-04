package com.ayurclinic.notification.service;

import com.ayurclinic.notification.config.NotificationProperties;
import com.ayurclinic.notification.entity.Notification;
import com.ayurclinic.notification.enums.NotificationChannel;
import com.ayurclinic.notification.enums.NotificationStatus;
import com.ayurclinic.notification.enums.NotificationType;
import com.ayurclinic.notification.repository.NotificationRepository;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.repository.PatientRepository;
import com.ayurclinic.followup.entity.FollowUp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.LocalTime;
import java.util.List;

@Service
public class FollowUpReminderService {

    private final NotificationRepository notificationRepository;
    private final PatientRepository patientRepository;
    private final NotificationProperties notificationProperties;

    public FollowUpReminderService(
            NotificationRepository notificationRepository,
            PatientRepository patientRepository,
            NotificationProperties notificationProperties
    ) {
        this.notificationRepository = notificationRepository;
        this.patientRepository = patientRepository;
        this.notificationProperties = notificationProperties;
    }

    @Transactional
    public void createReminderForFollowUp(FollowUp followUp) {

        if (followUp == null) {
            return;
        }

        if (!"SCHEDULED".equals(followUp.getStatus())) {
            return;
        }

        boolean alreadyExists =
                notificationRepository
                        .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                                followUp.getId(),
                                followUp.getTenantId()
                        )
                        .stream()
                        .anyMatch(notification ->
                                notification.getType()
                                        == NotificationType.FOLLOW_UP_REMINDER
                        );

        if (alreadyExists) {
            return;
        }

        Patient patient =
                patientRepository
                        .findByIdAndTenantId(
                                followUp.getPatientId(),
                                followUp.getTenantId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Patient not found"
                                )
                        );

        String recipient = patient.getMobile();

        if (recipient == null || recipient.isBlank()) {
            return;
        }

        OffsetDateTime scheduledAt =
                calculateReminderTime(
                        followUp.getFollowUpDate()
                );

        Notification notification = new Notification();

        notification.setTenantId(
                followUp.getTenantId()
        );

        notification.setClinicId(
                followUp.getClinicId()
        );

        notification.setPatientId(
                followUp.getPatientId()
        );

        notification.setFollowupId(
                followUp.getId()
        );

        notification.setType(
                NotificationType.FOLLOW_UP_REMINDER
        );

        notification.setChannel(
                NotificationChannel.SMS
        );

        notification.setRecipient(
                recipient
        );

        notification.setSubject(
                "AyurClinic Follow-up Reminder"
        );

        notification.setMessage(
                buildReminderMessage(followUp)
        );

        notification.setScheduledAt(
                scheduledAt
        );

        notification.setStatus(
                NotificationStatus.PENDING
        );

        notificationRepository.save(notification);
    }

    @Transactional
    public void rescheduleReminderForFollowUp(FollowUp followUp) {
        if (followUp == null) {
            return;
        }

        notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUp.getId(),
                        followUp.getTenantId()
                )
                .stream()
                .filter(notification ->
                        notification.getType() == NotificationType.FOLLOW_UP_REMINDER
                )
                .filter(notification ->
                        notification.getStatus() == NotificationStatus.PENDING
                )
                .forEach(notification -> {
                    notification.setScheduledAt(
                            calculateReminderTime(followUp.getFollowUpDate())
                    );

                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void cancelPendingReminderForFollowUp(FollowUp followUp) {
        if (followUp == null) {
            return;
        }

        notificationRepository
                .findByFollowupIdAndTenantIdOrderByCreatedAtDesc(
                        followUp.getId(),
                        followUp.getTenantId()
                )
                .stream()
                .filter(notification ->
                        notification.getType() == NotificationType.FOLLOW_UP_REMINDER
                )
                .filter(notification ->
                        notification.getStatus() == NotificationStatus.PENDING
                )
                .forEach(notification -> {
                    notification.setStatus(NotificationStatus.CANCELLED);
                    notificationRepository.save(notification);
                });
    }
    private OffsetDateTime calculateReminderTime(
            LocalDate followUpDate
    ) {

        LocalDate reminderDate =
                followUpDate.minusDays(
                        notificationProperties
                                .getReminderDaysBefore()
                );

        LocalTime reminderTime =
                LocalTime.of(
                        notificationProperties.getReminderHour(),
                        notificationProperties.getReminderMinute()
                );

        return reminderDate
                .atTime(reminderTime)
                .atOffset(ZoneOffset.ofHoursMinutes(5, 30));
    }

    private String buildReminderMessage(
            FollowUp followUp
    ) {

        return "Reminder: Your AyurClinic follow-up is scheduled for "
                + followUp.getFollowUpDate()
                + ".";
    }
}
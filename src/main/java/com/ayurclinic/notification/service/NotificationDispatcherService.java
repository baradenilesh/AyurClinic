package com.ayurclinic.notification.service;

import com.ayurclinic.notification.entity.Notification;
import com.ayurclinic.notification.enums.NotificationStatus;
import com.ayurclinic.notification.provider.NotificationProvider;
import com.ayurclinic.notification.provider.NotificationProviderResult;
import com.ayurclinic.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class NotificationDispatcherService {

    private final NotificationRepository notificationRepository;
    private final NotificationProvider notificationProvider;

    public NotificationDispatcherService(
            NotificationRepository notificationRepository,
            NotificationProvider notificationProvider
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationProvider = notificationProvider;
    }

    @Transactional
    public void processDueNotifications() {

        OffsetDateTime now = OffsetDateTime.now();

        List<Notification> notifications =
                notificationRepository
                        .findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                                NotificationStatus.PENDING,
                                now
                        );

        for (Notification notification : notifications) {
            processNotification(notification);
        }
    }

    @Transactional
    public void processNotification(Notification notification) {

        if (notification.getStatus() != NotificationStatus.PENDING) {
            return;
        }

        try {

            NotificationProviderResult result =
                    notificationProvider.send(notification);

            if (result.isSuccess()) {

                notification.setStatus(
                        NotificationStatus.SENT
                );

                notification.setProviderMessageId(
                        result.getProviderMessageId()
                );

                notification.setSentAt(
                        OffsetDateTime.now()
                );

                notification.setFailureReason(null);

            } else {

                notification.setStatus(
                        NotificationStatus.FAILED
                );

                notification.setFailureReason(
                        result.getFailureReason()
                );
            }

        } catch (Exception ex) {

            notification.setStatus(
                    NotificationStatus.FAILED
            );

            notification.setFailureReason(
                    ex.getMessage()
            );
        }

        notificationRepository.save(notification);
    }
}
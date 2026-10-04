package com.ayurclinic.notification.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private final NotificationDispatcherService dispatcherService;

    public NotificationScheduler(
            NotificationDispatcherService dispatcherService
    ) {
        this.dispatcherService = dispatcherService;
    }

    @Scheduled(fixedDelay = 30000)
    public void processScheduledNotifications() {

        dispatcherService.processDueNotifications();
    }
}
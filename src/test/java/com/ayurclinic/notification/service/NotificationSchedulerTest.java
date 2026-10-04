package com.ayurclinic.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private NotificationDispatcherService dispatcherService;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @Test
    void processScheduledNotifications_shouldInvokeDispatcher() {

        notificationScheduler.processScheduledNotifications();

        verify(dispatcherService)
                .processDueNotifications();
    }
}
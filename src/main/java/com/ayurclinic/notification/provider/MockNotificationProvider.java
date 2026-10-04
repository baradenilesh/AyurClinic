package com.ayurclinic.notification.provider;

import com.ayurclinic.notification.entity.Notification;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(
        prefix = "ayurclinic.notification.provider",
        name = "mode",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockNotificationProvider implements NotificationProvider {

    @Override
    public NotificationProviderResult send(
            Notification notification
    ) {

        return NotificationProviderResult.success(
                "MOCK-" + UUID.randomUUID()
        );
    }
}

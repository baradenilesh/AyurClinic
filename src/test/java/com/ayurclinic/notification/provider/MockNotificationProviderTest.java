package com.ayurclinic.notification.provider;

import com.ayurclinic.notification.entity.Notification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockNotificationProviderTest {

    @Test
    void send_shouldReturnSuccessfulMockResult() {

        MockNotificationProvider provider =
                new MockNotificationProvider();

        Notification notification =
                new Notification();

        NotificationProviderResult result =
                provider.send(notification);

        assertNotNull(result);

        assertTrue(
                result.isSuccess()
        );

        assertNotNull(
                result.getProviderMessageId()
        );

        assertTrue(
                result.getProviderMessageId()
                        .startsWith("MOCK-")
        );

        assertNull(
                result.getFailureReason()
        );
    }
}

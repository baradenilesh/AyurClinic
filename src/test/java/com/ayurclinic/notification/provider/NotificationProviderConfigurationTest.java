package com.ayurclinic.notification.provider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(
        properties = {
                "ayurclinic.notification.provider.mode=mock"
        }
)
class NotificationProviderConfigurationTest {

    @Autowired
    private NotificationProvider notificationProvider;

    @Test
    void mockMode_shouldLoadMockNotificationProvider() {

        assertNotNull(notificationProvider);

        assertInstanceOf(
                MockNotificationProvider.class,
                notificationProvider
        );
    }
}

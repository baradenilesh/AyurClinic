package com.ayurclinic.notification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({
        NotificationProperties.class,
        NotificationProviderProperties.class
})
public class NotificationSchedulingConfig {
}
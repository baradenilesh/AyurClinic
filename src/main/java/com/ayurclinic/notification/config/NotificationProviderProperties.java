package com.ayurclinic.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ayurclinic.notification.provider")
public class NotificationProviderProperties {

    /**
     * Provider mode used by the notification dispatcher.
     *
     * Supported values for now:
     * mock
     */
    private String mode = "mock";
}

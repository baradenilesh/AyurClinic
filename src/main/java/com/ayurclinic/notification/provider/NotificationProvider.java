package com.ayurclinic.notification.provider;

import com.ayurclinic.notification.entity.Notification;

public interface NotificationProvider {

    NotificationProviderResult send(Notification notification);
}
package com.ayurclinic.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ayurclinic.notification")
public class NotificationProperties {

    /**
     * Number of days before the follow-up date
     * when the reminder should be scheduled.
     */
    private long reminderDaysBefore = 1;

    /**
     * Time of day at which the reminder should be sent.
     */
    private int reminderHour = 9;

    private int reminderMinute = 0;

    public long getReminderDaysBefore() {
        return reminderDaysBefore;
    }

    public void setReminderDaysBefore(long reminderDaysBefore) {
        this.reminderDaysBefore = reminderDaysBefore;
    }

    public int getReminderHour() {
        return reminderHour;
    }

    public void setReminderHour(int reminderHour) {
        this.reminderHour = reminderHour;
    }

    public int getReminderMinute() {
        return reminderMinute;
    }

    public void setReminderMinute(int reminderMinute) {
        this.reminderMinute = reminderMinute;
    }
}
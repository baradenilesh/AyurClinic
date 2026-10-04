package com.ayurclinic.notification.dto;

import com.ayurclinic.notification.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NotificationUpdateStatusRequest {

    @NotNull
    private NotificationStatus status;

    @Size(max = 255)
    private String providerMessageId;

    private String failureReason;

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(String providerMessageId) {
        this.providerMessageId = providerMessageId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
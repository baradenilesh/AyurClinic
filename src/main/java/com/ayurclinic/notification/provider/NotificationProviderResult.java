package com.ayurclinic.notification.provider;

public class NotificationProviderResult {

    private final boolean success;
    private final String providerMessageId;
    private final String failureReason;

    private NotificationProviderResult(
            boolean success,
            String providerMessageId,
            String failureReason
    ) {
        this.success = success;
        this.providerMessageId = providerMessageId;
        this.failureReason = failureReason;
    }

    public static NotificationProviderResult success(
            String providerMessageId
    ) {
        return new NotificationProviderResult(
                true,
                providerMessageId,
                null
        );
    }

    public static NotificationProviderResult failure(
            String failureReason
    ) {
        return new NotificationProviderResult(
                false,
                null,
                failureReason
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
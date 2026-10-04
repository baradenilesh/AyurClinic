package com.ayurclinic.patient.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public class PatientHistoryRequest {

    @NotBlank
    private String historyType;

    private String title;

    private String description;

    private OffsetDateTime recordedAt;

    public String getHistoryType() {
        return historyType;
    }

    public void setHistoryType(String historyType) {
        this.historyType = historyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
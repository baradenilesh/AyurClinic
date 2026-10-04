package com.ayurclinic.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class PrescriptionCreateRequest {

    @NotNull
    private UUID consultationId;

    private String diagnosis;

    private String notes;

    @NotEmpty
    @Valid
    private List<PrescriptionItemRequest> items;

    public UUID getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(UUID consultationId) {
        this.consultationId = consultationId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PrescriptionItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemRequest> items) {
        this.items = items;
    }
}
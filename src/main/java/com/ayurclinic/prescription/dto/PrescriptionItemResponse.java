package com.ayurclinic.prescription.dto;

import com.ayurclinic.prescription.entity.PrescriptionItem;

import java.util.UUID;

public class PrescriptionItemResponse {

    private UUID id;
    private String medicineName;
    private String medicineType;
    private String dosage;
    private String frequency;
    private String duration;
    private String route;
    private String instructions;
    private String quantity;
    private Integer sortOrder;

    public static PrescriptionItemResponse fromEntity(
            PrescriptionItem item
    ) {
        PrescriptionItemResponse response =
                new PrescriptionItemResponse();

        response.setId(item.getId());
        response.setMedicineName(item.getMedicineName());
        response.setMedicineType(item.getMedicineType());
        response.setDosage(item.getDosage());
        response.setFrequency(item.getFrequency());
        response.setDuration(item.getDuration());
        response.setRoute(item.getRoute());
        response.setInstructions(item.getInstructions());
        response.setQuantity(item.getQuantity());
        response.setSortOrder(item.getSortOrder());

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineType() {
        return medicineType;
    }

    public void setMedicineType(String medicineType) {
        this.medicineType = medicineType;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
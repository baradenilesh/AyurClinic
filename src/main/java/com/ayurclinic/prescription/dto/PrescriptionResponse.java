package com.ayurclinic.prescription.dto;

import com.ayurclinic.prescription.entity.Prescription;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PrescriptionResponse {

    private UUID id;
    private UUID tenantId;
    private UUID clinicId;
    private UUID consultationId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;

    private LocalDate prescriptionDate;

    private String diagnosis;
    private String notes;
    private String status;

    private List<PrescriptionItemResponse> items;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static PrescriptionResponse fromEntity(
            Prescription prescription
    ) {
        PrescriptionResponse response =
                new PrescriptionResponse();

        response.setId(prescription.getId());
        response.setTenantId(prescription.getTenantId());
        response.setClinicId(prescription.getClinicId());
        response.setConsultationId(prescription.getConsultationId());
        response.setAppointmentId(prescription.getAppointmentId());
        response.setPatientId(prescription.getPatientId());
        response.setDoctorId(prescription.getDoctorId());
        response.setPrescriptionDate(
                prescription.getPrescriptionDate()
        );
        response.setDiagnosis(prescription.getDiagnosis());
        response.setNotes(prescription.getNotes());
        response.setStatus(prescription.getStatus());

        response.setItems(
                prescription.getItems()
                        .stream()
                        .map(PrescriptionItemResponse::fromEntity)
                        .toList()
        );

        response.setCreatedAt(prescription.getCreatedAt());
        response.setUpdatedAt(prescription.getUpdatedAt());

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getClinicId() {
        return clinicId;
    }

    public void setClinicId(UUID clinicId) {
        this.clinicId = clinicId;
    }

    public UUID getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(UUID consultationId) {
        this.consultationId = consultationId;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PrescriptionItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemResponse> items) {
        this.items = items;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
package com.ayurclinic.consultation.dto;

import com.ayurclinic.consultation.entity.Consultation;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ConsultationResponse {

    private UUID id;

    private UUID tenantId;

    private UUID clinicId;

    private UUID appointmentId;

    private UUID patientId;

    private UUID doctorId;

    private LocalDate consultationDate;

    private String chiefComplaint;

    private String symptoms;

    private String clinicalFindings;

    private String diagnosis;

    private String treatmentPlan;

    private String doctorNotes;

    private String status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public static ConsultationResponse fromEntity(
            Consultation consultation) {

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultation.getId());
        response.setTenantId(consultation.getTenantId());
        response.setClinicId(consultation.getClinicId());
        response.setAppointmentId(consultation.getAppointmentId());
        response.setPatientId(consultation.getPatientId());
        response.setDoctorId(consultation.getDoctorId());
        response.setConsultationDate(
                consultation.getConsultationDate()
        );
        response.setChiefComplaint(
                consultation.getChiefComplaint()
        );
        response.setSymptoms(
                consultation.getSymptoms()
        );
        response.setClinicalFindings(
                consultation.getClinicalFindings()
        );
        response.setDiagnosis(
                consultation.getDiagnosis()
        );
        response.setTreatmentPlan(
                consultation.getTreatmentPlan()
        );
        response.setDoctorNotes(
                consultation.getDoctorNotes()
        );
        response.setStatus(
                consultation.getStatus()
        );
        response.setCreatedAt(
                consultation.getCreatedAt()
        );
        response.setUpdatedAt(
                consultation.getUpdatedAt()
        );

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

    public LocalDate getConsultationDate() {
        return consultationDate;
    }

    public void setConsultationDate(LocalDate consultationDate) {
        this.consultationDate = consultationDate;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getClinicalFindings() {
        return clinicalFindings;
    }

    public void setClinicalFindings(String clinicalFindings) {
        this.clinicalFindings = clinicalFindings;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
package com.ayurclinic.consultation.dto;

public class ConsultationUpdateRequest {

    private String chiefComplaint;

    private String symptoms;

    private String clinicalFindings;

    private String diagnosis;

    private String treatmentPlan;

    private String doctorNotes;

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
}
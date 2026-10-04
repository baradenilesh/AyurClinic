package com.ayurclinic.consultation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "consultations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_consultation_appointment",
                        columnNames = "appointment_id"
                )
        }
)
@Getter
@Setter
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "clinic_id", nullable = false)
    private UUID clinicId;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    @Column(name = "chief_complaint")
    private String chiefComplaint;

    @Column(name = "symptoms")
    private String symptoms;

    @Column(name = "clinical_findings")
    private String clinicalFindings;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "treatment_plan")
    private String treatmentPlan;

    @Column(name = "doctor_notes")
    private String doctorNotes;

    @Column(name = "status", nullable = false)
    private String status = "IN_PROGRESS";

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
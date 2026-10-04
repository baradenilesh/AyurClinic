package com.ayurclinic.patient.repository;

import com.ayurclinic.patient.entity.PatientHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PatientHistoryRepository
        extends JpaRepository<PatientHistory, UUID> {

    List<PatientHistory> findByTenantIdAndPatientIdOrderByRecordedAtDesc(
            UUID tenantId,
            UUID patientId
    );
}
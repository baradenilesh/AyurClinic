package com.ayurclinic.patient.service;

import com.ayurclinic.patient.dto.PatientHistoryRequest;
import com.ayurclinic.patient.dto.PatientHistoryResponse;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.entity.PatientHistory;
import com.ayurclinic.patient.repository.PatientHistoryRepository;
import com.ayurclinic.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientHistoryService {

    private final PatientHistoryRepository patientHistoryRepository;
    private final PatientRepository patientRepository;

    public PatientHistoryService(
            PatientHistoryRepository patientHistoryRepository,
            PatientRepository patientRepository
    ) {
        this.patientHistoryRepository = patientHistoryRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientHistoryResponse addHistory(
            UUID tenantId,
            UUID patientId,
            PatientHistoryRequest request
    ) {

        Patient patient = patientRepository
                .findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() ->
                        new RuntimeException("Patient not found")
                );

        PatientHistory history = new PatientHistory();

        history.setTenantId(tenantId);
        history.setPatientId(patient.getId());
        history.setHistoryType(request.getHistoryType());
        history.setTitle(request.getTitle());
        history.setDescription(request.getDescription());
        history.setRecordedAt(request.getRecordedAt());

        PatientHistory saved =
                patientHistoryRepository.save(history);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PatientHistoryResponse> getHistory(
            UUID tenantId,
            UUID patientId
    ) {

        patientRepository
                .findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() ->
                        new RuntimeException("Patient not found")
                );

        return patientHistoryRepository
                .findByTenantIdAndPatientIdOrderByRecordedAtDesc(
                        tenantId,
                        patientId
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PatientHistoryResponse mapToResponse(
            PatientHistory history
    ) {

        PatientHistoryResponse response =
                new PatientHistoryResponse();

        response.setId(history.getId());
        response.setPatientId(history.getPatientId());
        response.setHistoryType(history.getHistoryType());
        response.setTitle(history.getTitle());
        response.setDescription(history.getDescription());
        response.setRecordedAt(history.getRecordedAt());
        response.setCreatedAt(history.getCreatedAt());

        return response;
    }
}
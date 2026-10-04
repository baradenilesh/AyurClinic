package com.ayurclinic.patient.service;

import com.ayurclinic.patient.dto.PatientHistoryRequest;
import com.ayurclinic.patient.dto.PatientHistoryResponse;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.entity.PatientHistory;
import com.ayurclinic.patient.repository.PatientHistoryRepository;
import com.ayurclinic.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientHistoryServiceTest {

    @Mock
    private PatientHistoryRepository patientHistoryRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientHistoryService patientHistoryService;

    private UUID tenantId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        patientId = UUID.randomUUID();
    }

    @Test
    void addHistory_shouldCreateHistoryForTenantPatient() {

        Patient patient = new Patient();

        patient.setId(patientId);
        patient.setTenantId(tenantId);

        PatientHistoryRequest request =
                new PatientHistoryRequest();

        request.setHistoryType("CONSULTATION");
        request.setTitle("Initial Consultation");
        request.setDescription(
                "Patient consultation completed."
        );

        PatientHistory savedHistory =
                new PatientHistory();

        UUID historyId = UUID.randomUUID();

        savedHistory.setId(historyId);
        savedHistory.setTenantId(tenantId);
        savedHistory.setPatientId(patientId);
        savedHistory.setHistoryType("CONSULTATION");
        savedHistory.setTitle("Initial Consultation");
        savedHistory.setDescription(
                "Patient consultation completed."
        );

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(patientHistoryRepository.save(
                any(PatientHistory.class)
        )).thenReturn(savedHistory);

        PatientHistoryResponse result =
                patientHistoryService.addHistory(
                        tenantId,
                        patientId,
                        request
                );

        assertEquals(
                historyId,
                result.getId()
        );

        assertEquals(
                patientId,
                result.getPatientId()
        );

        assertEquals(
                "CONSULTATION",
                result.getHistoryType()
        );

        assertEquals(
                "Initial Consultation",
                result.getTitle()
        );

        verify(patientRepository)
                .findByIdAndTenantId(
                        patientId,
                        tenantId
                );

        verify(patientHistoryRepository)
                .save(any(PatientHistory.class));
    }

    @Test
    void addHistory_shouldRejectPatientFromAnotherTenant() {

        UUID differentTenantId = UUID.randomUUID();

        PatientHistoryRequest request =
                new PatientHistoryRequest();

        request.setHistoryType("CONSULTATION");
        request.setTitle("Initial Consultation");

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.empty());

        RuntimeException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> patientHistoryService.addHistory(
                                tenantId,
                                patientId,
                                request
                        )
                );

        assertEquals(
                "Patient not found",
                exception.getMessage()
        );

        verify(patientRepository)
                .findByIdAndTenantId(
                        patientId,
                        tenantId
                );

        verify(patientHistoryRepository,
                org.mockito.Mockito.never())
                .save(any(PatientHistory.class));
    }
    @Test
    void getHistory_shouldReturnPatientHistory() {

        Patient patient = new Patient();

        patient.setId(patientId);
        patient.setTenantId(tenantId);

        PatientHistory history =
                new PatientHistory();

        UUID historyId = UUID.randomUUID();

        history.setId(historyId);
        history.setTenantId(tenantId);
        history.setPatientId(patientId);
        history.setHistoryType("CONSULTATION");
        history.setTitle("Initial Consultation");
        history.setDescription("Patient consultation completed.");

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(patientHistoryRepository
                .findByTenantIdAndPatientIdOrderByRecordedAtDesc(
                        tenantId,
                        patientId
                ))
                .thenReturn(java.util.List.of(history));

        java.util.List<PatientHistoryResponse> result =
                patientHistoryService.getHistory(
                        tenantId,
                        patientId
                );

        assertEquals(1, result.size());

        assertEquals(
                historyId,
                result.get(0).getId()
        );

        assertEquals(
                patientId,
                result.get(0).getPatientId()
        );

        assertEquals(
                "CONSULTATION",
                result.get(0).getHistoryType()
        );

        assertEquals(
                "Initial Consultation",
                result.get(0).getTitle()
        );

        verify(patientRepository)
                .findByIdAndTenantId(
                        patientId,
                        tenantId
                );

        verify(patientHistoryRepository)
                .findByTenantIdAndPatientIdOrderByRecordedAtDesc(
                        tenantId,
                        patientId
                );
    }

    @Test
    void getHistory_shouldRejectPatientFromAnotherTenant() {

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.empty());

        RuntimeException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> patientHistoryService.getHistory(
                                tenantId,
                                patientId
                        )
                );

        assertEquals(
                "Patient not found",
                exception.getMessage()
        );

        verify(patientRepository)
                .findByIdAndTenantId(
                        patientId,
                        tenantId
                );

        verify(
                patientHistoryRepository,
                org.mockito.Mockito.never()
        ).findByTenantIdAndPatientIdOrderByRecordedAtDesc(
                tenantId,
                patientId
        );
    }
}
package com.ayurclinic.patient.service;

import com.ayurclinic.common.exception.ResourceNotFoundException;
import com.ayurclinic.patient.dto.*;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.entity.PatientHistory;
import com.ayurclinic.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.patient.repository.PatientNumberCounterRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.ayurclinic.patient.repository.PatientHistoryRepository;
import com.ayurclinic.patient.service.PatientHistoryService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private PatientNumberCounterRepository patientNumberCounterRepository;

    @Mock
    private PatientHistoryRepository patientHistoryRepository;

    @Mock
    private PatientHistoryService patientHistoryService;

    @Test
    void createPatient_whenValidRequest_createsPatientForTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CreatePatientRequest request = new CreatePatientRequest();
        request.setClinicId(clinicId);
        request.setFirstName("Rahul");
        request.setLastName("Sharma");
        request.setMobile("9876543210");
        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(new com.ayurclinic.clinic.entity.Clinic()));
        when(patientRepository.existsByMobileAndTenantId(
                request.getMobile(),
                tenantId
        )).thenReturn(false);

        Patient savedPatient = new Patient();
        savedPatient.setId(UUID.randomUUID());
        savedPatient.setTenantId(tenantId);
        savedPatient.setClinicId(clinicId);
        savedPatient.setPatientNumber("PAT-000001");
        savedPatient.setFirstName("Rahul");
        savedPatient.setMobile("9876543210");
        savedPatient.setStatus("ACTIVE");

        when(patientNumberCounterRepository.getCurrentNumber(tenantId))
                .thenReturn(1L);
        when(patientRepository.save(any(Patient.class)))
                .thenReturn(savedPatient);

        var response = patientService.createPatient(
                tenantId,
                request
        );

        assertNotNull(response);
        assertEquals(tenantId, response.getTenantId());
        assertEquals(clinicId, response.getClinicId());
        assertEquals("Rahul", response.getFirstName());

        verify(patientRepository)
                .existsByMobileAndTenantId(
                        "9876543210",
                        tenantId
                );

        verify(patientRepository)
                .save(any(Patient.class));
    }

    @Test
    void createPatient_whenMobileAlreadyExists_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();

        CreatePatientRequest request = new CreatePatientRequest();
        request.setClinicId(UUID.randomUUID());
        request.setFirstName("Rahul");
        request.setMobile("9876543210");
        when(clinicRepository.findByIdAndTenantId(
                request.getClinicId(),
                tenantId
        )).thenReturn(
                Optional.of(new com.ayurclinic.clinic.entity.Clinic())
        );
        when(patientRepository.existsByMobileAndTenantId(
                request.getMobile(),
                tenantId
        )).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> patientService.createPatient(
                                tenantId,
                                request
                        )
                );

        assertEquals(
                "Patient with mobile number already exists",
                exception.getMessage()
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void getPatient_whenPatientBelongsToTenant_returnsPatient() {

        UUID tenantId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);
        patient.setClinicId(UUID.randomUUID());
        patient.setPatientNumber("PAT-000001");
        patient.setFirstName("Rahul");
        patient.setMobile("9876543210");
        patient.setStatus("ACTIVE");

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        var response = patientService.getPatient(
                tenantId,
                patientId
        );

        assertEquals(patientId, response.getId());
        assertEquals(tenantId, response.getTenantId());
        assertEquals("Rahul", response.getFirstName());
    }

    @Test
    void getPatient_whenPatientBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.getPatient(
                        tenantId,
                        patientId
                )
        );
    }

    @Test
    void searchPatients_whenNameIsBlank_returnsActivePatientsForTenant() {

        UUID tenantId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setTenantId(tenantId);
        patient.setFirstName("Rahul");
        patient.setMobile("9876543210");
        patient.setStatus("ACTIVE");

        when(patientRepository.findByTenantIdAndStatus(
                tenantId,
                "ACTIVE"
        )).thenReturn(List.of(patient));

        var response = patientService.searchPatients(
                tenantId,
                null
        );

        assertEquals(1, response.size());
        assertEquals("Rahul", response.get(0).getFirstName());
    }

    @Test
    void searchPatients_whenNameProvided_searchesWithinTenant() {

        UUID tenantId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setTenantId(tenantId);
        patient.setFirstName("Rahul");
        patient.setMobile("9876543210");
        patient.setStatus("ACTIVE");

        when(patientRepository
                .findByTenantIdAndFirstNameContainingIgnoreCaseOrTenantIdAndLastNameContainingIgnoreCase(
                        tenantId,
                        "Rahul",
                        tenantId,
                        "Rahul"
                ))
                .thenReturn(List.of(patient));

        var response = patientService.searchPatients(
                tenantId,
                "Rahul"
        );

        assertEquals(1, response.size());
        assertEquals("Rahul", response.get(0).getFirstName());
    }

    @Test
    void updatePatient_whenPatientBelongsToTenant_updatesPatient() {

        UUID tenantId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);
        patient.setClinicId(UUID.randomUUID());
        patient.setFirstName("Rahul");
        patient.setMobile("9876543210");
        patient.setStatus("ACTIVE");

        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setFirstName("Rahul Updated");
        request.setMobile("9999999999");

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(patientRepository.existsByMobileAndTenantIdAndIdNot(
                "9999999999",
                tenantId,
                patientId
        )).thenReturn(false);

        when(patientRepository.save(patient))
                .thenReturn(patient);

        var response = patientService.updatePatient(
                tenantId,
                patientId,
                request
        );

        assertEquals(
                "Rahul Updated",
                response.getFirstName()
        );

        assertEquals(
                "9999999999",
                response.getMobile()
        );
    }

    @Test
    void updatePatient_whenPatientBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setFirstName("Updated");
        request.setMobile("9999999999");

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.updatePatient(
                        tenantId,
                        patientId,
                        request
                )
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void updatePatient_whenMobileBelongsToAnotherPatient_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);

        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setFirstName("Rahul");
        request.setMobile("9999999999");

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(patientRepository.existsByMobileAndTenantIdAndIdNot(
                "9999999999",
                tenantId,
                patientId
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> patientService.updatePatient(
                        tenantId,
                        patientId,
                        request
                )
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void updatePatientStatus_whenValidStatus_updatesStatus() {

        UUID tenantId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);
        patient.setStatus("ACTIVE");

        UpdatePatientStatusRequest request =
                new UpdatePatientStatusRequest();

        request.setStatus("INACTIVE");

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(patientRepository.save(patient))
                .thenReturn(patient);

        var response = patientService.updatePatientStatus(
                tenantId,
                patientId,
                request
        );

        assertEquals("INACTIVE", response.getStatus());
    }

    @Test
    void updatePatientStatus_whenInvalidStatus_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);

        UpdatePatientStatusRequest request =
                new UpdatePatientStatusRequest();

        request.setStatus("DELETED");

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> patientService.updatePatientStatus(
                                tenantId,
                                patientId,
                                request
                        )
                );

        assertEquals(
                "Status must be ACTIVE or INACTIVE",
                exception.getMessage()
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void createPatient_whenClinicBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CreatePatientRequest request = new CreatePatientRequest();
        request.setClinicId(clinicId);
        request.setFirstName("Rahul");
        request.setMobile("9876543210");

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.createPatient(
                        tenantId,
                        request
                )
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void searchPatients_byMobile_shouldReturnPatient() {

        UUID testPatientId = UUID.randomUUID();
        UUID testTenantId = UUID.randomUUID();
        UUID testClinicId = UUID.randomUUID();

        Patient patient = new Patient();

        patient.setId(testPatientId);
        patient.setTenantId(testTenantId);
        patient.setClinicId(testClinicId);
        patient.setPatientNumber("PAT-000001");
        patient.setFirstName("Rahul");
        patient.setMobile("9876543210");
        patient.setStatus("ACTIVE");

        when(patientRepository.findByMobileAndTenantId(
                "9876543210",
                testTenantId
        )).thenReturn(Optional.of(patient));

        List<PatientResponse> result =
                patientService.searchPatients(
                        testTenantId,
                        null,
                        "9876543210",
                        null
                );

        assertEquals(1, result.size());
        assertEquals(
                "Rahul",
                result.get(0).getFirstName()
        );

        verify(patientRepository)
                .findByMobileAndTenantId(
                        "9876543210",
                        testTenantId
                );
    }

    @Test
    void searchPatients_byPatientNumber_shouldReturnPatients() {

        UUID testPatientId = UUID.randomUUID();
        UUID testTenantId = UUID.randomUUID();
        UUID testClinicId = UUID.randomUUID();

        Patient patient = new Patient();

        patient.setId(testPatientId);
        patient.setTenantId(testTenantId);
        patient.setClinicId(testClinicId);
        patient.setPatientNumber("PAT-000001");
        patient.setFirstName("Rahul");
        patient.setMobile("9876543210");
        patient.setStatus("ACTIVE");

        when(patientRepository
                .findByTenantIdAndPatientNumberContainingIgnoreCase(
                        testTenantId,
                        "PAT-000001"
                ))
                .thenReturn(List.of(patient));

        List<PatientResponse> result =
                patientService.searchPatients(
                        testTenantId,
                        null,
                        null,
                        "PAT-000001"
                );

        assertEquals(1, result.size());
        assertEquals(
                "PAT-000001",
                result.get(0).getPatientNumber()
        );

        verify(patientRepository)
                .findByTenantIdAndPatientNumberContainingIgnoreCase(
                        testTenantId,
                        "PAT-000001"
                );
    }


}
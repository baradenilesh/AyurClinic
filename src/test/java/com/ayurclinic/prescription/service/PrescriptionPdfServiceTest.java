package com.ayurclinic.prescription.service;

import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.patient.repository.PatientRepository;
import com.ayurclinic.prescription.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionPdfServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PrescriptionPdfService prescriptionPdfService;

    private UUID tenantId;
    private UUID prescriptionId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        prescriptionId = UUID.randomUUID();
    }

    @Test
    void generatePrescriptionPdf_shouldRejectCrossTenantAccess() {

        when(
                prescriptionRepository.findByIdAndTenantId(
                        prescriptionId,
                        tenantId
                )
        ).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                prescriptionPdfService
                                        .generatePrescriptionPdf(
                                                tenantId,
                                                prescriptionId
                                        )
                );

        assertEquals(
                "Prescription not found",
                exception.getMessage()
        );

        verify(
                prescriptionRepository
        ).findByIdAndTenantId(
                prescriptionId,
                tenantId
        );

        verifyNoInteractions(
                clinicRepository,
                doctorRepository,
                patientRepository
        );
    }

    @Test
    void generatePrescriptionPdf_shouldRejectNullTenant() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        prescriptionPdfService
                                .generatePrescriptionPdf(
                                        null,
                                        prescriptionId
                                )
        );

        verifyNoInteractions(
                prescriptionRepository,
                clinicRepository,
                doctorRepository,
                patientRepository
        );
    }
}
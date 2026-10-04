package com.ayurclinic.prescription.service;

import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.prescription.dto.*;
import com.ayurclinic.prescription.entity.Prescription;
import com.ayurclinic.prescription.enums.PrescriptionStatus;
import com.ayurclinic.prescription.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private UUID tenantId;
    private UUID consultationId;
    private UUID clinicId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private UUID prescriptionId;

    private Consultation consultation;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        consultationId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        prescriptionId = UUID.randomUUID();

        consultation = new Consultation();
        consultation.setId(consultationId);
        consultation.setTenantId(tenantId);
        consultation.setClinicId(clinicId);
        consultation.setAppointmentId(appointmentId);
        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorId);
        consultation.setConsultationDate(LocalDate.of(2026, 10, 2));
        consultation.setStatus("COMPLETED");
    }

    @Test
    void createPrescription_shouldCreateDraft() {

        PrescriptionCreateRequest request =
                createRequest();

        when(consultationRepository.findByIdAndTenantId(
                consultationId,
                tenantId
        )).thenReturn(Optional.of(consultation));

        when(prescriptionRepository
                .existsByConsultationIdAndTenantId(
                        consultationId,
                        tenantId
                )).thenReturn(false);

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenAnswer(invocation -> {
                    Prescription prescription =
                            invocation.getArgument(0);

                    prescription.setId(prescriptionId);

                    return prescription;
                });

        PrescriptionResponse response =
                prescriptionService.createPrescription(
                        tenantId,
                        request
                );

        assertNotNull(response);
        assertEquals(
                prescriptionId,
                response.getId()
        );

        assertEquals(
                consultationId,
                response.getConsultationId()
        );

        assertEquals(
                PrescriptionStatus.DRAFT.name(),
                response.getStatus()
        );

        assertEquals(
                1,
                response.getItems().size()
        );

        assertEquals(
                "Ashwagandha",
                response.getItems()
                        .get(0)
                        .getMedicineName()
        );

        verify(prescriptionRepository)
                .save(any(Prescription.class));
    }

    @Test
    void createPrescription_shouldRejectNullTenant() {

        PrescriptionCreateRequest request =
                createRequest();

        assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionService.createPrescription(
                        null,
                        request
                )
        );

        verifyNoInteractions(
                consultationRepository,
                prescriptionRepository
        );
    }

    @Test
    void createPrescription_shouldRejectNullRequest() {

        assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionService.createPrescription(
                        tenantId,
                        null
                )
        );

        verifyNoInteractions(
                consultationRepository,
                prescriptionRepository
        );
    }

    @Test
    void createPrescription_shouldRejectMissingConsultationId() {

        PrescriptionCreateRequest request =
                createRequest();

        request.setConsultationId(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionService.createPrescription(
                        tenantId,
                        request
                )
        );

        verifyNoInteractions(
                consultationRepository,
                prescriptionRepository
        );
    }

    @Test
    void createPrescription_shouldRejectEmptyItems() {

        PrescriptionCreateRequest request =
                createRequest();

        request.setItems(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionService.createPrescription(
                        tenantId,
                        request
                )
        );

        verifyNoInteractions(
                consultationRepository,
                prescriptionRepository
        );
    }

    @Test
    void createPrescription_shouldRejectMissingConsultation() {

        PrescriptionCreateRequest request =
                createRequest();

        when(consultationRepository.findByIdAndTenantId(
                consultationId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionService.createPrescription(
                        tenantId,
                        request
                )
        );

        verify(prescriptionRepository, never())
                .save(any());
    }

    @Test
    void createPrescription_shouldRejectCrossTenantConsultation() {

        PrescriptionCreateRequest request =
                createRequest();

        when(consultationRepository.findByIdAndTenantId(
                consultationId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionService.createPrescription(
                        tenantId,
                        request
                )
        );

        verify(prescriptionRepository, never())
                .save(any());
    }

    @Test
    void createPrescription_shouldRejectIncompleteConsultation() {

        PrescriptionCreateRequest request =
                createRequest();

        consultation.setStatus("IN_PROGRESS");

        when(consultationRepository.findByIdAndTenantId(
                consultationId,
                tenantId
        )).thenReturn(Optional.of(consultation));

        assertThrows(
                IllegalStateException.class,
                () -> prescriptionService.createPrescription(
                        tenantId,
                        request
                )
        );

        verify(prescriptionRepository, never())
                .save(any());
    }

    @Test
    void createPrescription_shouldRejectDuplicatePrescription() {

        PrescriptionCreateRequest request =
                createRequest();

        when(consultationRepository.findByIdAndTenantId(
                consultationId,
                tenantId
        )).thenReturn(Optional.of(consultation));

        when(prescriptionRepository
                .existsByConsultationIdAndTenantId(
                        consultationId,
                        tenantId
                )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> prescriptionService.createPrescription(
                        tenantId,
                        request
                )
        );

        verify(prescriptionRepository, never())
                .save(any());
    }

    @Test
    void getPrescriptionById_shouldReturnPrescription() {

        Prescription prescription =
                createPrescription();

        when(prescriptionRepository.findByIdAndTenantId(
                prescriptionId,
                tenantId
        )).thenReturn(Optional.of(prescription));

        PrescriptionResponse response =
                prescriptionService.getPrescriptionById(
                        tenantId,
                        prescriptionId
                );

        assertNotNull(response);
        assertEquals(
                prescriptionId,
                response.getId()
        );
    }

    @Test
    void getPrescriptionById_shouldRejectCrossTenantAccess() {

        when(prescriptionRepository.findByIdAndTenantId(
                prescriptionId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionService.getPrescriptionById(
                        tenantId,
                        prescriptionId
                )
        );
    }

    @Test
    void getByConsultationId_shouldReturnPrescription() {

        Prescription prescription =
                createPrescription();

        when(prescriptionRepository
                .findByConsultationIdAndTenantId(
                        consultationId,
                        tenantId
                )).thenReturn(Optional.of(prescription));

        PrescriptionResponse response =
                prescriptionService.getByConsultationId(
                        tenantId,
                        consultationId
                );

        assertNotNull(response);
        assertEquals(
                consultationId,
                response.getConsultationId()
        );
    }

    @Test
    void getPatientPrescriptions_shouldReturnHistory() {

        Prescription prescription =
                createPrescription();

        when(prescriptionRepository
                .findByTenantIdAndPatientIdOrderByPrescriptionDateDesc(
                        tenantId,
                        patientId
                )).thenReturn(List.of(prescription));

        List<PrescriptionResponse> result =
                prescriptionService.getPatientPrescriptions(
                        tenantId,
                        patientId
                );

        assertEquals(1, result.size());
        assertEquals(
                prescriptionId,
                result.get(0).getId()
        );
    }

    @Test
    void getDoctorPrescriptions_shouldReturnHistory() {

        Prescription prescription =
                createPrescription();

        when(prescriptionRepository
                .findByTenantIdAndDoctorIdOrderByPrescriptionDateDesc(
                        tenantId,
                        doctorId
                )).thenReturn(List.of(prescription));

        List<PrescriptionResponse> result =
                prescriptionService.getDoctorPrescriptions(
                        tenantId,
                        doctorId
                );

        assertEquals(1, result.size());
    }

    @Test
    void getClinicPrescriptions_shouldReturnHistory() {

        Prescription prescription =
                createPrescription();

        when(prescriptionRepository
                .findByTenantIdAndClinicIdOrderByPrescriptionDateDesc(
                        tenantId,
                        clinicId
                )).thenReturn(List.of(prescription));

        List<PrescriptionResponse> result =
                prescriptionService.getClinicPrescriptions(
                        tenantId,
                        clinicId
                );

        assertEquals(1, result.size());
    }

    @Test
    void updatePrescription_shouldUpdateDraft() {

        Prescription prescription =
                createPrescription();

        PrescriptionUpdateRequest request =
                new PrescriptionUpdateRequest();

        request.setDiagnosis("Updated diagnosis");
        request.setNotes("Updated notes");
        request.setItems(List.of(
                createItemRequest(
                        "Brahmi"
                ),
                createItemRequest(
                        "Triphala"
                )
        ));

        when(prescriptionRepository.findByIdAndTenantId(
                prescriptionId,
                tenantId
        )).thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PrescriptionResponse response =
                prescriptionService.updatePrescription(
                        tenantId,
                        prescriptionId,
                        request
                );

        assertEquals(
                "Updated diagnosis",
                response.getDiagnosis()
        );

        assertEquals(
                "Updated notes",
                response.getNotes()
        );

        assertEquals(
                2,
                response.getItems().size()
        );

        assertEquals(
                "Brahmi",
                response.getItems()
                        .get(0)
                        .getMedicineName()
        );
    }

    @Test
    void updatePrescription_shouldRejectIssuedPrescription() {

        Prescription prescription =
                createPrescription();

        prescription.setStatus(
                PrescriptionStatus.ISSUED.name()
        );

        PrescriptionUpdateRequest request =
                new PrescriptionUpdateRequest();

        request.setItems(List.of(
                createItemRequest("Brahmi")
        ));

        when(prescriptionRepository.findByIdAndTenantId(
                prescriptionId,
                tenantId
        )).thenReturn(Optional.of(prescription));

        assertThrows(
                IllegalStateException.class,
                () -> prescriptionService.updatePrescription(
                        tenantId,
                        prescriptionId,
                        request
                )
        );

        verify(prescriptionRepository, never())
                .save(any());
    }

    @Test
    void issuePrescription_shouldIssueDraft() {

        Prescription prescription =
                createPrescription();

        when(prescriptionRepository.findByIdAndTenantId(
                prescriptionId,
                tenantId
        )).thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PrescriptionResponse response =
                prescriptionService.issuePrescription(
                        tenantId,
                        prescriptionId
                );

        assertEquals(
                PrescriptionStatus.ISSUED.name(),
                response.getStatus()
        );

        verify(prescriptionRepository)
                .save(any(Prescription.class));
    }

    @Test
    void issuePrescription_shouldRejectAlreadyIssued() {

        Prescription prescription =
                createPrescription();

        prescription.setStatus(
                PrescriptionStatus.ISSUED.name()
        );

        when(prescriptionRepository.findByIdAndTenantId(
                prescriptionId,
                tenantId
        )).thenReturn(Optional.of(prescription));

        assertThrows(
                IllegalStateException.class,
                () -> prescriptionService.issuePrescription(
                        tenantId,
                        prescriptionId
                )
        );

        verify(prescriptionRepository, never())
                .save(any());
    }

    @Test
    void issuePrescription_shouldRejectPrescriptionWithoutItems() {

        Prescription prescription =
                createPrescription();

        prescription.getItems().clear();

        when(prescriptionRepository.findByIdAndTenantId(
                prescriptionId,
                tenantId
        )).thenReturn(Optional.of(prescription));

        assertThrows(
                IllegalStateException.class,
                () -> prescriptionService.issuePrescription(
                        tenantId,
                        prescriptionId
                )
        );

        verify(prescriptionRepository, never())
                .save(any());
    }

    private PrescriptionCreateRequest createRequest() {

        PrescriptionCreateRequest request =
                new PrescriptionCreateRequest();

        request.setConsultationId(
                consultationId
        );

        request.setDiagnosis(
                "Stress-related headache"
        );

        request.setNotes(
                "Take with warm water"
        );

        request.setItems(List.of(
                createItemRequest("Ashwagandha")
        ));

        return request;
    }

    private PrescriptionItemRequest createItemRequest(
            String medicineName
    ) {
        PrescriptionItemRequest request =
                new PrescriptionItemRequest();

        request.setMedicineName(medicineName);
        request.setMedicineType("Tablet");
        request.setDosage("500 mg");
        request.setFrequency("Twice daily");
        request.setDuration("30 days");
        request.setRoute("Oral");
        request.setInstructions(
                "After meals"
        );
        request.setQuantity("60");

        return request;
    }

    private Prescription createPrescription() {

        Prescription prescription =
                new Prescription();

        prescription.setId(prescriptionId);
        prescription.setTenantId(tenantId);
        prescription.setClinicId(clinicId);
        prescription.setConsultationId(consultationId);
        prescription.setAppointmentId(appointmentId);
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctorId);
        prescription.setPrescriptionDate(
                consultation.getConsultationDate()
        );
        prescription.setDiagnosis(
                "Stress-related headache"
        );
        prescription.setNotes(
                "Take with warm water"
        );
        prescription.setStatus(
                PrescriptionStatus.DRAFT.name()
        );

        PrescriptionItemRequest itemRequest =
                createItemRequest("Ashwagandha");

        com.ayurclinic.prescription.entity.PrescriptionItem item =
                new com.ayurclinic.prescription.entity.PrescriptionItem();

        item.setMedicineName(
                itemRequest.getMedicineName()
        );
        item.setMedicineType(
                itemRequest.getMedicineType()
        );
        item.setDosage(
                itemRequest.getDosage()
        );
        item.setFrequency(
                itemRequest.getFrequency()
        );
        item.setDuration(
                itemRequest.getDuration()
        );
        item.setRoute(
                itemRequest.getRoute()
        );
        item.setInstructions(
                itemRequest.getInstructions()
        );
        item.setQuantity(
                itemRequest.getQuantity()
        );
        item.setSortOrder(0);

        prescription.addItem(item);

        return prescription;
    }
}

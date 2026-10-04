package com.ayurclinic.followup.service;

import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.followup.dto.FollowUpCreateRequest;
import com.ayurclinic.followup.dto.FollowUpUpdateRequest;
import com.ayurclinic.followup.entity.FollowUp;
import com.ayurclinic.followup.repository.FollowUpRepository;
import com.ayurclinic.notification.service.FollowUpReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowUpServiceTest {

    @Mock
    private FollowUpRepository followUpRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private FollowUpReminderService followUpReminderService;

    private FollowUpService followUpService;

    private UUID tenantId;
    private UUID consultationId;
    private UUID clinicId;
    private UUID patientId;
    private UUID doctorId;

    @BeforeEach
    void setUp() {

        followUpRepository =
                mock(FollowUpRepository.class);

        consultationRepository =
                mock(ConsultationRepository.class);

        followUpReminderService =
                mock(FollowUpReminderService.class);

        followUpService =
                new FollowUpService(
                        followUpRepository,
                        consultationRepository,
                        followUpReminderService
                );

        tenantId = UUID.randomUUID();
        consultationId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
    }

    @Test
    void createFollowUp_shouldCreateScheduledFollowUp() {

        Consultation consultation = new Consultation();

        consultation.setId(consultationId);
        consultation.setClinicId(clinicId);
        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorId);
        consultation.setStatus("COMPLETED");

        FollowUpCreateRequest request =
                new FollowUpCreateRequest();

        request.setConsultationId(consultationId);
        request.setFollowUpDate(
                LocalDate.now().plusDays(7)
        );
        request.setReason(
                "Review treatment progress"
        );
        request.setNotes(
                "Continue medicines"
        );

        when(
                consultationRepository.findByIdAndTenantId(
                        consultationId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(consultation)
        );

        when(
                followUpRepository.save(
                        any(FollowUp.class)
                )
        ).thenAnswer(invocation -> {

            FollowUp followUp =
                    invocation.getArgument(0);

            followUp.setId(
                    UUID.randomUUID()
            );

            return followUp;
        });

        var response =
                followUpService.createFollowUp(
                        tenantId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                tenantId,
                response.getTenantId()
        );

        assertEquals(
                clinicId,
                response.getClinicId()
        );

        assertEquals(
                patientId,
                response.getPatientId()
        );

        assertEquals(
                doctorId,
                response.getDoctorId()
        );

        assertEquals(
                "SCHEDULED",
                response.getStatus()
        );

        verify(
                consultationRepository
        ).findByIdAndTenantId(
                consultationId,
                tenantId
        );

        verify(
                followUpRepository
        ).save(
                any(FollowUp.class)
        );

        verify(
                followUpReminderService
        ).createReminderForFollowUp(
                any(FollowUp.class)
        );
    }

    @Test
    void createFollowUp_shouldRejectCrossTenantConsultation() {

        when(
                consultationRepository.findByIdAndTenantId(
                        consultationId,
                        tenantId
                )
        ).thenReturn(
                Optional.empty()
        );

        FollowUpCreateRequest request =
                new FollowUpCreateRequest();

        request.setConsultationId(
                consultationId
        );

        request.setFollowUpDate(
                LocalDate.now().plusDays(7)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                followUpService.createFollowUp(
                                        tenantId,
                                        request
                                )
                );

        assertEquals(
                "Consultation not found",
                exception.getMessage()
        );

        verify(
                consultationRepository
        ).findByIdAndTenantId(
                consultationId,
                tenantId
        );

        verifyNoInteractions(
                followUpRepository
        );

        verifyNoInteractions(
                followUpReminderService
        );
    }

    @Test
    void createFollowUp_shouldRejectIncompleteConsultation() {

        Consultation consultation =
                new Consultation();

        consultation.setId(
                consultationId
        );

        consultation.setClinicId(
                clinicId
        );

        consultation.setPatientId(
                patientId
        );

        consultation.setDoctorId(
                doctorId
        );

        consultation.setStatus(
                "IN_PROGRESS"
        );

        FollowUpCreateRequest request =
                new FollowUpCreateRequest();

        request.setConsultationId(
                consultationId
        );

        request.setFollowUpDate(
                LocalDate.now().plusDays(7)
        );

        when(
                consultationRepository.findByIdAndTenantId(
                        consultationId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(consultation)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                followUpService.createFollowUp(
                                        tenantId,
                                        request
                                )
                );

        assertEquals(
                "Follow-up can only be created for a completed consultation",
                exception.getMessage()
        );

        verifyNoInteractions(
                followUpRepository
        );

        verifyNoInteractions(
                followUpReminderService
        );
    }

    @Test
    void updateFollowUp_shouldReschedulePendingReminder() {

        UUID followUpId =
                UUID.randomUUID();

        FollowUp followUp =
                createScheduledFollowUp(
                        followUpId,
                        LocalDate.of(
                                2026,
                                10,
                                15
                        )
                );

        FollowUpUpdateRequest request =
                new FollowUpUpdateRequest();

        request.setFollowUpDate(
                LocalDate.of(
                        2026,
                        10,
                        20
                )
        );

        when(
                followUpRepository.findByIdAndTenantId(
                        followUpId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(followUp)
        );

        when(
                followUpRepository.save(
                        any(FollowUp.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        var response =
                followUpService.updateFollowUp(
                        tenantId,
                        followUpId,
                        request
                );

        assertEquals(
                LocalDate.of(
                        2026,
                        10,
                        20
                ),
                response.getFollowUpDate()
        );

        verify(
                followUpRepository
        ).save(
                followUp
        );

        verify(
                followUpReminderService
        ).rescheduleReminderForFollowUp(
                followUp
        );
    }

    @Test
    void completeFollowUp_shouldChangeStatusToCompleted() {

        UUID followUpId =
                UUID.randomUUID();

        FollowUp followUp =
                createScheduledFollowUp(
                        followUpId,
                        LocalDate.now().plusDays(7)
                );

        when(
                followUpRepository.findByIdAndTenantId(
                        followUpId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(followUp)
        );

        when(
                followUpRepository.save(
                        any(FollowUp.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        var response =
                followUpService.completeFollowUp(
                        tenantId,
                        followUpId
                );

        assertEquals(
                "COMPLETED",
                response.getStatus()
        );

        verify(
                followUpReminderService,
                never()
        ).cancelPendingReminderForFollowUp(
                any(FollowUp.class)
        );
    }

    @Test
    void cancelFollowUp_shouldCancelPendingReminder() {

        UUID followUpId =
                UUID.randomUUID();

        FollowUp followUp =
                createScheduledFollowUp(
                        followUpId,
                        LocalDate.now().plusDays(7)
                );

        when(
                followUpRepository.findByIdAndTenantId(
                        followUpId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(followUp)
        );

        when(
                followUpRepository.save(
                        any(FollowUp.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        var response =
                followUpService.cancelFollowUp(
                        tenantId,
                        followUpId
                );

        assertEquals(
                "CANCELLED",
                response.getStatus()
        );

        verify(
                followUpReminderService
        ).cancelPendingReminderForFollowUp(
                followUp
        );
    }

    @Test
    void cancelFollowUp_shouldNotCancelReminderForAlreadyCompletedFollowUp() {

        UUID followUpId =
                UUID.randomUUID();

        FollowUp followUp =
                createScheduledFollowUp(
                        followUpId,
                        LocalDate.now().plusDays(7)
                );

        followUp.setStatus(
                "COMPLETED"
        );

        when(
                followUpRepository.findByIdAndTenantId(
                        followUpId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(followUp)
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        followUpService.cancelFollowUp(
                                tenantId,
                                followUpId
                        )
        );

        verify(
                followUpRepository,
                never()
        ).save(
                any(FollowUp.class)
        );

        verify(
                followUpReminderService,
                never()
        ).cancelPendingReminderForFollowUp(
                any(FollowUp.class)
        );
    }

    @Test
    void completeFollowUp_shouldRejectAlreadyCompletedFollowUp() {

        UUID followUpId =
                UUID.randomUUID();

        FollowUp followUp =
                createScheduledFollowUp(
                        followUpId,
                        LocalDate.now().plusDays(7)
                );

        followUp.setStatus(
                "COMPLETED"
        );

        when(
                followUpRepository.findByIdAndTenantId(
                        followUpId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(followUp)
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        followUpService.completeFollowUp(
                                tenantId,
                                followUpId
                        )
        );

        verify(
                followUpRepository,
                never()
        ).save(
                any(FollowUp.class)
        );
    }

    @Test
    void getFollowUp_shouldRejectCrossTenantAccess() {

        UUID followUpId =
                UUID.randomUUID();

        when(
                followUpRepository.findByIdAndTenantId(
                        followUpId,
                        tenantId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                followUpService.getFollowUp(
                                        tenantId,
                                        followUpId
                                )
                );

        assertEquals(
                "Follow-up not found",
                exception.getMessage()
        );
    }

    private FollowUp createScheduledFollowUp(
            UUID followUpId,
            LocalDate followUpDate
    ) {

        FollowUp followUp =
                new FollowUp();

        followUp.setId(
                followUpId
        );

        followUp.setTenantId(
                tenantId
        );

        followUp.setClinicId(
                clinicId
        );

        followUp.setPatientId(
                patientId
        );

        followUp.setDoctorId(
                doctorId
        );

        followUp.setConsultationId(
                consultationId
        );

        followUp.setFollowUpDate(
                followUpDate
        );

        followUp.setStatus(
                "SCHEDULED"
        );

        return followUp;
    }
}
package com.ayurclinic.followup.service;

import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.followup.dto.FollowUpCreateRequest;
import com.ayurclinic.followup.dto.FollowUpResponse;
import com.ayurclinic.followup.dto.FollowUpUpdateRequest;
import com.ayurclinic.followup.entity.FollowUp;
import com.ayurclinic.followup.enums.FollowUpStatus;
import com.ayurclinic.followup.repository.FollowUpRepository;
import com.ayurclinic.notification.service.FollowUpReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowUpService {

    private final FollowUpRepository followUpRepository;

    private final ConsultationRepository consultationRepository;

    private final FollowUpReminderService followUpReminderService;

    public FollowUpResponse createFollowUp(
            UUID tenantId,
            FollowUpCreateRequest request
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Follow-up request is required"
            );
        }

        if (request.getConsultationId() == null) {
            throw new IllegalArgumentException(
                    "Consultation ID is required"
            );
        }

        Consultation consultation =
                consultationRepository
                        .findByIdAndTenantId(
                                request.getConsultationId(),
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Consultation not found"
                                )
                        );

        String consultationStatus =
                String.valueOf(
                        consultation.getStatus()
                );

        if (!"COMPLETED".equals(
                consultationStatus
        )) {
            throw new IllegalStateException(
                    "Follow-up can only be created for a completed consultation"
            );
        }

        FollowUp followUp = new FollowUp();

        followUp.setTenantId(tenantId);

        followUp.setClinicId(
                consultation.getClinicId()
        );

        followUp.setPatientId(
                consultation.getPatientId()
        );

        followUp.setDoctorId(
                consultation.getDoctorId()
        );

        followUp.setConsultationId(
                consultation.getId()
        );

        followUp.setAppointmentId(
                request.getAppointmentId()
        );

        followUp.setFollowUpDate(
                request.getFollowUpDate()
        );

        followUp.setReason(
                request.getReason()
        );

        followUp.setNotes(
                request.getNotes()
        );

        followUp.setStatus(
                FollowUpStatus.SCHEDULED.name()
        );

        followUp.setReminderSent(false);

        FollowUp saved =
                followUpRepository.save(
                        followUp
                );

        /*
         * Automatically create the follow-up reminder
         * after the follow-up has been successfully saved.
         */
        followUpReminderService.createReminderForFollowUp(
                saved
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FollowUpResponse getFollowUp(
            UUID tenantId,
            UUID followUpId
    ) {

        FollowUp followUp =
                followUpRepository
                        .findByIdAndTenantId(
                                followUpId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Follow-up not found"
                                )
                        );

        return toResponse(followUp);
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> getPatientFollowUps(
            UUID tenantId,
            UUID patientId
    ) {

        return followUpRepository
                .findByTenantIdAndPatientIdOrderByFollowUpDateAsc(
                        tenantId,
                        patientId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> getDoctorFollowUps(
            UUID tenantId,
            UUID doctorId
    ) {

        return followUpRepository
                .findByTenantIdAndDoctorIdOrderByFollowUpDateAsc(
                        tenantId,
                        doctorId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> getClinicFollowUps(
            UUID tenantId,
            UUID clinicId
    ) {

        return followUpRepository
                .findByTenantIdAndClinicIdOrderByFollowUpDateAsc(
                        tenantId,
                        clinicId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> getFollowUpsByDate(
            UUID tenantId,
            LocalDate date
    ) {

        return followUpRepository
                .findByTenantIdAndFollowUpDateOrderByFollowUpDateAsc(
                        tenantId,
                        date
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FollowUpResponse updateFollowUp(
            UUID tenantId,
            UUID followUpId,
            FollowUpUpdateRequest request
    ) {

        FollowUp followUp =
                getEntity(
                        tenantId,
                        followUpId
                );

        validateScheduled(
                followUp
        );

        if (request == null) {
            throw new IllegalArgumentException(
                    "Follow-up request is required"
            );
        }

        if (request.getFollowUpDate() != null) {
            followUp.setFollowUpDate(
                    request.getFollowUpDate()
            );
        }

        if (request.getReason() != null) {
            followUp.setReason(
                    request.getReason()
            );
        }

        if (request.getNotes() != null) {
            followUp.setNotes(
                    request.getNotes()
            );
        }

        FollowUp saved =
                followUpRepository.save(
                        followUp
                );

        /*
         * If the follow-up date changed, update the
         * existing pending reminder schedule.
         */
        followUpReminderService.rescheduleReminderForFollowUp(
                saved
        );

        return toResponse(saved);
    }

    @Transactional
    public FollowUpResponse completeFollowUp(
            UUID tenantId,
            UUID followUpId
    ) {

        FollowUp followUp =
                getEntity(
                        tenantId,
                        followUpId
                );

        validateScheduled(
                followUp
        );

        followUp.setStatus(
                FollowUpStatus.COMPLETED.name()
        );

        return toResponse(
                followUpRepository.save(
                        followUp
                )
        );
    }

    @Transactional
    public FollowUpResponse cancelFollowUp(
            UUID tenantId,
            UUID followUpId
    ) {

        FollowUp followUp =
                getEntity(
                        tenantId,
                        followUpId
                );

        validateScheduled(
                followUp
        );

        followUp.setStatus(
                FollowUpStatus.CANCELLED.name()
        );

        FollowUp saved =
                followUpRepository.save(
                        followUp
                );

        /*
         * Cancel any pending reminder so a cancelled
         * follow-up does not generate a patient reminder.
         */
        followUpReminderService.cancelPendingReminderForFollowUp(
                saved
        );

        return toResponse(saved);
    }

    @Transactional
    public FollowUpResponse markFollowUpMissed(
            UUID tenantId,
            UUID followUpId
    ) {

        FollowUp followUp =
                getEntity(
                        tenantId,
                        followUpId
                );

        validateScheduled(
                followUp
        );

        followUp.setStatus(
                FollowUpStatus.MISSED.name()
        );

        return toResponse(
                followUpRepository.save(
                        followUp
                )
        );
    }

    private FollowUp getEntity(
            UUID tenantId,
            UUID followUpId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }

        return followUpRepository
                .findByIdAndTenantId(
                        followUpId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Follow-up not found"
                        )
                );
    }

    private void validateScheduled(
            FollowUp followUp
    ) {

        if (!FollowUpStatus.SCHEDULED.name()
                .equals(followUp.getStatus())) {

            throw new IllegalStateException(
                    "Only scheduled follow-ups can be modified"
            );
        }
    }

    private FollowUpResponse toResponse(
            FollowUp followUp
    ) {

        return FollowUpResponse.builder()
                .id(followUp.getId())
                .tenantId(followUp.getTenantId())
                .clinicId(followUp.getClinicId())
                .patientId(followUp.getPatientId())
                .doctorId(followUp.getDoctorId())
                .consultationId(
                        followUp.getConsultationId()
                )
                .appointmentId(
                        followUp.getAppointmentId()
                )
                .followUpDate(
                        followUp.getFollowUpDate()
                )
                .reason(
                        followUp.getReason()
                )
                .notes(
                        followUp.getNotes()
                )
                .status(
                        followUp.getStatus()
                )
                .reminderSent(
                        followUp.isReminderSent()
                )
                .createdAt(
                        followUp.getCreatedAt()
                )
                .updatedAt(
                        followUp.getUpdatedAt()
                )
                .build();
    }
}

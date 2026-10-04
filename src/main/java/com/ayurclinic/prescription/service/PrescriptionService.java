package com.ayurclinic.prescription.service;

import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.prescription.dto.*;
import com.ayurclinic.prescription.entity.Prescription;
import com.ayurclinic.prescription.entity.PrescriptionItem;
import com.ayurclinic.prescription.enums.PrescriptionStatus;
import com.ayurclinic.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationRepository consultationRepository;

    public PrescriptionResponse createPrescription(
            UUID tenantId,
            PrescriptionCreateRequest request
    ) {
        validateTenant(tenantId);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Prescription request cannot be null"
            );
        }

        if (request.getConsultationId() == null) {
            throw new IllegalArgumentException(
                    "Consultation ID is required"
            );
        }

        if (request.getItems() == null ||
                request.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one prescription item is required"
            );
        }

        Consultation consultation =
                consultationRepository
                        .findByIdAndTenantId(
                                request.getConsultationId(),
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Consultation not found"
                                )
                        );

        if (!"COMPLETED".equals(consultation.getStatus())) {
            throw new IllegalStateException(
                    "Prescription can only be created for a completed consultation"
            );
        }

        if (prescriptionRepository
                .existsByConsultationIdAndTenantId(
                        request.getConsultationId(),
                        tenantId
                )) {

            throw new IllegalStateException(
                    "Prescription already exists for this consultation"
            );
        }

        Prescription prescription = new Prescription();

        prescription.setTenantId(tenantId);
        prescription.setClinicId(consultation.getClinicId());
        prescription.setConsultationId(consultation.getId());
        prescription.setAppointmentId(
                consultation.getAppointmentId()
        );
        prescription.setPatientId(
                consultation.getPatientId()
        );
        prescription.setDoctorId(
                consultation.getDoctorId()
        );
        prescription.setPrescriptionDate(
                consultation.getConsultationDate()
        );
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setNotes(request.getNotes());
        prescription.setStatus(
                PrescriptionStatus.DRAFT.name()
        );

        addItems(
                prescription,
                request.getItems()
        );

        Prescription saved =
                prescriptionRepository.save(prescription);

        return PrescriptionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(
            UUID tenantId,
            UUID prescriptionId
    ) {
        validateTenant(tenantId);

        Prescription prescription =
                prescriptionRepository
                        .findByIdAndTenantId(
                                prescriptionId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Prescription not found"
                                )
                        );

        return PrescriptionResponse.fromEntity(
                prescription
        );
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getByConsultationId(
            UUID tenantId,
            UUID consultationId
    ) {
        validateTenant(tenantId);

        Prescription prescription =
                prescriptionRepository
                        .findByConsultationIdAndTenantId(
                                consultationId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Prescription not found"
                                )
                        );

        return PrescriptionResponse.fromEntity(
                prescription
        );
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPatientPrescriptions(
            UUID tenantId,
            UUID patientId
    ) {
        validateTenant(tenantId);

        return prescriptionRepository
                .findByTenantIdAndPatientIdOrderByPrescriptionDateDesc(
                        tenantId,
                        patientId
                )
                .stream()
                .map(PrescriptionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getDoctorPrescriptions(
            UUID tenantId,
            UUID doctorId
    ) {
        validateTenant(tenantId);

        return prescriptionRepository
                .findByTenantIdAndDoctorIdOrderByPrescriptionDateDesc(
                        tenantId,
                        doctorId
                )
                .stream()
                .map(PrescriptionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getClinicPrescriptions(
            UUID tenantId,
            UUID clinicId
    ) {
        validateTenant(tenantId);

        return prescriptionRepository
                .findByTenantIdAndClinicIdOrderByPrescriptionDateDesc(
                        tenantId,
                        clinicId
                )
                .stream()
                .map(PrescriptionResponse::fromEntity)
                .toList();
    }

    public PrescriptionResponse updatePrescription(
            UUID tenantId,
            UUID prescriptionId,
            PrescriptionUpdateRequest request
    ) {
        validateTenant(tenantId);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Prescription update request cannot be null"
            );
        }

        if (request.getItems() == null ||
                request.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one prescription item is required"
            );
        }

        Prescription prescription =
                prescriptionRepository
                        .findByIdAndTenantId(
                                prescriptionId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Prescription not found"
                                )
                        );

        if (PrescriptionStatus.ISSUED.name()
                .equals(prescription.getStatus())) {

            throw new IllegalStateException(
                    "Issued prescription cannot be modified"
            );
        }

        prescription.setDiagnosis(
                request.getDiagnosis()
        );

        prescription.setNotes(
                request.getNotes()
        );

        prescription.getItems().clear();

        addItems(
                prescription,
                request.getItems()
        );

        Prescription saved =
                prescriptionRepository.save(prescription);

        return PrescriptionResponse.fromEntity(saved);
    }

    public PrescriptionResponse issuePrescription(
            UUID tenantId,
            UUID prescriptionId
    ) {
        validateTenant(tenantId);

        Prescription prescription =
                prescriptionRepository
                        .findByIdAndTenantId(
                                prescriptionId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Prescription not found"
                                )
                        );

        if (PrescriptionStatus.ISSUED.name()
                .equals(prescription.getStatus())) {

            throw new IllegalStateException(
                    "Prescription is already issued"
            );
        }

        if (prescription.getItems() == null ||
                prescription.getItems().isEmpty()) {

            throw new IllegalStateException(
                    "Prescription must contain at least one item"
            );
        }

        prescription.setStatus(
                PrescriptionStatus.ISSUED.name()
        );

        Prescription saved =
                prescriptionRepository.save(prescription);

        return PrescriptionResponse.fromEntity(saved);
    }

    private void addItems(
            Prescription prescription,
            List<PrescriptionItemRequest> requests
    ) {
        for (int index = 0;
             index < requests.size();
             index++) {

            PrescriptionItemRequest request =
                    requests.get(index);

            if (request == null) {
                throw new IllegalArgumentException(
                        "Prescription item cannot be null"
                );
            }

            if (request.getMedicineName() == null ||
                    request.getMedicineName().isBlank()) {

                throw new IllegalArgumentException(
                        "Medicine name is required"
                );
            }

            PrescriptionItem item =
                    new PrescriptionItem();

            item.setMedicineName(
                    request.getMedicineName()
            );

            item.setMedicineType(
                    request.getMedicineType()
            );

            item.setDosage(
                    request.getDosage()
            );

            item.setFrequency(
                    request.getFrequency()
            );

            item.setDuration(
                    request.getDuration()
            );

            item.setRoute(
                    request.getRoute()
            );

            item.setInstructions(
                    request.getInstructions()
            );

            item.setQuantity(
                    request.getQuantity()
            );

            item.setSortOrder(
                    request.getSortOrder() != null
                            ? request.getSortOrder()
                            : index
            );

            prescription.addItem(item);
        }
    }

    private void validateTenant(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }
    }
}
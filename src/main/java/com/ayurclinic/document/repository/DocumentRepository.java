package com.ayurclinic.document.repository;

import com.ayurclinic.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository
        extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<Document> findByTenantIdOrderByCreatedAtDesc(
            UUID tenantId
    );

    List<Document> findByTenantIdAndPatientIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID patientId
    );

    List<Document> findByTenantIdAndConsultationIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID consultationId
    );

    List<Document> findByTenantIdAndFollowupIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID followupId
    );

    List<Document> findByTenantIdAndClinicIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID clinicId
    );
}

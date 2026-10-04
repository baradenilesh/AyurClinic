package com.ayurclinic.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PatientNumberCounterRepository
        extends JpaRepository<PatientNumberCounterRepository.PatientNumberCounter, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO patient_number_counters (tenant_id, next_number)
            VALUES (:tenantId, 2)
            ON CONFLICT (tenant_id)
            DO UPDATE SET next_number =
                patient_number_counters.next_number + 1
            """, nativeQuery = true)
    void incrementCounter(@Param("tenantId") UUID tenantId);

    @Query(value = """
            SELECT next_number - 1
            FROM patient_number_counters
            WHERE tenant_id = :tenantId
            """, nativeQuery = true)
    Long getCurrentNumber(@Param("tenantId") UUID tenantId);

    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "patient_number_counters")
    class PatientNumberCounter {

        @jakarta.persistence.Id
        @jakarta.persistence.Column(name = "tenant_id")
        private UUID tenantId;

        @jakarta.persistence.Column(name = "next_number")
        private Long nextNumber;

        public UUID getTenantId() {
            return tenantId;
        }

        public void setTenantId(UUID tenantId) {
            this.tenantId = tenantId;
        }

        public Long getNextNumber() {
            return nextNumber;
        }

        public void setNextNumber(Long nextNumber) {
            this.nextNumber = nextNumber;
        }
    }
}
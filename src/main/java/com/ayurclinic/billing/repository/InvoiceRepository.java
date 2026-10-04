package com.ayurclinic.billing.repository;

import com.ayurclinic.billing.entity.Invoice;
import com.ayurclinic.billing.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface InvoiceRepository
        extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<Invoice> findByTenantIdAndInvoiceNumber(
            UUID tenantId,
            String invoiceNumber
    );

    List<Invoice>
    findByTenantIdOrderByInvoiceDateDesc(
            UUID tenantId
    );

    List<Invoice>
    findByTenantIdAndPatientIdOrderByInvoiceDateDesc(
            UUID tenantId,
            UUID patientId
    );

    List<Invoice>
    findByTenantIdAndAppointmentIdOrderByInvoiceDateDesc(
            UUID tenantId,
            UUID appointmentId
    );

    List<Invoice>
    findByTenantIdAndConsultationIdOrderByInvoiceDateDesc(
            UUID tenantId,
            UUID consultationId
    );

    List<Invoice>
    findByTenantIdAndStatusOrderByInvoiceDateDesc(
            UUID tenantId,
            InvoiceStatus status
    );

    @Query("""
        SELECT COALESCE(
            SUM(i.totalAmount - i.paidAmount),
            0
        )
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.status NOT IN (
              com.ayurclinic.billing.enums.InvoiceStatus.PAID,
              com.ayurclinic.billing.enums.InvoiceStatus.CANCELLED
          )
        """)
    BigDecimal sumOutstandingAmountByTenantId(
            @Param("tenantId") UUID tenantId
    );
}
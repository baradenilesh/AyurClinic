package com.ayurclinic.billing.repository;

import com.ayurclinic.billing.entity.Payment;
import com.ayurclinic.billing.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentRepository
        extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<Payment>
    findByTenantIdAndInvoiceIdOrderByPaymentDateDesc(
            UUID tenantId,
            UUID invoiceId
    );

    List<Payment>
    findByTenantIdAndPatientIdOrderByPaymentDateDesc(
            UUID tenantId,
            UUID patientId
    );

    List<Payment>
    findByTenantIdAndStatusOrderByPaymentDateDesc(
            UUID tenantId,
            PaymentStatus status
    );

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.tenantId = :tenantId
          AND p.paymentDate = :paymentDate
          AND p.status = :status
        """)
    BigDecimal sumAmountByTenantIdAndPaymentDateAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("paymentDate") LocalDate paymentDate,
            @Param("status") PaymentStatus status
    );
}
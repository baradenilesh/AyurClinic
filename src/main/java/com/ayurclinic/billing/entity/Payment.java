package com.ayurclinic.billing.entity;

import com.ayurclinic.billing.enums.PaymentMethod;
import com.ayurclinic.billing.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(
                        name = "idx_payments_tenant",
                        columnList = "tenant_id"
                ),
                @Index(
                        name = "idx_payments_clinic",
                        columnList = "clinic_id"
                ),
                @Index(
                        name = "idx_payments_patient",
                        columnList = "patient_id"
                ),
                @Index(
                        name = "idx_payments_invoice",
                        columnList = "invoice_id"
                ),
                @Index(
                        name = "idx_payments_payment_date",
                        columnList = "payment_date"
                ),
                @Index(
                        name = "idx_payments_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "tenant_id",
            nullable = false
    )
    private UUID tenantId;

    @Column(
            name = "clinic_id",
            nullable = false
    )
    private UUID clinicId;

    @Column(
            name = "patient_id",
            nullable = false
    )
    private UUID patientId;

    @Column(
            name = "invoice_id",
            nullable = false
    )
    private UUID invoiceId;

    @Column(
            name = "amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "payment_date",
            nullable = false
    )
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 30
    )
    private PaymentMethod paymentMethod;

    @Column(
            name = "transaction_reference",
            length = 255
    )
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private PaymentStatus status;

    @Column(
            name = "notes",
            length = 1000
    )
    private String notes;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now =
                OffsetDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        if (status == null) {
            status = PaymentStatus.PENDING;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = OffsetDateTime.now();
    }
}
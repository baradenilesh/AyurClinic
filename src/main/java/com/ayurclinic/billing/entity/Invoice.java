package com.ayurclinic.billing.entity;

import com.ayurclinic.billing.enums.InvoiceStatus;
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
        name = "invoices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_invoice_tenant_number",
                        columnNames = {
                                "tenant_id",
                                "invoice_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_invoices_tenant",
                        columnList = "tenant_id"
                ),
                @Index(
                        name = "idx_invoices_clinic",
                        columnList = "clinic_id"
                ),
                @Index(
                        name = "idx_invoices_patient",
                        columnList = "patient_id"
                ),
                @Index(
                        name = "idx_invoices_appointment",
                        columnList = "appointment_id"
                ),
                @Index(
                        name = "idx_invoices_consultation",
                        columnList = "consultation_id"
                ),
                @Index(
                        name = "idx_invoices_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_invoices_invoice_date",
                        columnList = "invoice_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

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

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "consultation_id")
    private UUID consultationId;

    @Column(
            name = "invoice_number",
            nullable = false,
            length = 50
    )
    private String invoiceNumber;

    @Column(
            name = "invoice_date",
            nullable = false
    )
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(
            name = "discount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discount;

    @Column(
            name = "tax",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal tax;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(
            name = "paid_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private InvoiceStatus status;

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

        if (invoiceDate == null) {
            invoiceDate = LocalDate.now();
        }

        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        if (discount == null) {
            discount = BigDecimal.ZERO;
        }

        if (tax == null) {
            tax = BigDecimal.ZERO;
        }

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }

        if (status == null) {
            status = InvoiceStatus.DRAFT;
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

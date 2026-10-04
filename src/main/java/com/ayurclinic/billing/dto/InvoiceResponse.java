 package com.ayurclinic.billing.dto;

import com.ayurclinic.billing.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class InvoiceResponse {

    private UUID id;

    private UUID tenantId;

    private UUID clinicId;

    private UUID patientId;

    private UUID appointmentId;

    private UUID consultationId;

    private String invoiceNumber;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private BigDecimal subtotal;

    private BigDecimal discount;

    private BigDecimal tax;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private BigDecimal outstandingAmount;

    private InvoiceStatus status;

    private String notes;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}

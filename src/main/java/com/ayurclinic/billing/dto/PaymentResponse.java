package com.ayurclinic.billing.dto;

import com.ayurclinic.billing.enums.PaymentMethod;
import com.ayurclinic.billing.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {

    private UUID id;

    private UUID tenantId;

    private UUID clinicId;

    private UUID patientId;

    private UUID invoiceId;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private PaymentMethod paymentMethod;

    private String transactionReference;

    private PaymentStatus status;

    private String notes;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
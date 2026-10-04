package com.ayurclinic.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class InvoiceUpdateRequest {

    private UUID appointmentId;

    private UUID consultationId;

    private LocalDate dueDate;

    @DecimalMin(
            value = "0.00",
            message = "Subtotal cannot be negative"
    )
    private BigDecimal subtotal;

    @DecimalMin(
            value = "0.00",
            message = "Discount cannot be negative"
    )
    private BigDecimal discount;

    @DecimalMin(
            value = "0.00",
            message = "Tax cannot be negative"
    )
    private BigDecimal tax;

    @Size(
            max = 1000,
            message = "Notes cannot exceed 1000 characters"
    )
    private String notes;
}
 package com.ayurclinic.billing.dto;

import com.ayurclinic.billing.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PaymentCreateRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Payment amount must be greater than zero"
    )
    private BigDecimal amount;

    private LocalDate paymentDate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(
            max = 255,
            message = "Transaction reference cannot exceed 255 characters"
    )
    private String transactionReference;

    @Size(
            max = 1000,
            message = "Notes cannot exceed 1000 characters"
    )
    private String notes;
}
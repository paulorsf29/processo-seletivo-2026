package com.example.Ecomerce.dto.payment;

import com.example.Ecomerce.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record PaymentStatusUpdateRequest(

        @NotNull(message = "Status é obrigatório")
        PaymentStatus status
) {
}

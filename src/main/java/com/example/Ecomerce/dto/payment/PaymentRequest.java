package com.example.Ecomerce.dto.payment;

import com.example.Ecomerce.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(

        @NotNull(message = "Pedido é obrigatório")
        Long orderId,

        @NotNull(message = "Forma de pagamento é obrigatória")
        PaymentMethod method
) {
}

package com.example.Ecomerce.dto.order;

import com.example.Ecomerce.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull(message = "Status é obrigatório")
        OrderStatus status
) {
}

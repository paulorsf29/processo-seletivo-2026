package com.example.Ecomerce.dto.payment;

import com.example.Ecomerce.model.Payment;
import com.example.Ecomerce.model.PaymentMethod;
import com.example.Ecomerce.model.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String createdAt,
        String updatedAt
) {
    public static PaymentResponse fromEntity(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null,
                payment.getUpdatedAt() != null ? payment.getUpdatedAt().toString() : null
        );
    }
}

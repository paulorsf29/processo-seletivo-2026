package com.example.Ecomerce.dto.order;

import com.example.Ecomerce.model.Order;
import com.example.Ecomerce.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String userName,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        String couponCode,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        String createdAt,
        String updatedAt
) {
    public static OrderResponse fromEntity(Order order) {
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getName(),
                order.getStatus(),
                order.getTotalAmount().add(discount),
                discount,
                order.getCouponCode(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::fromEntity).toList(),
                order.getCreatedAt() != null ? order.getCreatedAt().toString() : null,
                order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null
        );
    }
}

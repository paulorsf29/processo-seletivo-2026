package com.example.Ecomerce.dto.cart;

import com.example.Ecomerce.model.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        Integer stockQuantity,
        BigDecimal subtotal
) {
    public static CartItemResponse fromEntity(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageUrl(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getProduct().getStockQuantity(),
                subtotal
        );
    }
}

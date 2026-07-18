package com.example.Ecomerce.dto.cart;

import com.example.Ecomerce.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        BigDecimal totalAmount
) {
    public static CartResponse fromEntity(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(CartItemResponse::fromEntity).toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total);
    }
}

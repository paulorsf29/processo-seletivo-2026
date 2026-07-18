package com.example.Ecomerce.controller;

import com.example.Ecomerce.dto.cart.CartItemRequest;
import com.example.Ecomerce.dto.cart.CartResponse;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.security.CurrentUserService;
import com.example.Ecomerce.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(cartService.addItem(user, request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long productId,
            @RequestParam @Min(value = 1, message = "Quantidade deve ser maior que zero") Integer quantity
    ) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(cartService.updateItem(user, productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long productId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(cartService.removeItem(user, productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        User user = currentUserService.getCurrentUser();
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}

package com.example.Ecomerce.controller;

import com.example.Ecomerce.dto.order.OrderRequest;
import com.example.Ecomerce.dto.order.OrderResponse;
import com.example.Ecomerce.dto.order.OrderStatusUpdateRequest;
import com.example.Ecomerce.model.OrderStatus;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.security.CurrentUserService;
import com.example.Ecomerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        User customer = currentUserService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(customer, request));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponse>> myOrders(Pageable pageable) {
        User customer = currentUserService.getCurrentUser();
        return ResponseEntity.ok(orderService.getMyOrders(customer, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        User requester = currentUserService.getCurrentUser();
        return ResponseEntity.ok(orderService.getById(id, requester));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listAll(
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(status, pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        User requester = currentUserService.getCurrentUser();
        return ResponseEntity.ok(orderService.cancelOrder(id, requester));
    }
}

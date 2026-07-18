package com.example.Ecomerce.controller;

import com.example.Ecomerce.dto.payment.PaymentRequest;
import com.example.Ecomerce.dto.payment.PaymentResponse;
import com.example.Ecomerce.dto.payment.PaymentStatusUpdateRequest;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.security.CurrentUserService;
import com.example.Ecomerce.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        User customer = currentUserService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(customer, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        User requester = currentUserService.getCurrentUser();
        return ResponseEntity.ok(paymentService.getById(id, requester));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody PaymentStatusUpdateRequest request) {
        return ResponseEntity.ok(paymentService.updateStatus(id, request.status()));
    }
}

package com.example.Ecomerce.controller;

import com.example.Ecomerce.dto.coupon.CouponValidationRequest;
import com.example.Ecomerce.dto.coupon.CouponValidationResponse;
import com.example.Ecomerce.dto.cart.CartResponse;
import com.example.Ecomerce.model.Coupon;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.security.CurrentUserService;
import com.example.Ecomerce.service.CartService;
import com.example.Ecomerce.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validate(@Valid @RequestBody CouponValidationRequest request) {
        User user = currentUserService.getCurrentUser();
        CartResponse cart = cartService.getCart(user);

        Coupon coupon = couponService.validateForUse(user, request.code(), cart.totalAmount());
        var discount = couponService.calculateDiscount(coupon, cart.totalAmount());

        return ResponseEntity.ok(new CouponValidationResponse(
                coupon.getCode(),
                cart.totalAmount(),
                discount,
                cart.totalAmount().subtract(discount)
        ));
    }
}

package com.example.Ecomerce.controller;

import com.example.Ecomerce.dto.coupon.CouponRequest;
import com.example.Ecomerce.dto.coupon.CouponResponse;
import com.example.Ecomerce.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<Page<CouponResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(couponService.list(pageable));
    }

    @PostMapping
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> update(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.Ecomerce.dto.coupon;

import java.math.BigDecimal;

public record CouponValidationResponse(
        String code,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal finalTotal
) {
}

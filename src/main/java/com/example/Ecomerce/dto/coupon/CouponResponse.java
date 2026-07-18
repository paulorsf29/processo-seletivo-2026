package com.example.Ecomerce.dto.coupon;

import com.example.Ecomerce.model.Coupon;
import com.example.Ecomerce.model.DiscountType;

import java.math.BigDecimal;

public record CouponResponse(
        Long id,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderValue,
        String expirationDate,
        boolean active,
        String createdAt
) {
    public static CouponResponse fromEntity(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderValue(),
                coupon.getExpirationDate() != null ? coupon.getExpirationDate().toString() : null,
                coupon.isActive(),
                coupon.getCreatedAt() != null ? coupon.getCreatedAt().toString() : null
        );
    }
}

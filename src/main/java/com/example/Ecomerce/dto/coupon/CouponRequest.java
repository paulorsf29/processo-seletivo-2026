package com.example.Ecomerce.dto.coupon;

import com.example.Ecomerce.model.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(

        @NotBlank(message = "Código é obrigatório")
        String code,

        @NotNull(message = "Tipo de desconto é obrigatório")
        DiscountType discountType,

        @NotNull(message = "Valor do desconto é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Valor do desconto deve ser maior que zero")
        BigDecimal discountValue,

        BigDecimal minOrderValue,

        LocalDateTime expirationDate,

        Boolean active
) {
}

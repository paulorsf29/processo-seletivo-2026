package com.example.Ecomerce.dto.coupon;

import jakarta.validation.constraints.NotBlank;

public record CouponValidationRequest(

        @NotBlank(message = "Código do cupom é obrigatório")
        String code
) {
}

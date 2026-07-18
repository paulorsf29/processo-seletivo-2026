package com.example.Ecomerce.dto.metrics;

import java.math.BigDecimal;

public record BestSellingProductResponse(
        Long productId,
        String productName,
        Long totalSold,
        BigDecimal totalRevenue
) {
}

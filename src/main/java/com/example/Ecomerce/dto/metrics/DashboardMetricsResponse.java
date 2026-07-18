package com.example.Ecomerce.dto.metrics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardMetricsResponse(
        BigDecimal totalRevenue,
        long totalPayments,
        Map<String, Long> paymentsByStatus,
        long totalOrders,
        Map<String, Long> ordersByStatus,
        List<BestSellingProductResponse> topSellingProducts
) {
}

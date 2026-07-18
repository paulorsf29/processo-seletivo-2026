package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.metrics.BestSellingProductResponse;
import com.example.Ecomerce.dto.metrics.DashboardMetricsResponse;
import com.example.Ecomerce.model.OrderStatus;
import com.example.Ecomerce.model.PaymentStatus;
import com.example.Ecomerce.repository.OrderItemRepository;
import com.example.Ecomerce.repository.OrderRepository;
import com.example.Ecomerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private static final int DEFAULT_TOP_PRODUCTS_LIMIT = 5;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboard() {
        Map<String, Long> paymentsByStatus = new LinkedHashMap<>();
        for (PaymentStatus status : PaymentStatus.values()) {
            paymentsByStatus.put(status.name(), paymentRepository.countByStatus(status));
        }

        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepository.countByStatus(status));
        }

        return new DashboardMetricsResponse(
                paymentRepository.sumApprovedAmount(),
                paymentRepository.count(),
                paymentsByStatus,
                orderRepository.count(),
                ordersByStatus,
                getBestSellingProducts(DEFAULT_TOP_PRODUCTS_LIMIT)
        );
    }

    @Transactional(readOnly = true)
    public List<BestSellingProductResponse> getBestSellingProducts(int limit) {
        return orderItemRepository.findBestSellingProducts(PageRequest.of(0, limit)).stream()
                .map(view -> new BestSellingProductResponse(
                        view.getProductId(),
                        view.getProductName(),
                        view.getTotalSold(),
                        view.getTotalRevenue()
                ))
                .toList();
    }
}

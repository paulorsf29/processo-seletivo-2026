package com.example.Ecomerce.controller;

import com.example.Ecomerce.dto.metrics.BestSellingProductResponse;
import com.example.Ecomerce.dto.metrics.DashboardMetricsResponse;
import com.example.Ecomerce.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
public class AdminMetricsController {

    private final MetricsService metricsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardMetricsResponse> dashboard() {
        return ResponseEntity.ok(metricsService.getDashboard());
    }

    @GetMapping("/best-selling-products")
    public ResponseEntity<List<BestSellingProductResponse>> bestSellingProducts(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(metricsService.getBestSellingProducts(limit));
    }
}

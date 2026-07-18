package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.metrics.BestSellingProductResponse;
import com.example.Ecomerce.dto.metrics.DashboardMetricsResponse;
import com.example.Ecomerce.model.OrderStatus;
import com.example.Ecomerce.model.PaymentStatus;
import com.example.Ecomerce.repository.BestSellingProductView;
import com.example.Ecomerce.repository.OrderItemRepository;
import com.example.Ecomerce.repository.OrderRepository;
import com.example.Ecomerce.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private MetricsService metricsService;

    private BestSellingProductView view(Long productId, String name, long totalSold, String revenue) {
        BestSellingProductView view = mock(BestSellingProductView.class);
        when(view.getProductId()).thenReturn(productId);
        when(view.getProductName()).thenReturn(name);
        when(view.getTotalSold()).thenReturn(totalSold);
        when(view.getTotalRevenue()).thenReturn(new BigDecimal(revenue));
        return view;
    }

    @Test
    void getDashboard_aggregatesAllCounters() {
        when(paymentRepository.sumApprovedAmount()).thenReturn(new BigDecimal("1500.00"));
        when(paymentRepository.count()).thenReturn(12L);
        for (PaymentStatus status : PaymentStatus.values()) {
            when(paymentRepository.countByStatus(status)).thenReturn(status == PaymentStatus.APPROVED ? 8L : 1L);
        }

        when(orderRepository.count()).thenReturn(15L);
        for (OrderStatus status : OrderStatus.values()) {
            when(orderRepository.countByStatus(status)).thenReturn(status == OrderStatus.PAID ? 9L : 2L);
        }

        List<BestSellingProductView> views = List.of(view(1L, "Camisa Brasil", 40L, "4000.00"));
        when(orderItemRepository.findBestSellingProducts(any(Pageable.class))).thenReturn(views);

        DashboardMetricsResponse dashboard = metricsService.getDashboard();

        assertThat(dashboard.totalRevenue()).isEqualByComparingTo("1500.00");
        assertThat(dashboard.totalPayments()).isEqualTo(12L);
        assertThat(dashboard.paymentsByStatus().get(PaymentStatus.APPROVED.name())).isEqualTo(8L);
        assertThat(dashboard.totalOrders()).isEqualTo(15L);
        assertThat(dashboard.ordersByStatus().get(OrderStatus.PAID.name())).isEqualTo(9L);
        assertThat(dashboard.topSellingProducts()).hasSize(1);
        assertThat(dashboard.topSellingProducts().get(0).productName()).isEqualTo("Camisa Brasil");
    }

    @Test
    void getBestSellingProducts_mapsProjectionAndRespectsLimit() {
        List<BestSellingProductView> views = List.of(
                view(1L, "Camisa Brasil", 40L, "4000.00"),
                view(2L, "Camisa Argentina", 30L, "3000.00")
        );
        when(orderItemRepository.findBestSellingProducts(eq(PageRequest.of(0, 3)))).thenReturn(views);

        List<BestSellingProductResponse> result = metricsService.getBestSellingProducts(3);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).productId()).isEqualTo(1L);
        assertThat(result.get(0).totalSold()).isEqualTo(40L);
        assertThat(result.get(0).totalRevenue()).isEqualByComparingTo("4000.00");
        verify(orderItemRepository).findBestSellingProducts(eq(PageRequest.of(0, 3)));
    }
}

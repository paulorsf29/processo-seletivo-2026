package com.example.Ecomerce.repository;

import com.example.Ecomerce.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select oi.product.id as productId,
                   oi.product.name as productName,
                   sum(oi.quantity) as totalSold,
                   sum(oi.subtotal) as totalRevenue
            from OrderItem oi
            where oi.order.status <> com.example.Ecomerce.model.OrderStatus.CANCELED
            group by oi.product.id, oi.product.name
            order by sum(oi.quantity) desc
            """)
    List<BestSellingProductView> findBestSellingProducts(Pageable pageable);
}

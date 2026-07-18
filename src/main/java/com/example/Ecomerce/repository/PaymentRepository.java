package com.example.Ecomerce.repository;

import com.example.Ecomerce.model.Order;
import com.example.Ecomerce.model.Payment;
import com.example.Ecomerce.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    long countByStatus(PaymentStatus status);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = com.example.Ecomerce.model.PaymentStatus.APPROVED")
    BigDecimal sumApprovedAmount();
}

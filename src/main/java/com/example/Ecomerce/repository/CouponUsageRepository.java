package com.example.Ecomerce.repository;

import com.example.Ecomerce.model.Coupon;
import com.example.Ecomerce.model.CouponUsage;
import com.example.Ecomerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    boolean existsByCouponAndUser(Coupon coupon, User user);
}

package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.coupon.CouponRequest;
import com.example.Ecomerce.dto.coupon.CouponResponse;
import com.example.Ecomerce.exception.BusinessException;
import com.example.Ecomerce.exception.ResourceNotFoundException;
import com.example.Ecomerce.model.Coupon;
import com.example.Ecomerce.model.DiscountType;
import com.example.Ecomerce.model.Order;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.repository.CouponRepository;
import com.example.Ecomerce.repository.CouponUsageRepository;
import com.example.Ecomerce.model.CouponUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Transactional(readOnly = true)
    public Page<CouponResponse> list(Pageable pageable) {
        return couponRepository.findAll(pageable).map(CouponResponse::fromEntity);
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        if (couponRepository.findByCodeIgnoreCase(request.code()).isPresent()) {
            throw new BusinessException("Já existe um cupom com o código: " + request.code());
        }
        Coupon coupon = Coupon.builder()
                .code(request.code().toUpperCase())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderValue(request.minOrderValue())
                .expirationDate(request.expirationDate())
                .active(request.active() == null || request.active())
                .build();
        return CouponResponse.fromEntity(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse update(Long id, CouponRequest request) {
        Coupon coupon = findEntity(id);
        coupon.setCode(request.code().toUpperCase());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderValue(request.minOrderValue());
        coupon.setExpirationDate(request.expirationDate());
        if (request.active() != null) {
            coupon.setActive(request.active());
        }
        return CouponResponse.fromEntity(couponRepository.save(coupon));
    }

    @Transactional
    public void delete(Long id) {
        Coupon coupon = findEntity(id);
        couponRepository.delete(coupon);
    }

    @Transactional(readOnly = true)
    public Coupon validateForUse(User user, String code, BigDecimal orderSubtotal) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Cupom inválido: " + code));

        if (!coupon.isActive()) {
            throw new BusinessException("Cupom inativo: " + code);
        }
        if (coupon.isExpired()) {
            throw new BusinessException("Cupom expirado: " + code);
        }
        if (coupon.getMinOrderValue() != null && orderSubtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new BusinessException("Este cupom exige um pedido de no mínimo " + coupon.getMinOrderValue());
        }
        if (couponUsageRepository.existsByCouponAndUser(coupon, user)) {
            throw new BusinessException("Você já utilizou este cupom");
        }

        return coupon;
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderSubtotal) {
        BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENTAGE
                ? orderSubtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountValue();

        return discount.min(orderSubtotal);
    }

    @Transactional
    public void registerUsage(Coupon coupon, User user, Order order) {
        couponUsageRepository.save(CouponUsage.builder().coupon(coupon).user(user).order(order).build());
    }

    private Coupon findEntity(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupom não encontrado: " + id));
    }
}

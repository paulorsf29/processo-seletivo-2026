package com.example.Ecomerce.service;

import com.example.Ecomerce.exception.BusinessException;
import com.example.Ecomerce.model.Coupon;
import com.example.Ecomerce.model.DiscountType;
import com.example.Ecomerce.model.Role;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.repository.CouponRepository;
import com.example.Ecomerce.repository.CouponUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponService couponService;

    private User customer() {
        return User.builder().id(1L).name("Cliente").email("cliente@example.com").password("x").role(Role.CUSTOMER).build();
    }

    private Coupon percentageCoupon(BigDecimal minOrderValue, LocalDateTime expiration, boolean active) {
        return Coupon.builder()
                .code("PROMO10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .minOrderValue(minOrderValue)
                .expirationDate(expiration)
                .active(active)
                .build();
    }

    @Test
    void validateForUse_throwsWhenCodeUnknown() {
        when(couponRepository.findByCodeIgnoreCase("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.validateForUse(customer(), "NOPE", BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cupom inválido");
    }

    @Test
    void validateForUse_throwsWhenInactive() {
        Coupon coupon = percentageCoupon(null, null, false);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateForUse(customer(), "PROMO10", BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void validateForUse_throwsWhenExpired() {
        Coupon coupon = percentageCoupon(null, LocalDateTime.now().minusDays(1), true);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateForUse(customer(), "PROMO10", BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void validateForUse_throwsWhenBelowMinOrderValue() {
        Coupon coupon = percentageCoupon(new BigDecimal("200.00"), null, true);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateForUse(customer(), "PROMO10", new BigDecimal("100.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mínimo");
    }

    @Test
    void validateForUse_throwsWhenAlreadyUsedByUser() {
        Coupon coupon = percentageCoupon(null, null, true);
        User customer = customer();
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCouponAndUser(coupon, customer)).thenReturn(true);

        assertThatThrownBy(() -> couponService.validateForUse(customer, "PROMO10", BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já utilizou");
    }

    @Test
    void validateForUse_returnsCouponWhenAllRulesPass() {
        Coupon coupon = percentageCoupon(new BigDecimal("50.00"), LocalDateTime.now().plusDays(1), true);
        User customer = customer();
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCouponAndUser(coupon, customer)).thenReturn(false);

        Coupon result = couponService.validateForUse(customer, "PROMO10", new BigDecimal("100.00"));

        assertThat(result).isEqualTo(coupon);
    }

    @Test
    void calculateDiscount_percentage() {
        Coupon coupon = percentageCoupon(null, null, true);

        BigDecimal discount = couponService.calculateDiscount(coupon, new BigDecimal("200.00"));

        assertThat(discount).isEqualByComparingTo("20.00");
    }

    @Test
    void calculateDiscount_fixedNeverExceedsSubtotal() {
        Coupon coupon = Coupon.builder().code("FIXED50").discountType(DiscountType.FIXED).discountValue(new BigDecimal("50.00")).active(true).build();

        BigDecimal discount = couponService.calculateDiscount(coupon, new BigDecimal("30.00"));

        assertThat(discount).isEqualByComparingTo("30.00");
    }
}

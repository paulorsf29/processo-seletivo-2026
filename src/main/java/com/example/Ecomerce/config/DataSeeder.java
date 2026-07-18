package com.example.Ecomerce.config;

import com.example.Ecomerce.model.*;
import com.example.Ecomerce.repository.CouponRepository;
import com.example.Ecomerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Populates the catalog and a couple of example coupons on first run, so a fresh clone is usable immediately. */
@Component
@Order(2)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    @Override
    public void run(String... args) {
        seedProducts();
        seedCoupons();
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            return;
        }

        List<Product> products = List.of(
                product("Camisa Seleção Brasileira 2026 I", "Camisa titular da Seleção Brasileira, tecido leve e respirável.",
                        new BigDecimal("349.90"), "Brasil", "Nike", "Seleções", Size.M, 40,
                        "https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=600"),
                product("Camisa Seleção Argentina 2026 I", "Camisa oficial da Argentina, tricampeã mundial.",
                        new BigDecimal("349.90"), "Argentina", "Adidas", "Seleções", Size.G, 35,
                        "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=600"),
                product("Camisa Flamengo 2026 I", "Camisa titular rubro-negra, torcedor.",
                        new BigDecimal("279.90"), "Flamengo", "Adidas", "Clubes Brasileiros", Size.M, 60,
                        "https://images.unsplash.com/photo-1580087433259-7b03a1cd9b56?w=600"),
                product("Camisa Palmeiras 2026 I", "Camisa titular alviverde, torcedor.",
                        new BigDecimal("279.90"), "Palmeiras", "Puma", "Clubes Brasileiros", Size.M, 55,
                        "https://images.unsplash.com/photo-1571744865781-2b64bc78d80f?w=600"),
                product("Camisa Corinthians 2026 I", "Camisa titular alvinegra, torcedor.",
                        new BigDecimal("279.90"), "Corinthians", "Nike", "Clubes Brasileiros", Size.G, 50,
                        "https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?w=600"),
                product("Camisa Real Madrid 2026 I", "Camisa titular merengue, torcedor.",
                        new BigDecimal("389.90"), "Real Madrid", "Adidas", "Clubes Europeus", Size.M, 30,
                        "https://images.unsplash.com/photo-1552667466-07770ae110d0?w=600"),
                product("Camisa Barcelona 2026 I", "Camisa titular blaugrana, torcedor.",
                        new BigDecimal("389.90"), "Barcelona", "Nike", "Clubes Europeus", Size.G, 28,
                        "https://images.unsplash.com/photo-1522778034537-20a2486be803?w=600"),
                product("Camisa Manchester City 2026 I", "Camisa titular citizen, torcedor.",
                        new BigDecimal("379.90"), "Manchester City", "Puma", "Clubes Europeus", Size.M, 32,
                        "https://images.unsplash.com/photo-1489944440615-453fc2b6a9a9?w=600"),
                product("Camisa Retrô Seleção Brasileira 1994", "Edição retrô comemorativa do tetracampeonato.",
                        new BigDecimal("299.90"), "Brasil", "Nike", "Retrô", Size.M, 25,
                        "https://images.unsplash.com/photo-1518098268026-4e89f1a2cd8e?w=600"),
                product("Camisa Retrô Santos 1962", "Edição retrô comemorativa da era Pelé.",
                        new BigDecimal("299.90"), "Santos", "Umbro", "Retrô", Size.P, 20,
                        "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=600")
        );

        productRepository.saveAll(products);
    }

    private Product product(String name, String description, BigDecimal price, String team, String brand,
                              String category, Size size, int stock, String imageUrl) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .team(team)
                .brand(brand)
                .category(category)
                .size(size)
                .stockQuantity(stock)
                .imageUrl(imageUrl)
                .active(true)
                .build();
    }

    private void seedCoupons() {
        if (couponRepository.count() > 0) {
            return;
        }

        List<Coupon> coupons = List.of(
                Coupon.builder()
                        .code("BEMVINDO10")
                        .discountType(DiscountType.PERCENTAGE)
                        .discountValue(new BigDecimal("10"))
                        .minOrderValue(null)
                        .expirationDate(LocalDateTime.now().plusDays(90))
                        .active(true)
                        .build(),
                Coupon.builder()
                        .code("FRETE20")
                        .discountType(DiscountType.FIXED)
                        .discountValue(new BigDecimal("20.00"))
                        .minOrderValue(new BigDecimal("150.00"))
                        .expirationDate(LocalDateTime.now().plusDays(60))
                        .active(true)
                        .build()
        );

        couponRepository.saveAll(coupons);
    }
}

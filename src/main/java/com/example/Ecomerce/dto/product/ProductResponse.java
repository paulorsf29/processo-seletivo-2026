package com.example.Ecomerce.dto.product;

import com.example.Ecomerce.model.Product;
import com.example.Ecomerce.model.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String team,
        String brand,
        String category,
        Size size,
        Integer stockQuantity,
        String imageUrl,
        boolean active,
        String createdAt,
        String updatedAt
) implements Serializable {
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getTeam(),
                product.getBrand(),
                product.getCategory(),
                product.getSize(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.isActive(),
                product.getCreatedAt() != null ? product.getCreatedAt().toString() : null,
                product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : null
        );
    }
}

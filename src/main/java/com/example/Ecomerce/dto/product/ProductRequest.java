package com.example.Ecomerce.dto.product;

import com.example.Ecomerce.model.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        String description,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
        BigDecimal price,

        String team,

        String brand,

        String category,

        Size size,

        @NotNull(message = "Quantidade em estoque é obrigatória")
        @Min(value = 0, message = "Estoque não pode ser negativo")
        Integer stockQuantity,

        String imageUrl,

        Boolean active
) {
}

package com.example.Ecomerce.repository;

import com.example.Ecomerce.model.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> filter(String search, String category, BigDecimal minPrice, BigDecimal maxPrice, boolean onlyActive) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (onlyActive) {
                predicate = cb.and(predicate, cb.isTrue(root.get("active")));
            }
            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("team"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("brand"), "")), pattern)
                ));
            }
            if (StringUtils.hasText(category)) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicate;
        };
    }
}

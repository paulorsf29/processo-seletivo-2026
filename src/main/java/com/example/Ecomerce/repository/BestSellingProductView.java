package com.example.Ecomerce.repository;

public interface BestSellingProductView {

    Long getProductId();

    String getProductName();

    Long getTotalSold();

    java.math.BigDecimal getTotalRevenue();
}

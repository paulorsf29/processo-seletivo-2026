package com.example.Ecomerce.repository;

import com.example.Ecomerce.model.Cart;
import com.example.Ecomerce.model.CartItem;
import com.example.Ecomerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}

package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.cart.CartItemRequest;
import com.example.Ecomerce.dto.cart.CartResponse;
import com.example.Ecomerce.exception.BusinessException;
import com.example.Ecomerce.exception.ResourceNotFoundException;
import com.example.Ecomerce.model.Cart;
import com.example.Ecomerce.model.CartItem;
import com.example.Ecomerce.model.Product;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.repository.CartItemRepository;
import com.example.Ecomerce.repository.CartRepository;
import com.example.Ecomerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        return CartResponse.fromEntity(getOrCreateCart(user));
    }

    @Transactional
    public CartResponse addItem(User user, CartItemRequest request) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + request.productId()));

        if (!product.isActive()) {
            throw new BusinessException("Produto indisponível: " + product.getName());
        }

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        int currentQuantity = item != null ? item.getQuantity() : 0;
        int newQuantity = currentQuantity + request.quantity();

        if (product.getStockQuantity() < newQuantity) {
            throw new BusinessException("Estoque insuficiente para o produto: " + product.getName());
        }

        if (item == null) {
            item = CartItem.builder().product(product).quantity(newQuantity).build();
            cart.addItem(item);
        } else {
            item.setQuantity(newQuantity);
        }
        cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse updateItem(User user, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não está no carrinho: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new BusinessException("Estoque insuficiente para o produto: " + product.getName());
        }

        item.setQuantity(quantity);
        cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse removeItem(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        if (item != null) {
            cart.getItems().remove(item);
            cartRepository.save(cart);
        }

        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}

package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.cart.CartItemRequest;
import com.example.Ecomerce.dto.cart.CartResponse;
import com.example.Ecomerce.exception.BusinessException;
import com.example.Ecomerce.model.Cart;
import com.example.Ecomerce.model.CartItem;
import com.example.Ecomerce.model.Product;
import com.example.Ecomerce.model.Role;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.repository.CartItemRepository;
import com.example.Ecomerce.repository.CartRepository;
import com.example.Ecomerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User customer() {
        return User.builder().id(1L).name("Cliente").email("cliente@example.com").password("x").role(Role.CUSTOMER).build();
    }

    private Product product(int stock) {
        return Product.builder().id(1L).name("Camisa Brasil").price(new BigDecimal("100.00")).stockQuantity(stock).active(true).build();
    }

    private Cart emptyCart(User user) {
        return Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
    }

    @Test
    void addItem_createsNewLineWhenProductNotInCart() {
        User user = customer();
        Product product = product(10);
        Cart cart = emptyCart(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.addItem(user, new CartItemRequest(1L, 2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void addItem_incrementsQuantityWhenProductAlreadyInCart() {
        User user = customer();
        Product product = product(10);
        Cart cart = emptyCart(user);
        CartItem existing = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        cart.getItems().add(existing);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existing));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.addItem(user, new CartItemRequest(1L, 3));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void addItem_throwsWhenStockInsufficient() {
        User user = customer();
        Product product = product(2);
        Cart cart = emptyCart(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(user, new CartItemRequest(1L, 5)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void updateItem_changesQuantity() {
        User user = customer();
        Product product = product(10);
        Cart cart = emptyCart(user);
        CartItem existing = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        cart.getItems().add(existing);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existing));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.updateItem(user, 1L, 7);

        assertThat(response.items().get(0).quantity()).isEqualTo(7);
    }

    @Test
    void removeItem_removesLine() {
        User user = customer();
        Product product = product(10);
        Cart cart = emptyCart(user);
        CartItem existing = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        cart.getItems().add(existing);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existing));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.removeItem(user, 1L);

        assertThat(response.items()).isEmpty();
    }

    @Test
    void clearCart_emptiesAllItems() {
        User user = customer();
        Product product = product(10);
        Cart cart = emptyCart(user);
        cart.getItems().add(CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build());
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.clearCart(user);

        assertThat(cart.getItems()).isEmpty();
    }
}

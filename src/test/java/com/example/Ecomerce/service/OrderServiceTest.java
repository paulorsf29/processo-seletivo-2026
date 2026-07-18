package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.order.OrderRequest;
import com.example.Ecomerce.dto.order.OrderResponse;
import com.example.Ecomerce.exception.BusinessException;
import com.example.Ecomerce.exception.ResourceNotFoundException;
import com.example.Ecomerce.model.*;
import com.example.Ecomerce.repository.OrderRepository;
import com.example.Ecomerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @Mock
    private CouponService couponService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private User customer(Long id) {
        return User.builder().id(id).name("Cliente").email("cliente@example.com").password("x").role(Role.CUSTOMER).build();
    }

    private User admin() {
        return User.builder().id(99L).name("Admin").email("admin@example.com").password("x").role(Role.ADMIN).build();
    }

    private Product product(int stock, boolean active) {
        return Product.builder()
                .id(1L)
                .name("Camisa Brasil")
                .price(new BigDecimal("100.00"))
                .stockQuantity(stock)
                .active(active)
                .build();
    }

    private Cart cartWith(Product product, int quantity) {
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        cart.addItem(CartItem.builder().product(product).quantity(quantity).build());
        return cart;
    }

    @Test
    void createOrder_computesTotalAndDecrementsStock() {
        User customer = customer(1L);
        Product product = product(10, true);
        when(cartService.getOrCreateCart(customer)).thenReturn(cartWith(product, 3));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(customer, new OrderRequest(null));

        assertThat(response.totalAmount()).isEqualByComparingTo("300.00");
        assertThat(response.items()).hasSize(1);
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(7);
        verify(cartService).clearCart(customer);
    }

    @Test
    void createOrder_throwsWhenCartEmpty() {
        User customer = customer(1L);
        when(cartService.getOrCreateCart(customer)).thenReturn(Cart.builder().id(1L).items(new ArrayList<>()).build());

        assertThatThrownBy(() -> orderService.createOrder(customer, new OrderRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("carrinho está vazio");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_throwsWhenStockInsufficient() {
        User customer = customer(1L);
        Product product = product(2, true);
        when(cartService.getOrCreateCart(customer)).thenReturn(cartWith(product, 5));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(customer, new OrderRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_throwsWhenProductInactive() {
        User customer = customer(1L);
        Product product = product(10, false);
        when(cartService.getOrCreateCart(customer)).thenReturn(cartWith(product, 1));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(customer, new OrderRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void createOrder_applesCouponDiscount() {
        User customer = customer(1L);
        Product product = product(10, true);
        when(cartService.getOrCreateCart(customer)).thenReturn(cartWith(product, 2));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Coupon coupon = Coupon.builder().code("PROMO10").discountType(DiscountType.PERCENTAGE).discountValue(new BigDecimal("10")).active(true).build();
        when(couponService.validateForUse(eq(customer), eq("PROMO10"), any())).thenReturn(coupon);
        when(couponService.calculateDiscount(eq(coupon), any())).thenReturn(new BigDecimal("20.00"));

        OrderResponse response = orderService.createOrder(customer, new OrderRequest("PROMO10"));

        assertThat(response.subtotal()).isEqualByComparingTo("200.00");
        assertThat(response.discountAmount()).isEqualByComparingTo("20.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("180.00");
        verify(couponService).registerUsage(eq(coupon), eq(customer), any(Order.class));
    }

    @Test
    void getById_allowsOwner() {
        User owner = customer(1L);
        Order order = Order.builder().id(10L).user(owner).status(OrderStatus.PENDING_PAYMENT).totalAmount(BigDecimal.TEN).items(List.of()).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getById(10L, owner);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void getById_allowsAdminForAnyOrder() {
        User owner = customer(1L);
        Order order = Order.builder().id(10L).user(owner).status(OrderStatus.PENDING_PAYMENT).totalAmount(BigDecimal.TEN).items(List.of()).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getById(10L, admin());

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void getById_deniesStranger() {
        User owner = customer(1L);
        User stranger = customer(2L);
        Order order = Order.builder().id(10L).user(owner).status(OrderStatus.PENDING_PAYMENT).totalAmount(BigDecimal.TEN).items(List.of()).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getById(10L, stranger))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateStatus_allowsPaidToShipped() {
        User owner = customer(1L);
        Order order = Order.builder().id(10L).user(owner).status(OrderStatus.PAID).totalAmount(BigDecimal.TEN).items(List.of()).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateStatus(10L, OrderStatus.SHIPPED);

        assertThat(response.status()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void updateStatus_rejectsInvalidTransition() {
        User owner = customer(1L);
        Order order = Order.builder().id(10L).user(owner).status(OrderStatus.SHIPPED).totalAmount(BigDecimal.TEN).items(List.of()).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(10L, OrderStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_restocksItemsAndAllowsOwner() {
        User owner = customer(1L);
        Product product = product(5, true);
        OrderItem item = OrderItem.builder().product(product).quantity(3).unitPrice(new BigDecimal("100.00")).subtotal(new BigDecimal("300.00")).build();
        Order order = Order.builder().id(10L).user(owner).status(OrderStatus.PAID).totalAmount(new BigDecimal("300.00")).items(List.of(item)).build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.cancelOrder(10L, owner);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(product.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void cancelOrder_deniesAfterShipped() {
        User owner = customer(1L);
        Order order = Order.builder().id(10L).user(owner).status(OrderStatus.SHIPPED).totalAmount(BigDecimal.TEN).items(List.of()).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(10L, owner))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    void cancelOrder_deniesStranger() {
        User owner = customer(1L);
        User stranger = customer(2L);
        Order order = Order.builder().id(10L).user(owner).status(OrderStatus.PAID).totalAmount(BigDecimal.TEN).items(List.of()).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(10L, stranger))
                .isInstanceOf(AccessDeniedException.class);
    }
}

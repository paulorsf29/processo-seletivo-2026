package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.order.OrderRequest;
import com.example.Ecomerce.dto.order.OrderResponse;
import com.example.Ecomerce.exception.BusinessException;
import com.example.Ecomerce.exception.ResourceNotFoundException;
import com.example.Ecomerce.model.*;
import com.example.Ecomerce.repository.OrderRepository;
import com.example.Ecomerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING_PAYMENT, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PAID, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final CouponService couponService;

    @Transactional
    public OrderResponse createOrder(User customer, OrderRequest request) {
        Cart cart = cartService.getOrCreateCart(customer);

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Seu carrinho está vazio");
        }

        Order order = Order.builder()
                .user(customer)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        // Snapshot cart items before mutating the cart/products to avoid concurrent-modification issues.
        for (CartItem cartItem : cart.getItems().stream().toList()) {
            Long productId = cartItem.getProduct().getId();
            int quantity = cartItem.getQuantity();

            // Pessimistic write lock: holds the row until this transaction commits, so a
            // concurrent checkout for the same product can't both succeed on the last unit.
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));

            if (!product.isActive()) {
                throw new BusinessException("Produto indisponível: " + product.getName());
            }
            if (product.getStockQuantity() < quantity) {
                throw new BusinessException("Estoque insuficiente para o produto: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(itemSubtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .subtotal(itemSubtotal)
                    .build();

            order.addItem(orderItem);
        }

        Coupon coupon = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (StringUtils.hasText(request.couponCode())) {
            coupon = couponService.validateForUse(customer, request.couponCode(), subtotal);
            discount = couponService.calculateDiscount(coupon, subtotal);
            order.setCouponCode(coupon.getCode());
            order.setDiscountAmount(discount);
        }

        order.setTotalAmount(subtotal.subtract(discount));

        Order saved = orderRepository.save(order);

        if (coupon != null) {
            couponService.registerUsage(coupon, customer, saved);
        }

        cartService.clearCart(customer);

        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(User customer, Pageable pageable) {
        return orderRepository.findByUser(customer, pageable).map(OrderResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        return page.map(OrderResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id, User requester) {
        Order order = findEntity(id);
        assertOwnerOrAdmin(order, requester);
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = findEntity(id);
        applyStatusTransition(order, newStatus);
        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, User requester) {
        Order order = findEntity(id);
        assertOwnerOrAdmin(order, requester);
        applyStatusTransition(order, OrderStatus.CANCELED);
        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    private void applyStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus current = order.getStatus();
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));

        if (current == newStatus) {
            return;
        }
        if (!allowed.contains(newStatus)) {
            throw new BusinessException(
                    "Transição de status inválida: " + current + " -> " + newStatus
            );
        }

        if (newStatus == OrderStatus.CANCELED) {
            restockItems(order);
        }

        order.setStatus(newStatus);
    }

    private void restockItems(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + item.getProduct().getId()));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    Order findEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }

    private void assertOwnerOrAdmin(Order order, User requester) {
        boolean isOwner = order.getUser().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Você não tem permissão para acessar este pedido");
        }
    }
}

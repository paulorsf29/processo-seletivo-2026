package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.payment.PaymentRequest;
import com.example.Ecomerce.dto.payment.PaymentResponse;
import com.example.Ecomerce.exception.BusinessException;
import com.example.Ecomerce.model.*;
import com.example.Ecomerce.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentGatewaySimulator paymentGatewaySimulator;

    @InjectMocks
    private PaymentService paymentService;

    private User customer(Long id) {
        return User.builder().id(id).name("Cliente").email("cliente@example.com").password("x").role(Role.CUSTOMER).build();
    }

    private User admin() {
        return User.builder().id(99L).name("Admin").email("admin@example.com").password("x").role(Role.ADMIN).build();
    }

    private Order order(Long id, User owner, OrderStatus status) {
        return Order.builder().id(id).user(owner).status(status).totalAmount(new BigDecimal("150.00")).items(List.of()).build();
    }

    @Test
    void createPayment_approvedMarksOrderPaid() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PENDING_PAYMENT);
        when(orderService.findEntity(10L)).thenReturn(order);
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGatewaySimulator.approve()).thenReturn(true);

        PaymentRequest request = new PaymentRequest(10L, PaymentMethod.PIX);
        PaymentResponse response = paymentService.createPayment(owner, request);

        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.amount()).isEqualByComparingTo("150.00");
        verify(orderService).updateStatus(10L, OrderStatus.PAID);
    }

    @Test
    void createPayment_rejectedLeavesOrderPendingAndAllowsRetry() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PENDING_PAYMENT);
        when(orderService.findEntity(10L)).thenReturn(order);
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGatewaySimulator.approve()).thenReturn(false);

        PaymentRequest request = new PaymentRequest(10L, PaymentMethod.PIX);
        PaymentResponse response = paymentService.createPayment(owner, request);

        assertThat(response.status()).isEqualTo(PaymentStatus.REJECTED);
        verify(orderService, never()).updateStatus(eq(10L), any());
    }

    @Test
    void createPayment_allowsRetryAfterPreviousRejection() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PENDING_PAYMENT);
        Payment rejectedAttempt = Payment.builder().id(5L).order(order).amount(new BigDecimal("150.00")).method(PaymentMethod.PIX).status(PaymentStatus.REJECTED).build();
        when(orderService.findEntity(10L)).thenReturn(order);
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(rejectedAttempt));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGatewaySimulator.approve()).thenReturn(true);

        PaymentResponse response = paymentService.createPayment(owner, new PaymentRequest(10L, PaymentMethod.CREDIT_CARD));

        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED);
        verify(orderService).updateStatus(10L, OrderStatus.PAID);
    }

    @Test
    void createPayment_throwsWhenOrderNotPendingPayment() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PAID);
        when(orderService.findEntity(10L)).thenReturn(order);

        PaymentRequest request = new PaymentRequest(10L, PaymentMethod.PIX);

        assertThatThrownBy(() -> paymentService.createPayment(owner, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aguardando pagamento");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_throwsWhenAlreadyHasApprovedPayment() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PENDING_PAYMENT);
        Payment approved = Payment.builder().id(5L).order(order).status(PaymentStatus.APPROVED).build();
        when(orderService.findEntity(10L)).thenReturn(order);
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(approved));

        PaymentRequest request = new PaymentRequest(10L, PaymentMethod.PIX);

        assertThatThrownBy(() -> paymentService.createPayment(owner, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já possui um pagamento");
    }

    @Test
    void createPayment_deniesNonOwnerNonAdmin() {
        User owner = customer(1L);
        User stranger = customer(2L);
        Order order = order(10L, owner, OrderStatus.PENDING_PAYMENT);
        when(orderService.findEntity(10L)).thenReturn(order);

        PaymentRequest request = new PaymentRequest(10L, PaymentMethod.PIX);

        assertThatThrownBy(() -> paymentService.createPayment(stranger, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createPayment_allowsAdminToPayOnBehalfOfCustomer() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PENDING_PAYMENT);
        when(orderService.findEntity(10L)).thenReturn(order);
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGatewaySimulator.approve()).thenReturn(true);

        PaymentRequest request = new PaymentRequest(10L, PaymentMethod.CREDIT_CARD);

        assertThat(paymentService.createPayment(admin(), request)).isNotNull();
    }

    @Test
    void updateStatus_toApproved_syncsOrderToPaid() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PENDING_PAYMENT);
        Payment payment = Payment.builder().id(5L).order(order).amount(new BigDecimal("150.00")).method(PaymentMethod.PIX).status(PaymentStatus.PENDING).build();
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.updateStatus(5L, PaymentStatus.APPROVED);

        verify(orderService).updateStatus(10L, OrderStatus.PAID);
    }

    @Test
    void updateStatus_toRefunded_cancelsOrder() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PAID);
        Payment payment = Payment.builder().id(5L).order(order).amount(new BigDecimal("150.00")).method(PaymentMethod.PIX).status(PaymentStatus.APPROVED).build();
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.updateStatus(5L, PaymentStatus.REFUNDED);

        verify(orderService).updateStatus(10L, OrderStatus.CANCELED);
    }

    @Test
    void updateStatus_toRejected_doesNotTouchOrder() {
        User owner = customer(1L);
        Order order = order(10L, owner, OrderStatus.PENDING_PAYMENT);
        Payment payment = Payment.builder().id(5L).order(order).amount(new BigDecimal("150.00")).method(PaymentMethod.PIX).status(PaymentStatus.PENDING).build();
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.updateStatus(5L, PaymentStatus.REJECTED);

        verify(orderService, never()).updateStatus(eq(10L), any());
    }
}

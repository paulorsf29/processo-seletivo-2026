package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.payment.PaymentRequest;
import com.example.Ecomerce.dto.payment.PaymentResponse;
import com.example.Ecomerce.exception.BusinessException;
import com.example.Ecomerce.exception.ResourceNotFoundException;
import com.example.Ecomerce.model.*;
import com.example.Ecomerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final PaymentGatewaySimulator paymentGatewaySimulator;

    @Transactional
    public PaymentResponse createPayment(User customer, PaymentRequest request) {
        Order order = orderService.findEntity(request.orderId());

        boolean isOwner = order.getUser().getId().equals(customer.getId());
        if (!isOwner && customer.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Você não tem permissão para pagar este pedido");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("Este pedido não está aguardando pagamento");
        }

        Payment payment = paymentRepository.findByOrder(order).orElse(null);
        if (payment != null && payment.getStatus() != PaymentStatus.REJECTED) {
            throw new BusinessException("Este pedido já possui um pagamento registrado");
        }

        boolean approved = paymentGatewaySimulator.approve();

        if (payment == null) {
            payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .method(request.method())
                    .status(approved ? PaymentStatus.APPROVED : PaymentStatus.REJECTED)
                    .build();
        } else {
            // Retrying after a previous rejection: reuse the same payment row (order_id is unique).
            payment.setMethod(request.method());
            payment.setAmount(order.getTotalAmount());
            payment.setStatus(approved ? PaymentStatus.APPROVED : PaymentStatus.REJECTED);
        }

        paymentRepository.save(payment);

        if (approved) {
            orderService.updateStatus(order.getId(), OrderStatus.PAID);
        }

        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id, User requester) {
        Payment payment = findEntity(id);

        boolean isOwner = payment.getOrder().getUser().getId().equals(requester.getId());
        if (!isOwner && requester.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Você não tem permissão para acessar este pagamento");
        }

        return PaymentResponse.fromEntity(payment);
    }

    @Transactional
    public PaymentResponse updateStatus(Long id, PaymentStatus status) {
        Payment payment = findEntity(id);
        payment.setStatus(status);
        paymentRepository.save(payment);

        if (status == PaymentStatus.APPROVED) {
            orderService.updateStatus(payment.getOrder().getId(), OrderStatus.PAID);
        } else if (status == PaymentStatus.REFUNDED) {
            orderService.updateStatus(payment.getOrder().getId(), OrderStatus.CANCELED);
        }

        return PaymentResponse.fromEntity(payment);
    }

    private Payment findEntity(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + id));
    }
}

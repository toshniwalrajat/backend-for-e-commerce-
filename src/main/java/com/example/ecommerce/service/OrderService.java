package com.example.ecommerce.service;

import com.example.ecommerce.entity.OrderItemResponse;
import com.example.ecommerce.entity.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public OrderResponse checkout(String cartToken, PaymentMethod paymentMethod) {
        Cart cart = cartRepository.findWithItemsByCartToken(cartToken)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found"
                ));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot checkout an empty cart"
            );
        }

        Order order = new Order();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(
                            cartItem.getProduct().getId()
                    )
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "A product in the cart no longer exists"
                    ));

            int quantity = cartItem.getQuantity();

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Product '" + product.getName() + "' is not available"
                );
            }

            if (product.getStockQuantity() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for product '" + product.getName() + "'"
                );
            }

            // Reduce stock as part of the checkout transaction.
            product.setStockQuantity(product.getStockQuantity() - quantity);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setSku(product.getSku());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(quantity);
            order.addItem(orderItem);

            total = total.add(
                    product.getPrice().multiply(BigDecimal.valueOf(quantity))
            );
        }

        order.setTotalAmount(total);

        Payment payment = new Payment();
        payment.setAmount(total);
        payment.setMethod(paymentMethod);

        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            payment.setStatus(PaymentStatus.PENDING);
            order.setStatus(OrderStatus.PENDING);
        } else {
            // This is a mock payment result, not a real payment-provider charge.
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setProviderReference("MOCK-" + UUID.randomUUID());
            order.setStatus(OrderStatus.CONFIRMED);
        }

        order.setPayment(payment);

        // Save order, items, and payment through the cascade on Order.
        Order savedOrder = orderRepository.save(order);

        // Remove cart items after successful order creation.
        cart.getItems().clear();

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));

        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getSku(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())
                        )
                ))
                .toList();

        Payment payment = order.getPayment();

        return new OrderResponse(
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                payment == null ? null : payment.getMethod(),
                payment == null ? null : payment.getStatus(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}
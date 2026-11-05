package com.example.ecommerce.entity;

import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.PaymentMethod;
import com.example.ecommerce.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
}
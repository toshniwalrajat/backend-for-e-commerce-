package com.example.ecommerce.entity;

import com.example.ecommerce.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}
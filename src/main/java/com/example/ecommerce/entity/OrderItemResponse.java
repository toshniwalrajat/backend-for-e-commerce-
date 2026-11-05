package com.example.ecommerce.entity;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}
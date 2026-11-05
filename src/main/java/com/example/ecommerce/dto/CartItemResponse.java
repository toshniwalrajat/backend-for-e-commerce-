package com.example.ecommerce.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}
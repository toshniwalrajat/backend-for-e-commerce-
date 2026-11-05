package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        String cartToken,
        List<CartItemResponse> items,
        BigDecimal total,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
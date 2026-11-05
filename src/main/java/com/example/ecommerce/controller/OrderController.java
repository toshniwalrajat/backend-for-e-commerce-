package com.example.ecommerce.controller;

import com.example.ecommerce.entity.CheckoutRequest;
import com.example.ecommerce.entity.OrderResponse;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout/{cartToken}")
    public ResponseEntity<OrderResponse> checkout(
            @PathVariable String cartToken,
            @Valid @RequestBody CheckoutRequest request
    ) {
        return ResponseEntity.ok(
                orderService.checkout(cartToken, request.paymentMethod())
        );
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable String orderNumber
    ) {
        return ResponseEntity.ok(orderService.getOrder(orderNumber));
    }
}
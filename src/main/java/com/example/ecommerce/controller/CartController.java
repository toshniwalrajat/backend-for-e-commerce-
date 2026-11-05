package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddCartItemRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.dto.UpdateCartItemRequest;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart() {
        CartResponse cart = cartService.createCart();

        URI location = URI.create("/api/carts/" + cart.cartToken());
        return ResponseEntity.created(location).body(cart);
    }

    @GetMapping("/{cartToken}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable String cartToken
    ) {
        return ResponseEntity.ok(cartService.getCart(cartToken));
    }

    @PostMapping("/{cartToken}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable String cartToken,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(
                cartService.addItem(
                        cartToken,
                        request.productId(),
                        request.quantity()
                )
        );
    }

    @PatchMapping("/{cartToken}/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable String cartToken,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateItemQuantity(
                        cartToken,
                        productId,
                        request.quantity()
                )
        );
    }

    @DeleteMapping("/{cartToken}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable String cartToken,
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                cartService.removeItem(cartToken, productId)
        );
    }
}
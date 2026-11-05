package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartResponse createCart() {
        Cart cart = cartRepository.save(new Cart());
        return toResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String cartToken) {
        return toResponse(findCart(cartToken));
    }

    public CartResponse addItem(String cartToken, Long productId, Integer quantity) {
        Cart cart = findCart(cartToken);
        Product product = findProduct(productId);

        checkProductCanBeAdded(product);

        CartItem existingItem = cartItemRepository
                .findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElse(null);

        int newQuantity = quantity;

        if (existingItem != null) {
            newQuantity += existingItem.getQuantity();
        }

        checkStock(product, newQuantity);

        if (existingItem != null) {
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.addItem(newItem);
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartResponse updateItemQuantity(
            String cartToken,
            Long productId,
            Integer quantity
    ) {
        Cart cart = findCart(cartToken);
        CartItem item = findCartItem(cart, productId);
        Product product = item.getProduct();

        checkProductCanBeAdded(product);
        checkStock(product, quantity);

        item.setQuantity(quantity);
        cartRepository.save(cart);

        return toResponse(cart);
    }

    public CartResponse removeItem(String cartToken, Long productId) {
        Cart cart = findCart(cartToken);
        CartItem item = findCartItem(cart, productId);

        cart.removeItem(item);
        cartRepository.save(cart);

        return toResponse(cart);
    }

    private Cart findCart(String cartToken) {
        return cartRepository.findWithItemsByCartToken(cartToken)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found"
                ));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + productId
                ));
    }

    private CartItem findCartItem(Cart cart, Long productId) {
        return cartItemRepository
                .findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product is not in this cart"
                ));
    }

    private void checkProductCanBeAdded(Product product) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This product is not currently available"
            );
        }
    }

    private void checkStock(Product product, int requestedQuantity) {
        if (requestedQuantity > product.getStockQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Requested quantity exceeds available stock"
            );
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems()
                .stream()
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal lineTotal = product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    return new CartItemResponse(
                            product.getId(),
                            product.getName(),
                            product.getSku(),
                            product.getPrice(),
                            item.getQuantity(),
                            lineTotal
                    );
                })
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getCartToken(),
                itemResponses,
                total,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}
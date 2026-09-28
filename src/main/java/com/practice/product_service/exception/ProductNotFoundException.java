package com.practice.product_service.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product %d not found".formatted(productId));
    }
}

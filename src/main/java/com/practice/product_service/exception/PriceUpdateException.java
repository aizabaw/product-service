package com.practice.product_service.exception;

public class PriceUpdateException extends Exception {
    public PriceUpdateException(Long id) {
        super("Product %d not found".formatted(id));
    }

}

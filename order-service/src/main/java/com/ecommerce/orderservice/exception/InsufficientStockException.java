package com.ecommerce.orderservice.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, Integer available, Integer requested) {
        super("Insufficient stock for product id " + productId +
                ". Available: " + available + ", Requested: " + requested);
    }
}

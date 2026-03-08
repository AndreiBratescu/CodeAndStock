package com.stock.stock.web.dto;

/**
 * Stocul unui produs într-un anumit stand (pentru afișare înainte/după).
 */
public record StockAtLocationDto(
        Long productId,
        String productSku,
        String productName,
        Long standId,
        String city,
        String mallName,
        int quantity,
        Long daysInStock
) {
}

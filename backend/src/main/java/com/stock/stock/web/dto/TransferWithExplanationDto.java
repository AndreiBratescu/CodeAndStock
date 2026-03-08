package com.stock.stock.web.dto;

/**
 * Un transfer recomandat cu explicație de ce se face mutarea.
 */
public record TransferWithExplanationDto(
        Long productId,
        String productSku,
        String productName,
        Long sourceStandId,
        String sourceCity,
        String sourceMall,
        Long targetStandId,
        String targetCity,
        String targetMall,
        int quantityToMove,
        Long daysInStockAtSource,
        String explanation
) {
}

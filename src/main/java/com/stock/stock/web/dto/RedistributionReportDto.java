package com.stock.stock.web.dto;

import java.util.List;

/**
 * Raport complet de redistribuire: stoc înainte, transferuri cu explicații, stoc după.
 */
public record RedistributionReportDto(
        String summaryExplanation,
        List<StockAtLocationDto> before,
        List<TransferWithExplanationDto> transfers,
        List<StockAtLocationDto> after
) {
}

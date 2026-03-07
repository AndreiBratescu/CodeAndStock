package com.stock.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard overview showing aggregated statistics
 * for the admin panel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private long totalStores;
    private long totalProducts;
    private long totalUsers;
    private long totalInventoryItems;
    private long totalSales;
    private int totalStockQuantity;
    private int totalSoldQuantity;
}

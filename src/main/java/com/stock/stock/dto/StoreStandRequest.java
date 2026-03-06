package com.stock.stock.dto;

import com.stock.stock.domain.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a store stand.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreStandRequest {
    private City city;
    private String mallName;
    private Integer storageCapacity;
}

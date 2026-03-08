package com.stock.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String roles;
    private Boolean enabled;
    private Long storeStandId;
    private String storeMallName;
    private String storeCity;
}

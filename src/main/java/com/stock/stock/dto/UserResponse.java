package com.stock.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user information (excludes password).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String roles;
    private Boolean enabled;
    private Long storeStandId;
    private String storeStandMall;
    private String storeStandCity;
}

package com.stock.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRegistrationResponseDto {
    private Long requestId;
    private String email;
    private String username;
    private String password;
    private String role;
    private String storeMallName;
    private String status;
    private String message;
}

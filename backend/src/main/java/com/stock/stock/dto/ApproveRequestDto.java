package com.stock.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRequestDto {
    private String role; // ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_MANAGER
    private String username; // editable by admin (auto-generated as default)
    private String password; // editable by admin (auto-generated as default)
}

package com.stock.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing a user's role.
 * Valid roles: ROLE_EMPLOYEE, ROLE_ADMIN, ROLE_MANAGER
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleUpdateRequest {
    private String role;
}

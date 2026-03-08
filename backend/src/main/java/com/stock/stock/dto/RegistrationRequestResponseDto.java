package com.stock.stock.dto;

import com.stock.stock.domain.RegistrationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequestResponseDto {
    private Long id;
    private String email;
    private Long storeStandId;
    private String storeMallName;
    private String storeCity;
    private RegistrationRequest.RequestStatus status;
    private LocalDateTime createdAt;
    private String message;
}

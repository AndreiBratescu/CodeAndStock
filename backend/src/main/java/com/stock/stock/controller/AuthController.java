package com.stock.stock.controller;

import com.stock.stock.dto.LoginRequest;
import com.stock.stock.dto.LoginResponse;
import com.stock.stock.dto.RegisterRequest;
import com.stock.stock.dto.RegisterResponse;
import com.stock.stock.dto.RegistrationRequestDto;
import com.stock.stock.dto.RegistrationRequestResponseDto;
import com.stock.stock.dto.TokenRefreshRequest;
import com.stock.stock.dto.TokenRefreshResponse;
import com.stock.stock.domain.RegistrationRequest;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.repository.RegistrationRequestRepository;
import com.stock.stock.repository.StoreStandRepository;
import com.stock.stock.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class AuthController {

    private final AuthService authService;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final StoreStandRepository storeStandRepository;

    public AuthController(AuthService authService,
                          RegistrationRequestRepository registrationRequestRepository,
                          StoreStandRepository storeStandRepository) {
        this.authService = authService;
        this.registrationRequestRepository = registrationRequestRepository;
        this.storeStandRepository = storeStandRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            log.info("Registration attempt for user: {}", registerRequest.getUsername());
            RegisterResponse response = authService.register(registerRequest);
            log.info("User {} registered successfully", registerRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getUsername());
            log.info(
                    "Login payload debug - username: {}, passwordLength: {}",
                    loginRequest.getUsername(),
                    loginRequest.getPassword() == null ? 0 : loginRequest.getPassword().length()
            );
            LoginResponse response = authService.authenticate(loginRequest);
            log.info("User {} logged in successfully", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        try {
            TokenRefreshResponse response = authService.refreshToken(tokenRefreshRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
            authService.logout(username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/registration-request")
    public ResponseEntity<RegistrationRequestResponseDto> submitRegistrationRequest(@RequestBody RegistrationRequestDto requestDto) {
        try {
            log.info("Registration request submitted for email: {}", requestDto.getEmail());

            StoreStand storeStand = storeStandRepository.findById(requestDto.getStoreStandId())
                    .orElseThrow(() -> new RuntimeException("Store stand not found"));

            RegistrationRequest registrationRequest = RegistrationRequest.builder()
                    .email(requestDto.getEmail())
                    .storeStand(storeStand)
                    .status(RegistrationRequest.RequestStatus.PENDING)
                    .build();

            RegistrationRequest saved = registrationRequestRepository.save(registrationRequest);

            return ResponseEntity.ok(RegistrationRequestResponseDto.builder()
                    .id(saved.getId())
                    .email(saved.getEmail())
                    .storeStandId(saved.getStoreStand().getId())
                    .storeMallName(saved.getStoreStand().getMallName())
                    .storeCity(saved.getStoreStand().getCity().toString())
                    .status(saved.getStatus())
                    .createdAt(saved.getCreatedAt())
                    .message("Registration request submitted successfully. Admin will review it soon.")
                    .build());
        } catch (RuntimeException e) {
            log.error("Registration request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/store-stands")
    public ResponseEntity<?> getStoreStands() {
        try {
            return ResponseEntity.ok(storeStandRepository.findAll());
        } catch (Exception e) {
            log.error("Failed to fetch store stands: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}


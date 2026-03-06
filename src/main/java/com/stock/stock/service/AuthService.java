package com.stock.stock.service;

import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.dto.LoginRequest;
import com.stock.stock.dto.LoginResponse;
import com.stock.stock.dto.RegisterRequest;
import com.stock.stock.dto.RegisterResponse;
import com.stock.stock.dto.TokenRefreshRequest;
import com.stock.stock.dto.TokenRefreshResponse;
import com.stock.stock.repository.AppUserRepository;
import com.stock.stock.repository.StoreStandRepository;
import com.stock.stock.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final StoreStandRepository storeStandRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, StoreStandRepository storeStandRepository,
                       JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.storeStandRepository = storeStandRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        // Validate username uniqueness
        if (appUserRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Validate email uniqueness
        if (appUserRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Validate password
        if (registerRequest.getPassword() == null || registerRequest.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Get store stand if provided
        StoreStand storeStand = null;
        if (registerRequest.getStoreStandId() != null) {
            storeStand = storeStandRepository.findById(registerRequest.getStoreStandId())
                    .orElseThrow(() -> new RuntimeException("Store stand not found with id: " + registerRequest.getStoreStandId()));
        }

        // Create new user with default role ROLE_EMPLOYEE
        AppUser newUser = AppUser.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles("ROLE_EMPLOYEE")
                .enabled(true)
                .storeStand(storeStand)
                .build();

        AppUser savedUser = appUserRepository.save(newUser);
        log.info("New user registered successfully: {}", savedUser.getUsername());

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .storeStandId(savedUser.getStoreStand() != null ? savedUser.getStoreStand().getId() : null)
                .message("User registered successfully")
                .build();
    }

    @Transactional
    public LoginResponse authenticate(LoginRequest loginRequest) {
        AppUser user = appUserRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + loginRequest.getUsername()));

        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // Save refresh token to database
        user.setRefreshToken(refreshToken);
        appUserRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000) // Convert to seconds
                .username(user.getUsername())
                .roles(user.getRoles())
                .storeStandId(user.getStoreStand() != null ? user.getStoreStand().getId() : null)
                .build();
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        String refreshToken = tokenRefreshRequest.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify refresh token matches stored token
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("Refresh token does not match stored token");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000)
                .build();
    }

    @Transactional
    public void logout(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRefreshToken(null);
        appUserRepository.save(user);
    }
}


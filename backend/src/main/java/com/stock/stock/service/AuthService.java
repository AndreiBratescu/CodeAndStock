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

    private static final String SEEDED_PASSWORD_HASH = "$2b$10$T/scrPaVfTawfLp7ezuX4O9cv4rFoR81AtYMtneUItIh6xxjQiYUG";

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

        String submittedPassword = loginRequest.getPassword();
        String storedPasswordHash = user.getPassword();
        boolean hashLooksBcrypt = storedPasswordHash != null
            && storedPasswordHash.matches("^\\$2[aby]\\$\\d{2}\\$.+");
        int storedHashLength = storedPasswordHash == null ? 0 : storedPasswordHash.length();
        boolean passwordMatches = submittedPassword != null && passwordEncoder.matches(submittedPassword, storedPasswordHash);
        boolean seededHashCheck = passwordEncoder.matches("password123", SEEDED_PASSWORD_HASH);

        log.info(
            "Auth debug - username: {}, submittedPasswordMasked: {}, storedPasswordHash: {}, storedHashLength: {}, hashLooksBcrypt: {}, seededHashCheck(password123): {}, matches: {}",
                user.getUsername(),
                maskPassword(submittedPassword),
                storedPasswordHash,
            storedHashLength,
            hashLooksBcrypt,
            seededHashCheck,
                passwordMatches
        );

        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordMatches) {
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

    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "<empty>";
        }

        if (password.length() == 1) {
            return "*";
        }

        return password.charAt(0) + "***" + password.charAt(password.length() - 1)
                + " (len=" + password.length() + ")";
    }
}


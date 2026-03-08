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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private StoreStandRepository storeStandRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private AppUser testUser;
    private StoreStand testStoreStand;

    @BeforeEach
    void setUp() {
        testStoreStand = new StoreStand();
        testStoreStand.setId(1L);

        testUser = AppUser.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .roles("ROLE_EMPLOYEE")
                .enabled(true)
                .storeStand(testStoreStand)
                .build();
    }

    @Test
    void testRegister_Success() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .build();

        when(appUserRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        RegisterResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("ROLE_EMPLOYEE", response.getRoles());
        assertEquals("User registered successfully", response.getMessage());

        verify(appUserRepository).save(argThat(user ->
                user.getUsername().equals("newuser") &&
                user.getEmail().equals("newuser@example.com") &&
                user.getPassword().equals("hashedPassword123") &&
                user.getRoles().equals("ROLE_EMPLOYEE") &&
                user.getEnabled().equals(true)
        ));
    }

    @Test
    void testRegister_WithStoreStand() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .storeStandId(1L)
                .build();

        when(appUserRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(storeStandRepository.findById(1L)).thenReturn(Optional.of(testStoreStand));
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        RegisterResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals(1L, response.getStoreStandId());
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("newuser@example.com")
                .password("password123")
                .build();

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(appUserRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testRegister_PasswordTooShort() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("12345")
                .build();

        when(appUserRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));
        assertEquals("Password must be at least 6 characters long", exception.getMessage());
    }

    @Test
    void testRegister_InvalidStoreStandId() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .storeStandId(999L)
                .build();

        when(appUserRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(storeStandRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));
        assertTrue(exception.getMessage().contains("Store stand not found"));
    }

    @Test
    void testAuthenticate_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", "ROLE_EMPLOYEE")).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("refreshToken");
        when(jwtTokenProvider.getJwtExpirationMs()).thenReturn(86400000L);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(testUser);

        LoginResponse response = authService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("ROLE_EMPLOYEE", response.getRoles());
        assertEquals(1L, response.getStoreStandId());

        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void testAuthenticate_UserNotFound() {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");

        when(appUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.authenticate(loginRequest));
    }

    @Test
    void testAuthenticate_UserDisabled() {
        testUser.setEnabled(false);
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticate(loginRequest));
        assertEquals("User account is disabled", exception.getMessage());
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticate(loginRequest));
        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void testRefreshToken_Success() {
        String refreshToken = "validRefreshToken";
        testUser.setRefreshToken(refreshToken);

        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken("testuser", "ROLE_EMPLOYEE")).thenReturn("newAccessToken");
        when(jwtTokenProvider.getJwtExpirationMs()).thenReturn(86400000L);

        TokenRefreshResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals(86400L, response.getExpiresIn());
    }

    @Test
    void testRefreshToken_InvalidToken() {
        TokenRefreshRequest request = new TokenRefreshRequest("invalidToken");

        when(jwtTokenProvider.validateToken("invalidToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(request));
    }

    @Test
    void testRefreshToken_TokenMismatch() {
        String refreshToken = "validRefreshToken";
        testUser.setRefreshToken("differentToken");

        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.refreshToken(request));
        assertEquals("Refresh token does not match stored token", exception.getMessage());
    }

    @Test
    void testLogout_Success() {
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(appUserRepository.save(any(AppUser.class))).thenReturn(testUser);

        authService.logout("testuser");

        verify(appUserRepository).save(argThat(user -> user.getRefreshToken() == null));
    }

    @Test
    void testLogout_UserNotFound() {
        when(appUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.logout("nonexistent"));
    }
}


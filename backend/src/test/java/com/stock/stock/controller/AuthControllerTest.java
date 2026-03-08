package com.stock.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.dto.LoginRequest;
import com.stock.stock.dto.LoginResponse;
import com.stock.stock.dto.TokenRefreshRequest;
import com.stock.stock.dto.TokenRefreshResponse;
import com.stock.stock.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("testuser", "password123");

        StoreStand storeStand = new StoreStand();
        storeStand.setId(1L);

        loginResponse = LoginResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .expiresIn(86400L)
                .username("testuser")
                .roles("ROLE_EMPLOYEE")
                .storeStandId(1L)
                .build();
    }

    @Test
    void testLogin_Success() throws Exception {
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles").value("ROLE_EMPLOYEE"))
                .andExpect(jsonPath("$.storeStandId").value(1));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(authService.authenticate(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest("refreshToken");
        TokenRefreshResponse refreshResponse = TokenRefreshResponse.builder()
                .accessToken("newAccessToken")
                .expiresIn(86400L)
                .build();

        when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(refreshResponse);

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.expiresIn").value(86400));
    }

    @Test
    void testRefreshToken_InvalidToken() throws Exception {
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest("invalidToken");

        when(authService.refreshToken(any(TokenRefreshRequest.class)))
            .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isBadRequest());
    }
}


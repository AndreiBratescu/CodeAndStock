package com.stock.stock.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.City;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.dto.LoginRequest;
import com.stock.stock.dto.LoginResponse;
import com.stock.stock.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        // Clear users
        appUserRepository.deleteAll();

        // Create store stand (not persisted, just for user relationship)
        StoreStand testStoreStand = new StoreStand();
        testStoreStand.setId(1L);
        testStoreStand.setCity(City.BUCURESTI);
        testStoreStand.setMallName("Test Mall");
        testStoreStand.setStorageCapacity(1000);

        // Create test user
        testUser = AppUser.builder()
                .username("integrationtest")
                .email("integration@test.com")
                .password(passwordEncoder.encode("password123"))
                .roles("ROLE_EMPLOYEE")
                .enabled(true)
                .storeStand(testStoreStand)
                .build();
        testUser = appUserRepository.save(testUser);
    }

    @Test
    void testFullAuthenticationFlow() throws Exception {
        // 1. Login
        LoginRequest loginRequest = new LoginRequest("integrationtest", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value("integrationtest"))
                .andExpect(jsonPath("$.roles").value("ROLE_EMPLOYEE"))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);

        assertNotNull(loginResponse.getAccessToken());
        assertNotNull(loginResponse.getRefreshToken());

        String accessToken = loginResponse.getAccessToken();
        String refreshToken = loginResponse.getRefreshToken();

        // 2. Access protected endpoint with access token
        mockMvc.perform(get("/api/products/my-stand")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 3. Access protected endpoint without token (should fail)
        mockMvc.perform(get("/api/products/my-stand"))
                .andExpect(status().isUnauthorized());

        // 4. Logout
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 5. Verify refresh token was cleared from database
        AppUser updatedUser = appUserRepository.findByUsername("integrationtest").orElseThrow();
        assertNull(updatedUser.getRefreshToken());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("integrationtest", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginWithDisabledUser() throws Exception {
        testUser.setEnabled(false);
        appUserRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest("integrationtest", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/products/my-stand")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
}




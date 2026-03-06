package com.stock.stock.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "testSecretKeyForJwtTokenGenerationThisIsALongSecretKeyForHS256Algorithm";
    private final long jwtExpiration = 3600000L; // 1 hour
    private final long refreshExpiration = 7200000L; // 2 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", jwtExpiration);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationMs", refreshExpiration);
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String roles = "ROLE_EMPLOYEE";

        String token = jwtTokenProvider.generateToken(username, roles);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testValidateToken_ValidToken() {
        String username = "testuser";
        String roles = "ROLE_EMPLOYEE";
        String token = jwtTokenProvider.generateToken(username, roles);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testuser";
        String roles = "ROLE_EMPLOYEE";
        String token = jwtTokenProvider.generateToken(username, roles);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetRolesFromToken() {
        String username = "testuser";
        String roles = "ROLE_EMPLOYEE";
        String token = jwtTokenProvider.generateToken(username, roles);

        String extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        assertEquals(roles, extractedRoles);
    }

    @Test
    void testGenerateRefreshToken() {
        String username = "testuser";

        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);

        // Verify it's a valid token
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(refreshToken));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // Create provider with very short expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(shortLivedProvider, "jwtExpirationMs", 1000L); // 1 second

        String token = shortLivedProvider.generateToken("testuser", "ROLE_EMPLOYEE");

        // Token should be valid initially
        assertTrue(shortLivedProvider.validateToken(token));

        // Wait for token to expire
        Thread.sleep(1500);

        // Token should now be expired
        assertFalse(shortLivedProvider.validateToken(token));
    }

    @Test
    void testTokenContainsCorrectClaims() {
        String username = "testuser";
        String roles = "ROLE_EMPLOYEE,ROLE_ADMIN";
        String token = jwtTokenProvider.generateToken(username, roles);

        Key key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(username, claims.getSubject());
        assertEquals(roles, claims.get("roles", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}



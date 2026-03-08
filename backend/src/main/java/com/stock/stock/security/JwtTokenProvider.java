package com.stock.stock.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:mySecretKeyForJwtTokenGenerationThisIsALongSecretKeyForHS256Algorithm}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs; // 24 hours by default

    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshTokenExpirationMs; // 7 days by default

    // Generate JWT Token
    public String generateToken(String username, String roles) {
        return buildToken(username, roles, jwtExpirationMs);
    }

    // Generate Refresh Token
    public String generateRefreshToken(String username) {
        return buildToken(username, "", refreshTokenExpirationMs);
    }

    // Build token with claims
    private String buildToken(String username, String roles, long expirationTime) {
        javax.crypto.SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    // Validate JWT Token
    public boolean validateToken(String authToken) {
        try {
            javax.crypto.SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    // Get username from JWT Token
    public String getUsernameFromToken(String token) {
        try {
            javax.crypto.SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    // Get roles from JWT Token
    public String getRolesFromToken(String token) {
        try {
            javax.crypto.SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("roles", String.class);
        } catch (JwtException e) {
            log.error("Error extracting roles from token: {}", e.getMessage());
            return "";
        }
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        try {
            javax.crypto.SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Date expiration = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}


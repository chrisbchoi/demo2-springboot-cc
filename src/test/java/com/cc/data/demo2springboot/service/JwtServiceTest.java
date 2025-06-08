package com.cc.data.demo2springboot.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private String testUsername;
    private List<String> testRoles;
    private List<String> multipleRoles;
    private final long TEST_EXPIRATION = 3600000; // 1 hour in milliseconds

    @BeforeEach
    void setUp() {
        testUsername = "testuser";
        testRoles = Collections.singletonList("USER");
        multipleRoles = Arrays.asList("ADMIN", "USER");

        // Override the default expiration for predictable testing
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Generated token should not be empty")
    void generateToken_shouldReturnNonEmptyToken() {
        // Act
        String token = jwtService.generateToken(testUsername, testRoles);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Valid token should be validated successfully")
    void validateToken_withValidToken_shouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(testUsername, testRoles);

        // Act & Assert
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Invalid token should fail validation")
    void validateToken_withInvalidToken_shouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtService.validateToken("invalid.jwt.token"));
    }

    @Test
    @DisplayName("Tampered token should fail validation")
    void validateToken_withTamperedToken_shouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken(testUsername, testRoles);
        String tamperedToken = token.substring(0, token.length() - 5) + "12345"; // Change last 5 chars

        // Act & Assert
        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Should extract correct username from token")
    void extractUsername_shouldReturnCorrectUsername() {
        // Arrange
        String token = jwtService.generateToken(testUsername, testRoles);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    @DisplayName("Should extract correct roles from token")
    void extractClaims_shouldReturnCorrectRoles() {
        // Arrange
        String token = jwtService.generateToken(testUsername, testRoles);

        // Act
        Claims claims = jwtService.extractClaims(token);
        String roles = (String) claims.get("roles");

        // Assert
        assertEquals("USER", roles);
    }

    @Test
    @DisplayName("Should extract multiple roles properly")
    void extractClaims_withMultipleRoles_shouldReturnAllRoles() {
        // Arrange
        String token = jwtService.generateToken(testUsername, multipleRoles);

        // Act
        Claims claims = jwtService.extractClaims(token);
        String roles = (String) claims.get("roles");

        // Assert
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("USER"));
        assertEquals("ADMIN,USER", roles);
    }

    @Test
    @DisplayName("Token should have correct expiration time")
    void generateToken_shouldSetCorrectExpirationTime() {
        // Arrange
        long currentTimeMillis = System.currentTimeMillis();

        // Act
        String token = jwtService.generateToken(testUsername, testRoles);
        Claims claims = jwtService.extractClaims(token);
        Date expirationDate = claims.getExpiration();

        // Assert
        // Allow small timing differences (within 1 second)
        long expectedExpirationTimeMillis = currentTimeMillis + TEST_EXPIRATION;
        long actualExpirationTimeMillis = expirationDate.getTime();

        // Verify expiration time is within 1 second of expected (handles test execution timing variations)
        assertTrue(Math.abs(expectedExpirationTimeMillis - actualExpirationTimeMillis) < 1000);
    }

    @Test
    @DisplayName("Token should contain all required standard claims")
    void generateToken_shouldContainStandardClaims() {
        // Arrange & Act
        String token = jwtService.generateToken(testUsername, testRoles);
        Claims claims = jwtService.extractClaims(token);

        // Assert
        assertNotNull(claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(testUsername, claims.getSubject());
    }

    @Test
    @DisplayName("Token without roles should still be valid")
    void generateToken_withEmptyRoles_shouldCreateValidToken() {
        // Arrange
        List<String> emptyRoles = Collections.emptyList();

        // Act
        String token = jwtService.generateToken(testUsername, emptyRoles);

        // Assert
        assertTrue(jwtService.validateToken(token));
        Claims claims = jwtService.extractClaims(token);
        assertEquals("", claims.get("roles"));
    }

    @Test
    @DisplayName("Token with expired time should fail validation")
    void validateToken_withExpiredToken_shouldReturnFalse() throws Exception {
        // Arrange - Set a very short expiration for this specific test
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1); // 1 millisecond
        String token = jwtService.generateToken(testUsername, testRoles);

        // Wait for token to expire
        Thread.sleep(10);

        // Act & Assert
        assertFalse(jwtService.validateToken(token));

        // Reset expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
    }
}

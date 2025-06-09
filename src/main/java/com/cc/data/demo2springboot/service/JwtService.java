package com.cc.data.demo2springboot.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * Service class for handling JWT token generation and validation.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public JwtService() {
        // Try to get secret from environment variable first, fall back to other methods if not available
        String secret = System.getenv("JWT_SECRET");

        if (secret == null || secret.trim().isEmpty()) {
            // Fallback to property if environment variable isn't set
            secret = System.getProperty("jwt.secret");
        }

        // If neither is available, use a default (for development only)
        if (secret == null || secret.trim().isEmpty()) {
            secret = "1DUMMY472B4B6250655368566D5971337336763979214226452948404D635166"; // Default for dev only
        }

        // Decode the hex string to bytes for proper key length
        byte[] keyBytes;
        try {
            keyBytes = hexStringToByteArray(secret);
        } catch (Exception e) {
            // If not a valid hex string, try to use Base64 encoding
            keyBytes = Base64.getDecoder().decode(secret);
        }

        // Create signing key
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for a given username and roles
     *
     * @param username the username for which to generate a token
     * @param roles the user's roles
     * @return the JWT token string
     */
    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", String.join(",", roles));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token
     *
     * @param token the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts claims from a JWT token
     *
     * @param token the JWT token
     * @return the claims
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts username from a JWT token
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Convert a hex string to byte array
     */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

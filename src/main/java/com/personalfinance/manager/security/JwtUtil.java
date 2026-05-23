package com.personalfinance.manager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
public class JwtUtil {

    // Strong key of at least 256 bits (32 bytes) for HS256 signature algorithm
    private static final String SECRET_KEY_STRING = "fintech_secure_personal_finance_manager_jwt_secret_key_2026_generation";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    // Token validity: 24 hours (in milliseconds)
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /**
     * Generates a signed JWT with the username as the subject and the userId as a claim.
     */
    @NonNull
    public String generateToken(String username, UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        return Objects.requireNonNull(Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact());
    }

    /**
     * Extracts the username (subject) from the JWT.
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extracts the userId claim from the JWT.
     */
    public UUID getUserIdFromToken(String token) {
        String userIdStr = getClaims(token).get("userId", String.class);
        return UUID.fromString(userIdStr);
    }

    /**
     * Validates the JWT signature and expiration.
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

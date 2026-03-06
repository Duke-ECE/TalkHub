package com.talkhub.backend.security;

import com.talkhub.backend.config.AppProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long expiresSeconds;

    public JwtService(AppProperties appProperties) {
        byte[] keyBytes = deriveKey(appProperties.getJwtSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiresSeconds = appProperties.getJwtExpiresSeconds();
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expiresSeconds);

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claims(Map.of("username", username))
            .signWith(secretKey)
            .compact();
    }

    private byte[] deriveKey(String sourceSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(sourceSecret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize SHA-256 for JWT key derivation", e);
        }
    }
}

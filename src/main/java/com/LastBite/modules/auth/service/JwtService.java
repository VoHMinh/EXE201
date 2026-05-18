package com.LastBite.modules.auth.service;

import com.LastBite.modules.auth.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Handles JWT access token generation and refresh token creation.
 * <p>
 * Access tokens = JWT (signed, stateless, short-lived).
 * Refresh tokens = opaque random string (hashed, stored in DB+Redis).
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.signer-key}")
    private String signerKeyBase64;

    @Value("${jwt.access-token-duration:1800}")
    private long accessTokenDuration;

    @Value("${jwt.refresh-token-duration:604800}")
    private long refreshTokenDuration;

    private MACSigner macSigner;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    void init() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(signerKeyBase64);
            this.macSigner = new MACSigner(keyBytes);
            log.info("JwtService initialised (HS512, access={}s, refresh={}s)",
                    accessTokenDuration, refreshTokenDuration);
        } catch (KeyLengthException e) {
            throw new IllegalStateException("JWT signer key too short for HS512", e);
        }
    }

    /**
     * Generate a signed JWT access token.
     */
    public String generateAccessToken(User user) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(accessTokenDuration);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("user_id", user.getId().toString())
                    .claim("roles", List.of(user.getRole().name()))
                    .jwtID(UUID.randomUUID().toString())
                    .issuer("lastbite")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS512), claims);
            signedJWT.sign(macSigner);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate access token", e);
        }
    }

    /**
     * Generate a cryptographically random refresh token (opaque, not JWT).
     */
    public String generateRefreshToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256 hash of a refresh token for secure storage.
     */
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public long getAccessTokenDuration() {
        return accessTokenDuration;
    }

    public long getRefreshTokenDuration() {
        return refreshTokenDuration;
    }
}

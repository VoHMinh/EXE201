package com.LastBite.common.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Custom JWT decoder using HMAC-SHA512.
 * <p>
 * Delegates entirely to {@link NimbusJwtDecoder} which handles signature
 * verification, expiry checking, and throws the correct {@code BadJwtException}
 * that Spring Security translates into a clean 401 response.
 */
@Slf4j
@Component
public class JwtTokenProvider implements JwtDecoder {

    @Value("${jwt.signer-key}")
    private String signerKey;

    private NimbusJwtDecoder nimbusDecoder;

    @PostConstruct
    void init() {
        byte[] keyBytes = Base64.getDecoder().decode(signerKey);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA512");

        this.nimbusDecoder = NimbusJwtDecoder.withSecretKey(keySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        log.info("JWT token provider initialised (HS512)");
    }

    @Override
    public Jwt decode(String token) {
        return nimbusDecoder.decode(token);
    }
}

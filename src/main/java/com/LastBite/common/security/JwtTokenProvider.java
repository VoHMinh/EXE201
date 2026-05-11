package com.LastBite.common.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

/**
 * Custom JWT decoder using HMAC-SHA512.
 * <p>
 * <b>Improvement over DiHouse:</b> uses {@code @PostConstruct} to eagerly
 * initialise the {@link NimbusJwtDecoder}, making the component thread-safe
 * from the very first request.
 */
@Slf4j
@Component
public class JwtTokenProvider implements JwtDecoder {

    @Value("${jwt.signer-key}")
    private String signerKey;

    private NimbusJwtDecoder nimbusDecoder;
    private JWSVerifier verifier;

    @PostConstruct
    void init() {
        byte[] keyBytes = Base64.getDecoder().decode(signerKey);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA512");

        this.nimbusDecoder = NimbusJwtDecoder.withSecretKey(keySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        try {
            this.verifier = new MACVerifier(keyBytes);
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to create JWT verifier", e);
        }

        log.info("JWT token provider initialised (HS512)");
    }

    @Override
    public Jwt decode(String token) {
        // 1. Fast-fail: verify signature + expiry before full parse
        fastValidate(token);

        // 2. Delegate to Spring Security's NimbusJwtDecoder for standard Jwt object
        return nimbusDecoder.decode(token);
    }

    /**
     * Lightweight cryptographic + expiry check.
     */
    private void fastValidate(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(verifier)) {
                throw new JwtException("Invalid JWT signature");
            }

            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw new JwtException("JWT expired");
            }
        } catch (JwtException e) {
            throw e;
        } catch (ParseException | JOSEException e) {
            throw new JwtException("Invalid JWT token", e);
        }
    }
}

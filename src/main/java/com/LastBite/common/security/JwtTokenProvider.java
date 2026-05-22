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
 * Bộ decode JWT tùy chỉnh dùng HMAC-SHA512.
 * <p>
 * Ủy quyền cho {@link NimbusJwtDecoder} để kiểm tra chữ ký, thời hạn và ném
 * {@code BadJwtException} phù hợp để Spring Security chuyển thành response 401.
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

        log.info("JWT token provider đã khởi tạo (HS512)");
    }

    @Override
    public Jwt decode(String token) {
        return nimbusDecoder.decode(token);
    }
}

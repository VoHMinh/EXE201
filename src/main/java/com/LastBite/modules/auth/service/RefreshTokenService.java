package com.LastBite.modules.auth.service;

import com.LastBite.common.exception.ApiException;
import com.LastBite.common.exception.ErrorCode;
import com.LastBite.modules.auth.entity.RefreshToken;
import com.LastBite.modules.auth.entity.User;
import com.LastBite.modules.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Manages refresh tokens with Redis + DB dual-write and reuse detection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REDIS_PREFIX = "rt:";
    private static final String REDIS_USER_PREFIX = "rt:user:";

    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = jwtService.generateRefreshToken();
        String tokenHash = jwtService.hashToken(rawToken);

        Instant expiresAt = Instant.now().plusSeconds(jwtService.getRefreshTokenDuration());

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);

        Duration ttl = Duration.between(Instant.now(), expiresAt);
        cacheToken(tokenHash, user.getId(), ttl);

        log.debug("Created refresh token for user {}", user.getEmail());
        return rawToken;
    }

    /**
     * Validate a raw refresh token and return the associated user ID.
     * Redis is a cache only; DB revocation/expiry is still checked so a stale
     * Redis key cannot revive a revoked session.
     */
    public UUID validateAndGetUserId(String rawToken) {
        String tokenHash = jwtService.hashToken(rawToken);

        RefreshToken dbToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID,
                        "Refresh token khong hop le"));

        ensureValidToken(dbToken);

        Duration remaining = Duration.between(Instant.now(), dbToken.getExpiresAt());
        if (!remaining.isNegative()) {
            cacheToken(tokenHash, dbToken.getUser().getId(), remaining);
        }

        return dbToken.getUser().getId();
    }

    @Transactional
    public void revokeToken(String rawToken) {
        String tokenHash = jwtService.hashToken(rawToken);
        redisTemplate.delete(REDIS_PREFIX + tokenHash);

        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    redisTemplate.opsForSet().remove(REDIS_USER_PREFIX + token.getUser().getId(), tokenHash);
                });
    }

    @Transactional
    public void revokeAllByUserId(UUID userId) {
        int count = refreshTokenRepository.revokeAllByUserId(userId);
        deleteCachedTokensForUser(userId);
        log.info("Revoked {} refresh tokens for user {}", count, userId);
    }

    private void ensureValidToken(RefreshToken dbToken) {
        if (dbToken.isRevoked()) {
            UUID userId = dbToken.getUser().getId();
            log.warn("SECURITY: Refresh token reuse detected for user {}. Revoking all sessions.", userId);
            revokeAllByUserId(userId);
            throw new ApiException(ErrorCode.TOKEN_REUSE_DETECTED);
        }

        if (dbToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED, "Refresh token da het han");
        }
    }

    private void cacheToken(String tokenHash, UUID userId, Duration ttl) {
        redisTemplate.opsForValue().set(REDIS_PREFIX + tokenHash, userId.toString(), ttl);
        redisTemplate.opsForSet().add(REDIS_USER_PREFIX + userId, tokenHash);
        redisTemplate.expire(REDIS_USER_PREFIX + userId, ttl);
    }

    private void deleteCachedTokensForUser(UUID userId) {
        String userKey = REDIS_USER_PREFIX + userId;
        var tokenHashes = redisTemplate.opsForSet().members(userKey);
        if (tokenHashes != null && !tokenHashes.isEmpty()) {
            redisTemplate.delete(tokenHashes.stream()
                    .map(hash -> REDIS_PREFIX + hash)
                    .toList());
        }
        redisTemplate.delete(userKey);
    }
}

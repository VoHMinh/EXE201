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
 * Manages refresh tokens with <b>Redis + DB dual-write</b> strategy
 * and <b>reuse detection</b>.
 * <p>
 * <b>Token Rotation:</b> every refresh produces a new token and invalidates the old.
 * <b>Reuse Detection:</b> if an already-revoked token is used again, ALL sessions
 * for that user are revoked (indicates the token was stolen).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    private static final String REDIS_PREFIX = "rt:";

    /**
     * Create and persist a new refresh token for the given user.
     *
     * @return the RAW token (send to client). Hash is stored in DB+Redis.
     */
    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = jwtService.generateRefreshToken();
        String tokenHash = jwtService.hashToken(rawToken);

        Instant expiresAt = Instant.now().plusSeconds(jwtService.getRefreshTokenDuration());

        // 1. Save to DB (source of truth)
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);

        // 2. Cache in Redis with TTL (auto-expires, no cleanup needed)
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        redisTemplate.opsForValue().set(
                REDIS_PREFIX + tokenHash,
                user.getId().toString(),
                ttl
        );

        log.debug("Created refresh token for user {}", user.getEmail());
        return rawToken;
    }

    /**
     * Validate a raw refresh token and return the associated user ID.
     * <p>
     * Checks Redis first, falls back to DB on miss.
     * <b>REUSE DETECTION:</b> if token exists in DB but is already revoked,
     * all sessions for the user are revoked immediately.
     *
     * @throws ApiException if token is invalid, expired, revoked, or reused
     */
    public UUID validateAndGetUserId(String rawToken) {
        String tokenHash = jwtService.hashToken(rawToken);

        // 1. Try Redis first (fast path)
        String cachedUserId = redisTemplate.opsForValue().get(REDIS_PREFIX + tokenHash);

        if (cachedUserId != null) {
            return UUID.fromString(cachedUserId);
        }

        // 2. Redis miss → check DB (slow path)
        RefreshToken dbToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID,
                        "Refresh token không hợp lệ"));

        // ── REUSE DETECTION ──
        // If the token was already revoked, someone is trying to reuse it.
        // This is a strong signal the token was stolen. Revoke ALL sessions.
        if (dbToken.isRevoked()) {
            UUID userId = dbToken.getUser().getId();
            log.warn("🚨 SECURITY: Refresh token REUSE detected for user {}! " +
                    "Revoking ALL sessions.", userId);
            revokeAllByUserId(userId);
            throw new ApiException(ErrorCode.TOKEN_REUSE_DETECTED);
        }

        if (dbToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED, "Refresh token đã hết hạn");
        }

        // 3. Re-cache in Redis (heal the cache)
        Duration remaining = Duration.between(Instant.now(), dbToken.getExpiresAt());
        if (!remaining.isNegative()) {
            redisTemplate.opsForValue().set(
                    REDIS_PREFIX + tokenHash,
                    dbToken.getUser().getId().toString(),
                    remaining
            );
        }

        return dbToken.getUser().getId();
    }

    /**
     * Revoke a specific refresh token (used during token rotation).
     */
    @Transactional
    public void revokeToken(String rawToken) {
        String tokenHash = jwtService.hashToken(rawToken);

        // 1. Delete from Redis (immediate effect)
        redisTemplate.delete(REDIS_PREFIX + tokenHash);

        // 2. Mark revoked in DB (keep record for reuse detection!)
        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Revoke ALL refresh tokens for a user (logout everywhere / ban / reuse detection).
     */
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        // 1. Revoke in DB
        int count = refreshTokenRepository.revokeAllByUserId(userId);

        // 2. Redis keys will expire via TTL — no need to scan and delete

        log.info("Revoked {} refresh tokens for user {}", count, userId);
    }
}

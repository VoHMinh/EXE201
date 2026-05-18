package com.LastBite.modules.auth.job;

import com.LastBite.modules.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled job to clean up expired and revoked refresh tokens from the database.
 * <p>
 * Redis handles its own cleanup via TTL — this job only cleans PostgreSQL.
 * Runs daily at 3:00 AM.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Delete tokens that are:
     * - revoked = true (already used or invalidated)
     * - expired more than 1 day ago (keep recently expired for reuse detection)
     */
    @Scheduled(cron = "0 0 3 * * *") // 3:00 AM daily
    @Transactional
    public void cleanupExpiredTokens() {
        Instant cutoff = Instant.now().minusSeconds(86400); // 1 day buffer
        int deleted = refreshTokenRepository.deleteExpiredOrRevoked(cutoff);
        log.info("Token cleanup: deleted {} expired/revoked refresh tokens", deleted);
    }
}

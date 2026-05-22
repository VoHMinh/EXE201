package com.LastBite.modules.auth.job;

import com.LastBite.modules.auth.repository.EmailVerificationTokenRepository;
import com.LastBite.modules.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Job định kỳ để dọn token hết hạn trong database.
 * <p>
 * Redis tự dọn qua TTL — job này chỉ dọn PostgreSQL.
 * Chạy hằng ngày lúc 3:00 sáng.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailTokenRepository;

    /**
     * Xóa refresh token:
     * - revoked = true (đã dùng hoặc đã thu hồi)
     * - hết hạn hơn 1 ngày (giữ token vừa hết hạn để phát hiện reuse)
     */
    @Scheduled(cron = "0 0 3 * * *") // 3:00 AM daily
    @Transactional
    public void cleanupExpiredTokens() {
        Instant cutoff = Instant.now().minusSeconds(86400); // 1 day buffer
        int deleted = refreshTokenRepository.deleteExpiredOrRevoked(cutoff);
        log.info("Dọn token: đã xóa {} refresh token hết hạn/đã thu hồi", deleted);
    }

    /**
     * Xóa OTP hết hạn vì không cần giữ sau khi hết hạn.
     */
    @Scheduled(cron = "0 30 3 * * *") // 3:30 AM daily
    @Transactional
    public void cleanupExpiredOtpTokens() {
        int deleted = emailTokenRepository.deleteExpired(Instant.now());
        log.info("Dọn OTP: đã xóa {} OTP hết hạn", deleted);
    }
}

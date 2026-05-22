package com.LastBite.modules.auth.repository;

import com.LastBite.modules.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    /**
     * Tìm OTP mới nhất của người dùng, chưa hết hạn và chưa xác minh.
     */
    @Query("""
        SELECT t FROM EmailVerificationToken t
        WHERE t.user.id = :userId
          AND t.otpCode IS NOT NULL
          AND t.verified = false
          AND t.expiresAt > :now
        ORDER BY t.createdAt DESC LIMIT 1
        """)
    Optional<EmailVerificationToken> findLatestValidOtpByUserId(UUID userId, Instant now);

    /**
     * Tìm challenge link xác minh chưa hết hạn theo token hash.
     */
    @Query("""
        SELECT t FROM EmailVerificationToken t
        WHERE t.tokenHash = :tokenHash
          AND t.tokenHash IS NOT NULL
          AND t.verified = false
          AND t.expiresAt > :now
        """)
    Optional<EmailVerificationToken> findValidLinkByTokenHash(String tokenHash, Instant now);

    /**
     * Vô hiệu hóa toàn bộ token cũ của người dùng trước khi cấp token mới.
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.verified = false")
    int deleteUnverifiedByUserId(UUID userId);

    /**
     * Dọn token hết hạn bằng scheduled job.
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :cutoff")
    int deleteExpired(Instant cutoff);
}

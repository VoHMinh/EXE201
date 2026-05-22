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
     * Find the latest non-expired, non-verified OTP for a user.
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
     * Find a non-expired verification-link challenge by token hash.
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
     * Invalidate all previous tokens for a user (used before issuing a new OTP).
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.verified = false")
    int deleteUnverifiedByUserId(UUID userId);

    /**
     * Cleanup expired tokens (scheduled job).
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :cutoff")
    int deleteExpired(Instant cutoff);
}

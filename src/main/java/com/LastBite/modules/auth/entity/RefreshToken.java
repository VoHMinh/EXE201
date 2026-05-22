package com.LastBite.modules.auth.entity;

import com.LastBite.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Refresh token — lưu dạng hash trong DB để đảm bảo an toàn.
 * <p>
 * Áp dụng <b>xoay vòng token</b>: mỗi lần refresh tạo token mới và vô hiệu token cũ.
 * Việc dùng lại token cũ sẽ kích hoạt thu hồi toàn bộ phiên.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_tokens_token_hash", columnList = "token_hash")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Hash SHA-256 của token thật (không bao giờ lưu token thô). */
    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;
}

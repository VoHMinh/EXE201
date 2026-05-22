package com.LastBite.modules.auth.entity;

import com.LastBite.common.entity.BaseEntity;
import com.LastBite.modules.auth.enums.StoreRegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Hồ sơ đăng ký chủ cửa hàng — cần admin duyệt trước khi cấp ROLE_STORE_OWNER.
 * <p>
 * Luồng: Người dùng gửi form → status = PENDING → Admin duyệt →
 * APPROVED (user.role → STORE_OWNER) hoặc REJECTED (kèm lý do).
 */
@Entity
@Table(name = "store_registrations", indexes = {
        @Index(name = "idx_store_reg_user_id", columnList = "user_id"),
        @Index(name = "idx_store_reg_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRegistration extends BaseEntity {

    /** Người dùng đã gửi hồ sơ này. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "business_license_number", length = 100)
    private String businessLicenseNumber;

    /** URL ảnh giấy phép kinh doanh đã upload (lưu trên S3/cloud storage). */
    @Column(name = "business_license_image_url", length = 500)
    private String businessLicenseImageUrl;

    @Column(nullable = false, length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StoreRegistrationStatus status = StoreRegistrationStatus.PENDING;

    /** Admin đã duyệt hồ sơ này. */
    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
}

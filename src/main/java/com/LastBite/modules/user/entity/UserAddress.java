package com.LastBite.modules.user.entity;

import com.LastBite.common.entity.BaseEntity;
import com.LastBite.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Delivery address belonging to a user.
 * One user can have multiple addresses with one marked as default.
 */
@Entity
@Table(name = "user_addresses", indexes = {
        @Index(name = "idx_user_addresses_user_id", columnList = "user_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Label: HOME, OFFICE, OTHER */
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String label = "HOME";

    @Column(name = "full_address", nullable = false, columnDefinition = "TEXT")
    private String fullAddress;

    private Double lat;
    private Double lng;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}

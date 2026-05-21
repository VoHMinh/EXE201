package com.LastBite.modules.store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Reliability statistics for a store (1:1 with stores).
 * <p>
 * Tracks fulfillment rate for the auto-penalty system.
 * Created automatically when a store is registered.
 */
@Entity
@Table(name = "store_reliability_stats")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReliabilityStats {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "total_bags_listed", nullable = false)
    @Builder.Default
    private int totalBagsListed = 0;

    @Column(name = "total_bags_sold", nullable = false)
    @Builder.Default
    private int totalBagsSold = 0;

    @Column(name = "total_bags_fulfilled", nullable = false)
    @Builder.Default
    private int totalBagsFulfilled = 0;

    @Column(name = "total_bags_no_show", nullable = false)
    @Builder.Default
    private int totalBagsNoShow = 0;

    @Column(name = "fulfillment_rate", nullable = false)
    @Builder.Default
    private double fulfillmentRate = 1.0;

    @Column(name = "warning_count", nullable = false)
    @Builder.Default
    private int warningCount = 0;

    @Column(name = "is_under_review", nullable = false)
    @Builder.Default
    private boolean underReview = false;

    @Column(name = "suspended_until")
    private Instant suspendedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

package com.LastBite.modules.bag.entity;

import com.LastBite.common.entity.BaseEntity;
import com.LastBite.modules.bag.enums.BagStatus;
import com.LastBite.modules.bag.enums.BagType;
import com.LastBite.modules.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "surprise_bags", indexes = {
        @Index(name = "idx_surprise_bags_store_id", columnList = "store_id"),
        @Index(name = "idx_surprise_bags_store_status", columnList = "store_id,status"),
        @Index(name = "idx_surprise_bags_pickup_window", columnList = "pickup_start_time,pickup_end_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SurpriseBag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "bag_type", nullable = false, length = 30)
    @Builder.Default
    private BagType bagType = BagType.STANDARD;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text array")
    private String[] photos;

    @Column(name = "estimated_value", nullable = false, precision = 10, scale = 0)
    private BigDecimal estimatedValue;

    @Column(name = "sale_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal salePrice;

    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 0)
    @Builder.Default
    private BigDecimal platformFee = BigDecimal.valueOf(4000);

    @Column(name = "max_per_order", nullable = false)
    @Builder.Default
    private int maxPerOrder = 1;

    @Column(name = "pickup_start_time", nullable = false)
    private LocalTime pickupStartTime;

    @Column(name = "pickup_end_time", nullable = false)
    private LocalTime pickupEndTime;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "available_days", nullable = false, columnDefinition = "integer array")
    private Integer[] availableDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BagStatus status = BagStatus.DRAFT;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private int version = 1;
}

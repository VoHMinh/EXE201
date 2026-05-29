package com.LastBite.modules.bag.entity;

import com.LastBite.common.entity.BaseEntity;
import com.LastBite.modules.bag.enums.BagSize;
import com.LastBite.modules.store.enums.StoreCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "bag_price_tiers", indexes = {
        @Index(name = "idx_bag_price_tiers_category_size", columnList = "category,bag_size", unique = true),
        @Index(name = "idx_bag_price_tiers_active", columnList = "active")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BagPriceTier extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StoreCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "bag_size", nullable = false, length = 30)
    private BagSize bagSize;

    @Column(name = "minimum_value", nullable = false, precision = 10, scale = 0)
    private BigDecimal minimumValue;

    @Column(name = "base_sale_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal baseSalePrice;

    @Column(name = "dynamic_min_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal dynamicMinPrice;

    @Column(name = "dynamic_max_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal dynamicMaxPrice;

    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 0)
    private BigDecimal platformFee;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}

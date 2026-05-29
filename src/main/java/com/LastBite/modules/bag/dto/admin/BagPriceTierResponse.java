package com.LastBite.modules.bag.dto.admin;

import com.LastBite.modules.bag.enums.BagSize;
import com.LastBite.modules.store.enums.StoreCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BagPriceTierResponse {
    private UUID id;
    private StoreCategory category;
    private BagSize bagSize;
    private BigDecimal minimumValue;
    private BigDecimal baseSalePrice;
    private BigDecimal dynamicMinPrice;
    private BigDecimal dynamicMaxPrice;
    private BigDecimal platformFee;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}

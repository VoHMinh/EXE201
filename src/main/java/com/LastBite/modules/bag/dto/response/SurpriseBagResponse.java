package com.LastBite.modules.bag.dto.response;

import com.LastBite.modules.bag.enums.BagStatus;
import com.LastBite.modules.bag.enums.BagType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SurpriseBagResponse {
    private UUID id;
    private UUID storeId;
    private String storeName;
    private String name;
    private String description;
    private BagType bagType;
    private List<String> photos;
    private BigDecimal estimatedValue;
    private BigDecimal salePrice;
    private BigDecimal platformFee;
    private int maxPerOrder;
    private LocalTime pickupStartTime;
    private LocalTime pickupEndTime;
    private List<Integer> availableDays;
    private BagStatus status;
    private int version;
    private DailyStockResponse todayStock;
    private Instant createdAt;
    private Instant updatedAt;
}

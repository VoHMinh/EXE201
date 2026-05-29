package com.LastBite.modules.order.dto.response;

import com.LastBite.modules.order.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private UUID storeId;
    private String storeName;
    private UUID bagId;
    private String bagName;
    private UUID dailyStockId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal platformFee;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private OrderStatus status;
    private String pickupCode;
    private LocalDate pickupDate;
    private LocalTime pickupStartTime;
    private LocalTime pickupEndTime;
    private Instant reservedUntil;
    private String idempotencyKey;
    private Instant createdAt;
    private Instant updatedAt;
}

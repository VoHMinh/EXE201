package com.LastBite.modules.bag.dto.response;

import com.LastBite.modules.bag.enums.DailyStockStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class DailyStockResponse {
    private UUID id;
    private UUID bagId;
    private LocalDate date;
    private int quantity;
    private int reserved;
    private int sold;
    private int available;
    private DailyStockStatus status;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;
}

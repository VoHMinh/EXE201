package com.LastBite.modules.bag.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdjustTodayStockRequest {

    @Min(value = -50, message = "Mỗi lần chỉ được giảm tối đa 50 túi")
    @Max(value = 50, message = "Mỗi lần chỉ được tăng tối đa 50 túi")
    private int delta;

    private String reason;
}

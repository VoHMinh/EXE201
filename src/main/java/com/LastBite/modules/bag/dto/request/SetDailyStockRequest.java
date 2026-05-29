package com.LastBite.modules.bag.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetDailyStockRequest {

    @Min(value = 0, message = "Số lượng túi không được âm")
    @Max(value = 50, message = "Mỗi túi chỉ được set tối đa 50 phần/ngày")
    private int quantity;

    private String reason;
}

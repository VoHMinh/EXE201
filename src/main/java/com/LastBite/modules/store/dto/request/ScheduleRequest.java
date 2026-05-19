package com.LastBite.modules.store.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ScheduleRequest {

    @NotNull(message = "Ngày trong tuần không được để trống")
    @Min(value = 0, message = "Ngày phải từ 0 (Chủ nhật) đến 6 (Thứ 7)")
    @Max(value = 6, message = "Ngày phải từ 0 (Chủ nhật) đến 6 (Thứ 7)")
    private Integer dayOfWeek;

    @NotNull(message = "Giờ mở cửa không được để trống")
    private LocalTime openTime;

    @NotNull(message = "Giờ đóng cửa không được để trống")
    private LocalTime closeTime;

    private Boolean isOpen = true;
}

package com.LastBite.modules.bag.dto.request;

import com.LastBite.modules.bag.enums.BagType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CreateSurpriseBagRequest {

    @NotBlank(message = "Tên túi không được để trống")
    @Size(max = 255, message = "Tên túi tối đa 255 ký tự")
    private String name;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    private BagType bagType = BagType.STANDARD;

    private List<@NotBlank(message = "URL ảnh không được để trống") String> photos;

    @NotNull(message = "Giá trị ước tính không được để trống")
    @Positive(message = "Giá trị ước tính phải lớn hơn 0")
    private BigDecimal estimatedValue;

    @NotNull(message = "Giá bán không được để trống")
    @Positive(message = "Giá bán phải lớn hơn 0")
    private BigDecimal salePrice;

    private BigDecimal platformFee;

    @Min(value = 1, message = "Mỗi đơn phải cho mua ít nhất 1 túi")
    @Max(value = 3, message = "Mỗi khách tối đa 3 túi/ngày/cửa hàng")
    private Integer maxPerOrder = 1;

    @NotNull(message = "Giờ bắt đầu pickup không được để trống")
    private LocalTime pickupStartTime;

    @NotNull(message = "Giờ kết thúc pickup không được để trống")
    private LocalTime pickupEndTime;

    @NotEmpty(message = "Cần chọn ít nhất 1 ngày bán")
    private Set<@Min(value = 0, message = "Ngày bán phải từ 0 đến 6")
                @Max(value = 6, message = "Ngày bán phải từ 0 đến 6") Integer> availableDays;
}

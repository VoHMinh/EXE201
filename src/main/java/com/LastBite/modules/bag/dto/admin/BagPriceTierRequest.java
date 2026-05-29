package com.LastBite.modules.bag.dto.admin;

import com.LastBite.modules.bag.enums.BagSize;
import com.LastBite.modules.store.enums.StoreCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BagPriceTierRequest {

    @NotNull(message = "Danh mục không được để trống")
    private StoreCategory category;

    @NotNull(message = "Kích cỡ túi không được để trống")
    private BagSize bagSize;

    @NotNull(message = "Giá trị tối thiểu không được để trống")
    @Positive(message = "Giá trị tối thiểu phải lớn hơn 0")
    private BigDecimal minimumValue;

    @NotNull(message = "Giá bán chuẩn không được để trống")
    @Positive(message = "Giá bán chuẩn phải lớn hơn 0")
    private BigDecimal baseSalePrice;

    @NotNull(message = "Giá động thấp nhất không được để trống")
    @Positive(message = "Giá động thấp nhất phải lớn hơn 0")
    private BigDecimal dynamicMinPrice;

    @NotNull(message = "Giá động cao nhất không được để trống")
    @Positive(message = "Giá động cao nhất phải lớn hơn 0")
    private BigDecimal dynamicMaxPrice;

    @NotNull(message = "Phí nền tảng không được để trống")
    @PositiveOrZero(message = "Phí nền tảng không được âm")
    private BigDecimal platformFee;

    private Boolean active = true;
}

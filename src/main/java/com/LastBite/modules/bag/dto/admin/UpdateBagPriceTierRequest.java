package com.LastBite.modules.bag.dto.admin;

import com.LastBite.modules.bag.enums.BagSize;
import com.LastBite.modules.store.enums.StoreCategory;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateBagPriceTierRequest {

    private StoreCategory category;

    private BagSize bagSize;

    @Positive(message = "Giá trị tối thiểu phải lớn hơn 0")
    private BigDecimal minimumValue;

    @Positive(message = "Giá bán chuẩn phải lớn hơn 0")
    private BigDecimal baseSalePrice;

    @Positive(message = "Giá động thấp nhất phải lớn hơn 0")
    private BigDecimal dynamicMinPrice;

    @Positive(message = "Giá động cao nhất phải lớn hơn 0")
    private BigDecimal dynamicMaxPrice;

    @PositiveOrZero(message = "Phí nền tảng không được âm")
    private BigDecimal platformFee;

    private Boolean active;
}

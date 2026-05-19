package com.LastBite.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @Size(max = 50, message = "Nhãn tối đa 50 ký tự")
    private String label;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 1000, message = "Địa chỉ tối đa 1000 ký tự")
    private String fullAddress;

    private Double lat;
    private Double lng;
    private Boolean isDefault;
}

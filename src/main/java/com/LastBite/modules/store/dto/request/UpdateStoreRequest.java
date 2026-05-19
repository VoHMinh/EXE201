package com.LastBite.modules.store.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStoreRequest {

    @Size(max = 255, message = "Tên cửa hàng tối đa 255 ký tự")
    private String name;

    private String description;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String email;

    private String address;
    private Double lat;
    private Double lng;

    @Size(max = 500)
    private String coverImageUrl;

    @Size(max = 500)
    private String logoUrl;
}

package com.LastBite.modules.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @Size(max = 500, message = "URL ảnh đại diện tối đa 500 ký tự")
    private String avatarUrl;
}

package com.LastBite.modules.auth.dto.request;

import com.LastBite.modules.store.enums.StoreCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for partner (store owner) registration.
 * Creates BOTH the user account (role = STORE_OWNER) AND the store in a single step.
 */
@Data
public class RegisterPartnerRequest {

    // ── Thông tin tài khoản ──

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu phải từ 8-100 ký tự")
    private String password;

    @NotBlank(message = "Họ tên chủ cửa hàng không được để trống")
    @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String ownerPhone;

    // ── Thông tin cửa hàng ──

    @NotBlank(message = "Tên cửa hàng không được để trống")
    @Size(max = 255, message = "Tên cửa hàng tối đa 255 ký tự")
    private String storeName;

    private String storeDescription;

    @NotNull(message = "Danh mục cửa hàng không được để trống")
    private StoreCategory storeCategory;

    @Size(max = 20)
    private String storePhone;

    @Size(max = 255)
    private String storeEmail;

    @NotBlank(message = "Địa chỉ cửa hàng không được để trống")
    private String storeAddress;

    @Size(max = 100)
    private String storeDistrict;

    @Size(max = 100)
    private String storeCity;

    private Double lat;
    private Double lng;

    @Size(max = 500)
    private String coverImageUrl;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 100)
    private String businessLicenseNumber;

    @Size(max = 500)
    private String businessLicenseImageUrl;
}

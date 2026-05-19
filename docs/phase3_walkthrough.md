# Phase 3 — User Profile + Store Module — Walkthrough

## Tổng quan

Đã triển khai **31 file mới**, bao gồm:
- 🗄️ 1 migration SQL (V3)
- 📦 3 entities, 3 enums, 3 repositories
- 📝 7 DTOs (request + response)
- ⚙️ 4 services (UserService, AddressService, StoreService, StoreQueryService)
- 🌐 3 controllers (UserController, StoreOwnerController, StorePublicController)
- 🔧 2 utilities (CacheConfig, SlugUtil)

---

## Database Migration — V3

[V3__user_profile_and_store.sql](file:///d:/EXE201/LastBite_BE/src/main/resources/db/migration/V3__user_profile_and_store.sql)

| Thay đổi | Chi tiết |
|---|---|
| `ALTER users` | Thêm cột `last_login_at` (ghi lại lần login gần nhất) |
| `CREATE user_addresses` | Lưu nhiều địa chỉ giao hàng cho mỗi user |
| `CREATE stores` | Thông tin cửa hàng + verification_status |
| `CREATE store_schedules` | Lịch mở/đóng cửa theo ngày trong tuần |

---

## API Endpoints

### 🔒 User Profile (yêu cầu đăng nhập)

| Method | Endpoint | Nhận (Request) | Trả (Response) | Mô tả |
|---|---|---|---|---|
| `GET` | `/api/v1/users/me` | — | `UserResponse` | Lấy hồ sơ (cached 30 phút) |
| `PUT` | `/api/v1/users/me` | `UpdateProfileRequest` | `UserResponse` | Cập nhật fullName, phone, avatarUrl |
| `PUT` | `/api/v1/users/me/password` | `ChangePasswordRequest` | — | Đổi mật khẩu (cần oldPassword) |

### 🔒 User Addresses (yêu cầu đăng nhập)

| Method | Endpoint | Nhận | Trả | Mô tả |
|---|---|---|---|---|
| `GET` | `/api/v1/users/me/addresses` | — | `List<AddressResponse>` | Danh sách (cached 30 phút) |
| `POST` | `/api/v1/users/me/addresses` | `AddressRequest` | `AddressResponse` | Thêm mới (tối đa 10) |
| `PUT` | `/api/v1/users/me/addresses/{id}` | `AddressRequest` | `AddressResponse` | Cập nhật |
| `DELETE` | `/api/v1/users/me/addresses/{id}` | — | — | Xóa |
| `PATCH` | `/api/v1/users/me/addresses/{id}/default` | — | `AddressResponse` | Đặt mặc định |

### 🔒 Store Owner (yêu cầu đăng nhập)

| Method | Endpoint | Nhận | Trả | Mô tả |
|---|---|---|---|---|
| `POST` | `/api/v1/store-owner/store` | `CreateStoreRequest` | `StoreDetailResponse` | Tạo store (tự promote role → STORE_OWNER) |
| `GET` | `/api/v1/store-owner/store` | — | `StoreDetailResponse` | Xem store của mình |
| `PUT` | `/api/v1/store-owner/store` | `UpdateStoreRequest` | `StoreDetailResponse` | Cập nhật thông tin |
| `PUT` | `/api/v1/store-owner/store/schedules` | `List<ScheduleRequest>` | `StoreDetailResponse` | Cập nhật lịch |
| `PATCH` | `/api/v1/store-owner/store/pause` | — | `StoreDetailResponse` | Tạm ngưng |
| `PATCH` | `/api/v1/store-owner/store/activate` | — | `StoreDetailResponse` | Mở lại |

### 🌐 Store Public (KHÔNG cần đăng nhập)

| Method | Endpoint | Params | Trả | Mô tả |
|---|---|---|---|---|
| `GET` | `/api/v1/stores` | `keyword`, `category`, `page`, `size` | `Page<StoreResponse>` | Tìm kiếm (cached 5 phút) |
| `GET` | `/api/v1/stores/{slug}` | — | `StoreDetailResponse` | Chi tiết (cached 15 phút) |

---

## DTO Formats

### UpdateProfileRequest
```json
{
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "avatarUrl": "https://..."
}
```

### ChangePasswordRequest
```json
{
  "oldPassword": "matkhaucu123",
  "newPassword": "matkhaumoi456"
}
```

### AddressRequest
```json
{
  "label": "HOME",
  "fullAddress": "123 Nguyễn Huệ, Q1, TP.HCM",
  "lat": 10.7769,
  "lng": 106.7009,
  "isDefault": true
}
```

### CreateStoreRequest
```json
{
  "name": "Bánh Mì Sài Gòn",
  "description": "Bánh mì nóng giòn, giá rẻ",
  "category": "BAKERY",
  "phone": "0909123456",
  "email": "store@example.com",
  "address": "456 Lê Lợi, Q1, TP.HCM",
  "lat": 10.7731,
  "lng": 106.6999,
  "coverImageUrl": "https://...",
  "logoUrl": "https://...",
  "businessLicenseNumber": "41A-12345",
  "businessLicenseImageUrl": "https://..."
}
```

### ScheduleRequest (gửi list 7 ngày)
```json
[
  { "dayOfWeek": 1, "openTime": "07:00", "closeTime": "21:00", "isOpen": true },
  { "dayOfWeek": 0, "openTime": "08:00", "closeTime": "17:00", "isOpen": false }
]
```

### StoreResponse (kết quả tìm kiếm)
```json
{
  "id": "uuid",
  "name": "Bánh Mì Sài Gòn",
  "slug": "banh-mi-sai-gon",
  "category": "BAKERY",
  "address": "456 Lê Lợi, Q1",
  "avgRating": 4.5,
  "totalRatings": 120,
  "status": "ACTIVE",
  "verificationStatus": "VERIFIED"
}
```

### StoreDetailResponse (chi tiết + lịch)
```json
{
  "id": "uuid",
  "ownerId": "uuid",
  "name": "Bánh Mì Sài Gòn",
  "slug": "banh-mi-sai-gon",
  "...tất cả fields...",
  "schedules": [
    { "dayOfWeek": 1, "openTime": "07:00", "closeTime": "21:00", "isOpen": true }
  ]
}
```

---

## Redis Caching Strategy (Cache-Aside)

| Cache Name | TTL | Key | Evicted When |
|---|---|---|---|
| `user-profile` | 30 phút | `userId` | updateProfile, changePassword |
| `user-addresses` | 30 phút | `userId` | add/update/delete address, setDefault |
| `store-detail` | 15 phút | `ownerId` | update store, update schedules, pause/activate |
| `store-by-slug` | 15 phút | `slug` | update store, update schedules |
| `store-list` | 5 phút | `search:keyword:category:page:size` | update store, pause/activate |

**Flow:**
```
GET (read) → Check Redis → Hit → return cached
                          → Miss → query DB → save to Redis → return
CREATE/UPDATE/DELETE → Update DB → @CacheEvict → xóa cache cũ
GET tiếp theo → cache miss → query DB mới → cache lại
```

---

## N+1 Prevention

| Vấn đề | Giải pháp |
|---|---|
| `Store` → `schedules` (OneToMany) | `@NamedEntityGraph("Store.withSchedules")` + `@EntityGraph` trong Repository |
| `Store` → `owner` (OneToOne LAZY) | Chỉ truy cập `owner.getId()` (không trigger proxy load) |
| `UserAddress` → `user` | Dùng `getReferenceById()` thay vì `findById()` khi tạo address |

---

## Store Verification Flow

```
Customer đăng ký → role = CUSTOMER
    ↓
Gọi POST /store-owner/store → Tạo store (verification = PENDING)
                             → user.role = STORE_OWNER
    ↓
Admin duyệt (chưa implement endpoint):
  ✅ VERIFIED → store hiện lên /api/v1/stores (public)
  ❌ REJECTED → store.rejectionReason = "Lý do"
```

> **Lưu ý:** Admin endpoints chưa implement trong phase này. Cần thêm ở phase tiếp theo.

---

## Cấu trúc thư mục mới

```
modules/
├── auth/          (đã có)
├── user/          ← MỚI
│   ├── controller/UserController.java
│   ├── dto/request/  (3 files)
│   ├── dto/response/ (1 file)
│   ├── entity/UserAddress.java
│   ├── repository/UserAddressRepository.java
│   └── service/  (UserService, AddressService)
└── store/         ← MỚI
    ├── controller/ (StoreOwnerController, StorePublicController)
    ├── dto/request/  (3 files)
    ├── dto/response/ (2 files)
    ├── entity/ (Store, StoreSchedule)
    ├── enums/  (StoreCategory, StoreStatus, VerificationStatus)
    ├── repository/ (StoreRepository, StoreScheduleRepository)
    └── service/ (StoreService, StoreQueryService)
```

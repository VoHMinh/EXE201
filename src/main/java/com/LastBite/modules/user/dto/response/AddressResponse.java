package com.LastBite.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse implements Serializable {
    private UUID id;
    private String label;
    private String fullAddress;
    private Double lat;
    private Double lng;
    private boolean isDefault;
    private Instant createdAt;
}

package com.LastBite.modules.store.dto.response;

import com.LastBite.modules.store.enums.StoreCategory;
import com.LastBite.modules.store.enums.StoreStatus;
import com.LastBite.modules.store.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Store summary — used in search results (no schedules).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse implements Serializable {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private StoreCategory category;
    private String address;
    private Double lat;
    private Double lng;
    private String coverImageUrl;
    private String logoUrl;
    private StoreStatus status;
    private VerificationStatus verificationStatus;
    private double avgRating;
    private int totalRatings;
    private Instant createdAt;
}

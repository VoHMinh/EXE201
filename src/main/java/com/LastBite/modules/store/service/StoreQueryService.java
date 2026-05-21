package com.LastBite.modules.store.service;

import com.LastBite.common.exception.ApiException;
import com.LastBite.common.exception.ErrorCode;
import com.LastBite.modules.store.dto.response.StoreDetailResponse;
import com.LastBite.modules.store.dto.response.StoreResponse;
import com.LastBite.modules.store.entity.Store;
import com.LastBite.modules.store.enums.StoreCategory;
import com.LastBite.modules.store.enums.VerificationStatus;
import com.LastBite.modules.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public store queries for customers — read-only, cached.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreQueryService {

    private final StoreRepository storeRepository;

    /**
     * Search verified stores with optional keyword & category filter (cached 5 min).
     */
    @Cacheable(value = "store-list",
            key = "'search:' + #keyword + ':' + #category + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<StoreResponse> searchStores(String keyword, StoreCategory category, Pageable pageable) {
        return storeRepository
                .searchStores(VerificationStatus.VERIFIED, keyword, category, pageable)
                .map(this::toResponse);
    }

    /**
     * Get store detail by slug (cached 15 min).
     */
    @Cacheable(value = "store-by-slug", key = "#slug")
    public StoreDetailResponse getStoreBySlug(String slug) {
        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(ErrorCode.STORE_NOT_FOUND));

        // Only show verified stores to public
        if (store.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new ApiException(ErrorCode.STORE_NOT_FOUND, "Cửa hàng chưa được xác minh");
        }

        return StoreService.toDetailResponse(store);
    }

    private StoreResponse toResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .slug(store.getSlug())
                .description(store.getDescription())
                .category(store.getCategory())
                .address(store.getAddress())
                .district(store.getDistrict())
                .city(store.getCity())
                .lat(store.getLat())
                .lng(store.getLng())
                .coverImageUrl(store.getCoverImageUrl())
                .logoUrl(store.getLogoUrl())
                .status(store.getStatus())
                .verificationStatus(store.getVerificationStatus())
                .avgRating(store.getAvgRating())
                .totalRatings(store.getTotalRatings())
                .createdAt(store.getCreatedAt())
                .build();
    }
}

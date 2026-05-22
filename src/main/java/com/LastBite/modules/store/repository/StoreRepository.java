package com.LastBite.modules.store.repository;

import com.LastBite.modules.store.entity.Store;
import com.LastBite.modules.store.enums.StoreCategory;
import com.LastBite.modules.store.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    /** Find by owner — with schedules (prevent N+1). */
    @EntityGraph(value = "Store.withSchedules")
    Optional<Store> findByOwnerId(UUID ownerId);

    /** Find by slug — with schedules (prevent N+1). */
    @EntityGraph(value = "Store.withSchedules")
    Optional<Store> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByOwnerId(UUID ownerId);

    /** Search stores: only VERIFIED + ACTIVE, with optional filters. */
    @Query("""
        SELECT s FROM Store s
        WHERE s.verificationStatus = :verification
          AND s.status = com.LastBite.modules.store.enums.StoreStatus.ACTIVE
          AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:category IS NULL OR s.category = :category)
          AND (:city IS NULL OR LOWER(s.city) = LOWER(:city))
          AND (:district IS NULL OR LOWER(s.district) = LOWER(:district))
        ORDER BY s.avgRating DESC, s.totalRatings DESC
    """)
    Page<Store> searchStores(VerificationStatus verification,
                             String keyword,
                             StoreCategory category,
                             String city,
                             String district,
                             Pageable pageable);
}

package com.LastBite.modules.bag.service;

import com.LastBite.common.exception.ApiException;
import com.LastBite.modules.auth.repository.UserRepository;
import com.LastBite.modules.bag.dto.request.CreateSurpriseBagRequest;
import com.LastBite.modules.bag.entity.BagPriceTier;
import com.LastBite.modules.bag.entity.SurpriseBag;
import com.LastBite.modules.bag.enums.BagSize;
import com.LastBite.modules.bag.enums.BagType;
import com.LastBite.modules.bag.repository.BagDailyStockRepository;
import com.LastBite.modules.bag.repository.BagPriceTierRepository;
import com.LastBite.modules.bag.repository.StockAuditLogRepository;
import com.LastBite.modules.bag.repository.SurpriseBagRepository;
import com.LastBite.modules.store.entity.Store;
import com.LastBite.modules.store.enums.StoreCategory;
import com.LastBite.modules.store.enums.StoreStatus;
import com.LastBite.modules.store.enums.VerificationStatus;
import com.LastBite.modules.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SurpriseBagServiceTest {

    private final SurpriseBagRepository bagRepository = mock(SurpriseBagRepository.class);
    private final BagDailyStockRepository stockRepository = mock(BagDailyStockRepository.class);
    private final BagPriceTierRepository priceTierRepository = mock(BagPriceTierRepository.class);
    private final StockAuditLogRepository auditLogRepository = mock(StockAuditLogRepository.class);
    private final StoreRepository storeRepository = mock(StoreRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-05-25T00:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final BagPricingService pricingService = new BagPricingService(clock);

    private SurpriseBagService service;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        service = new SurpriseBagService(bagRepository, stockRepository, priceTierRepository,
                auditLogRepository, storeRepository, userRepository, pricingService, clock);
        ownerId = UUID.randomUUID();

        Store store = Store.builder()
                .name("Tiem banh Test")
                .slug("tiem-banh-test")
                .category(StoreCategory.BAKERY)
                .address("Quan 1")
                .status(StoreStatus.ACTIVE)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        ReflectionTestUtils.setField(store, "id", UUID.randomUUID());
        when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(store));
        when(priceTierRepository.findByCategoryAndBagSizeAndActiveTrue(StoreCategory.BAKERY, BagSize.STANDARD))
                .thenReturn(Optional.of(priceTier(StoreCategory.BAKERY, BagSize.STANDARD)));
        when(bagRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createSnapshotsPriceFromPlatformTier() {
        assertDoesNotThrow(() -> service.create(ownerId, validRequest()));

        verify(bagRepository).save(argThat(argument -> {
            SurpriseBag bag = (SurpriseBag) argument;
            assertEquals(StoreCategory.BAKERY, bag.getCategory());
            assertEquals(BagSize.STANDARD, bag.getBagSize());
            assertEquals(BigDecimal.valueOf(100000), bag.getMinimumValue());
            assertEquals(BigDecimal.valueOf(39000), bag.getBaseSalePrice());
            assertEquals(BigDecimal.valueOf(35000), bag.getDynamicMinPrice());
            assertEquals(BigDecimal.valueOf(45000), bag.getDynamicMaxPrice());
            return true;
        }));
    }

    @Test
    void createRejectsMissingPlatformTier() {
        CreateSurpriseBagRequest request = validRequest();
        request.setBagSize(BagSize.LARGE);
        when(priceTierRepository.findByCategoryAndBagSizeAndActiveTrue(StoreCategory.BAKERY, BagSize.LARGE))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.create(ownerId, request));

        verify(bagRepository, never()).save(any());
    }

    @Test
    void createRejectsPickupWindowShorterThanThirtyMinutes() {
        CreateSurpriseBagRequest request = validRequest();
        request.setPickupStartTime(LocalTime.of(20, 0));
        request.setPickupEndTime(LocalTime.of(20, 20));

        assertThrows(ApiException.class, () -> service.create(ownerId, request));

        verify(bagRepository, never()).save(any());
    }

    @Test
    void createAcceptsValidBagRules() {
        assertDoesNotThrow(() -> service.create(ownerId, validRequest()));

        verify(bagRepository).save(any());
    }

    private CreateSurpriseBagRequest validRequest() {
        CreateSurpriseBagRequest request = new CreateSurpriseBagRequest();
        request.setName("Tui banh cuoi ngay");
        request.setBagType(BagType.BREAD);
        request.setCategory(StoreCategory.BAKERY);
        request.setBagSize(BagSize.STANDARD);
        request.setPickupStartTime(LocalTime.of(20, 0));
        request.setPickupEndTime(LocalTime.of(21, 0));
        request.setAvailableDays(Set.of(1, 2, 3, 4, 5));
        return request;
    }

    private BagPriceTier priceTier(StoreCategory category, BagSize bagSize) {
        return BagPriceTier.builder()
                .category(category)
                .bagSize(bagSize)
                .minimumValue(BigDecimal.valueOf(100000))
                .baseSalePrice(BigDecimal.valueOf(39000))
                .dynamicMinPrice(BigDecimal.valueOf(35000))
                .dynamicMaxPrice(BigDecimal.valueOf(45000))
                .platformFee(BigDecimal.valueOf(4000))
                .active(true)
                .build();
    }
}

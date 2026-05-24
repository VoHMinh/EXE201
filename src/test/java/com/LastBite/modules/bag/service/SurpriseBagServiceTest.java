package com.LastBite.modules.bag.service;

import com.LastBite.common.exception.ApiException;
import com.LastBite.modules.bag.dto.request.CreateSurpriseBagRequest;
import com.LastBite.modules.bag.enums.BagType;
import com.LastBite.modules.bag.repository.BagDailyStockRepository;
import com.LastBite.modules.bag.repository.StockAuditLogRepository;
import com.LastBite.modules.bag.repository.SurpriseBagRepository;
import com.LastBite.modules.store.entity.Store;
import com.LastBite.modules.store.enums.StoreCategory;
import com.LastBite.modules.store.enums.StoreStatus;
import com.LastBite.modules.store.enums.VerificationStatus;
import com.LastBite.modules.store.repository.StoreRepository;
import com.LastBite.modules.auth.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SurpriseBagServiceTest {

    private final SurpriseBagRepository bagRepository = mock(SurpriseBagRepository.class);
    private final BagDailyStockRepository stockRepository = mock(BagDailyStockRepository.class);
    private final StockAuditLogRepository auditLogRepository = mock(StockAuditLogRepository.class);
    private final StoreRepository storeRepository = mock(StoreRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-05-25T00:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private SurpriseBagService service;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        service = new SurpriseBagService(bagRepository, stockRepository, auditLogRepository,
                storeRepository, userRepository, clock);
        ownerId = UUID.randomUUID();

        Store store = Store.builder()
                .name("Tiệm bánh Test")
                .slug("tiem-banh-test")
                .category(StoreCategory.BAKERY)
                .address("Quận 1")
                .status(StoreStatus.ACTIVE)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        ReflectionTestUtils.setField(store, "id", UUID.randomUUID());
        when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(store));
        when(bagRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createRejectsSalePriceAboveHalfEstimatedValue() {
        CreateSurpriseBagRequest request = validRequest();
        request.setEstimatedValue(BigDecimal.valueOf(100000));
        request.setSalePrice(BigDecimal.valueOf(60000));

        assertThrows(ApiException.class, () -> service.create(ownerId, request));

        verify(bagRepository, never()).save(any());
    }

    @Test
    void createRejectsSalePriceBelowTenPercentEstimatedValue() {
        CreateSurpriseBagRequest request = validRequest();
        request.setEstimatedValue(BigDecimal.valueOf(300000));
        request.setSalePrice(BigDecimal.valueOf(20000));

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
        request.setName("Túi bánh cuối ngày");
        request.setBagType(BagType.BREAD);
        request.setEstimatedValue(BigDecimal.valueOf(100000));
        request.setSalePrice(BigDecimal.valueOf(40000));
        request.setPickupStartTime(LocalTime.of(20, 0));
        request.setPickupEndTime(LocalTime.of(21, 0));
        request.setAvailableDays(Set.of(1, 2, 3, 4, 5));
        return request;
    }
}

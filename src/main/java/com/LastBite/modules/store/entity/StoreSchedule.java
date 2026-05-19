package com.LastBite.modules.store.entity;

import com.LastBite.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

/**
 * Weekly schedule for a store (one row per day of week).
 */
@Entity
@Table(name = "store_schedules",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_store_day",
                columnNames = {"store_id", "day_of_week"}))
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** 0 = Sunday, 1 = Monday, ..., 6 = Saturday */
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private boolean isOpen = true;
}

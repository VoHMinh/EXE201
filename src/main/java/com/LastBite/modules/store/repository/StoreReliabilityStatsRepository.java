package com.LastBite.modules.store.repository;

import com.LastBite.modules.store.entity.StoreReliabilityStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreReliabilityStatsRepository extends JpaRepository<StoreReliabilityStats, UUID> {
}

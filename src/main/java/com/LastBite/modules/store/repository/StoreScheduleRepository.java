package com.LastBite.modules.store.repository;

import com.LastBite.modules.store.entity.StoreSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, UUID> {
}

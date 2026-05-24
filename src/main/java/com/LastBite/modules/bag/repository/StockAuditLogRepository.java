package com.LastBite.modules.bag.repository;

import com.LastBite.modules.bag.entity.StockAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockAuditLogRepository extends JpaRepository<StockAuditLog, UUID> {

    @EntityGraph(attributePaths = {"dailyStock", "actor"})
    Page<StockAuditLog> findByBagIdOrderByCreatedAtDesc(UUID bagId, Pageable pageable);
}

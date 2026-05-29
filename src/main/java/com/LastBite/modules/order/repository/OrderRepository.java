package com.LastBite.modules.order.repository;

import com.LastBite.modules.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByUser_IdAndIdempotencyKey(UUID userId, String idempotencyKey);
}

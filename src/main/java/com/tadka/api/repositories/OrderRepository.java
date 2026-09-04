package com.tadka.api.repositories;

import com.tadka.api.domain.orders.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerId(UUID customerId);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findFirstPageByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND (o.createdAt < :createdAt OR (o.createdAt = :createdAt AND o.id < :id)) ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findNextPageByCustomerId(
            @Param("customerId") UUID customerId,
            @Param("createdAt") Instant createdAt,
            @Param("id") UUID id,
            Pageable pageable
    );
}

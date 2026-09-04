package com.tadka.api.repositories;

import com.tadka.api.domain.delivery.DeliveryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, UUID> {
    List<DeliveryAgent> findByIsAvailableTrue();
}

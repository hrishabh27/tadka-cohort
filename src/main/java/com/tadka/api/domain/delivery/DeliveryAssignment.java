package com.tadka.api.domain.delivery;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_assignments", schema = "delivery")
public class DeliveryAssignment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID agentId;

    @Column(nullable = false)
    private Instant assignedAt;

    private Instant deliveredAt;

    protected DeliveryAssignment() {}

    public DeliveryAssignment(UUID orderId, UUID agentId) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.agentId = agentId;
        this.assignedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}

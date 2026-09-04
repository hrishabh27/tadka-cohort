package com.tadka.api.domain.orders;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys", schema = "orders", indexes = {
    @Index(name = "idx_idempotency_keys_key", columnList = "key", unique = true)
})
public class IdempotencyKey {

    @Id
    private UUID id;

    @Column(name = "key", nullable = false, unique = true)
    private String key;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyKey() {}

    public IdempotencyKey(String key, UUID orderId, int statusCode, String responseBody) {
        this.id = UUID.randomUUID();
        this.key = key;
        this.orderId = orderId;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

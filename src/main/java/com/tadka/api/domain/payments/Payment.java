package com.tadka.api.domain.payments;

import com.tadka.api.domain.valueobjects.Money;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payments")
public class Payment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money amount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private Instant processedAt;

    protected Payment() {}

    public Payment(UUID orderId, Money amount, String status, String method) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.processedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Money getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getMethod() {
        return method;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}

package com.tadka.api.domain.orders;

import com.tadka.api.domain.valueobjects.Address;
import com.tadka.api.domain.valueobjects.Money;
import com.tadka.api.exceptions.DomainException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", schema = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Version
    private Long version;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency"))
    })
    private Money subtotal;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "delivery_fee_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "delivery_fee_currency"))
    })
    private Money deliveryFee;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "tax_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "tax_currency"))
    })
    private Money tax;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
    })
    private Money total;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "delivery_line1")),
        @AttributeOverride(name = "line2", column = @Column(name = "delivery_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "delivery_city")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "delivery_postal_code"))
    })
    private Address deliveryAddress;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Order() {}

    public Order(UUID customerId, UUID restaurantId, Address deliveryAddress, List<OrderItem> items, Money deliveryFee, Money tax) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.status = OrderStatus.Created;
        this.deliveryFee = deliveryFee != null ? deliveryFee : Money.inr(40);
        this.tax = tax != null ? tax : Money.inr(0);
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        calculateTotals();
    }

    public void calculateTotals() {
        Money sub = Money.inr(0);
        for (OrderItem item : items) {
            sub = sub.add(item.getTotalPrice());
        }
        this.subtotal = sub;
        this.total = this.subtotal.add(this.deliveryFee).add(this.tax);
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void transitionTo(OrderStatus newStatus) {
        if (this.status == newStatus) {
            throw new DomainException("Cannot transition order from " + this.status + " to " + newStatus + ": order is already " + newStatus);
        }
        boolean allowed = switch (this.status) {
            case Created -> newStatus == OrderStatus.Confirmed || newStatus == OrderStatus.Cancelled;
            case Confirmed -> newStatus == OrderStatus.Preparing || newStatus == OrderStatus.Cancelled;
            case Preparing -> newStatus == OrderStatus.OutForDelivery || newStatus == OrderStatus.Cancelled;
            case OutForDelivery -> newStatus == OrderStatus.Delivered || newStatus == OrderStatus.Cancelled;
            case Delivered, Cancelled -> false;
        };

        if (!allowed) {
            throw new DomainException("Illegal status transition from " + this.status + " to " + newStatus);
        }

        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public Long getVersion() {
        return version;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Money getDeliveryFee() {
        return deliveryFee;
    }

    public Money getTax() {
        return tax;
    }

    public Money getTotal() {
        return total;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

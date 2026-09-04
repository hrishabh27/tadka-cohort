package com.tadka.api.domain.orders;

import com.tadka.api.domain.valueobjects.Money;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "order_items", schema = "orders")
public class OrderItem {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID menuItemId;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_price_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "total_price_currency"))
    })
    private Money totalPrice;

    protected OrderItem() {}

    public OrderItem(UUID menuItemId, String name, Money unitPrice, int quantity) {
        this.id = UUID.randomUUID();
        this.menuItemId = menuItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = unitPrice != null ? unitPrice.multiply(quantity) : Money.inr(0);
    }

    public UUID getId() {
        return id;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public String getName() {
        return name;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getTotalPrice() {
        return totalPrice;
    }
}

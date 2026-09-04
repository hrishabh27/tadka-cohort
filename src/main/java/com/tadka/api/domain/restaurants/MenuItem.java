package com.tadka.api.domain.restaurants;

import com.tadka.api.domain.valueobjects.Money;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "menu_items", schema = "restaurants")
public class MenuItem {

    @Id
    private UUID id;

    @Column(name = "restaurant_id", insertable = false, updatable = false)
    private UUID restaurantId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private Money price;

    @Column(nullable = false)
    private boolean isAvailable;

    protected MenuItem() {}

    public MenuItem(UUID restaurantId, String name, String description, Money price, boolean isAvailable) {
        this.id = UUID.randomUUID();
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}

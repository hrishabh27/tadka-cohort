package com.tadka.api.domain.delivery;

import com.tadka.api.domain.valueobjects.GeoLocation;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "delivery_agents", schema = "delivery")
public class DeliveryAgent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "current_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "current_longitude"))
    })
    private GeoLocation currentLocation;

    @Column(nullable = false)
    private boolean isAvailable;

    protected DeliveryAgent() {}

    public DeliveryAgent(String name, String phone, GeoLocation currentLocation, boolean isAvailable) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.phone = phone;
        this.currentLocation = currentLocation;
        this.isAvailable = isAvailable;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public GeoLocation getCurrentLocation() {
        return currentLocation;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}

package com.tadka.api.domain.restaurants;

import com.tadka.api.domain.valueobjects.Address;
import com.tadka.api.domain.valueobjects.GeoLocation;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants", schema = "restaurants")
public class Restaurant {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "address_line1")),
        @AttributeOverride(name = "line2", column = @Column(name = "address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "address_city")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "address_postal_code"))
    })
    private Address address;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "longitude"))
    })
    private GeoLocation location;

    @Column(nullable = false)
    private boolean isActive;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private List<MenuItem> menuItems = new ArrayList<>();

    protected Restaurant() {}

    public Restaurant(String name, Address address, GeoLocation location, boolean isActive) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.address = address;
        this.location = location;
        this.isActive = isActive;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void addMenuItem(MenuItem item) {
        this.menuItems.add(item);
    }
}

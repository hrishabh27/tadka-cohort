package com.tadka.api.domain.users;

import com.tadka.api.domain.valueobjects.Address;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_addresses", schema = "users")
public class UserAddress {

    @Id
    private UUID id;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private String label;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "line1")),
        @AttributeOverride(name = "line2", column = @Column(name = "line2")),
        @AttributeOverride(name = "city", column = @Column(name = "city")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "postal_code"))
    })
    private Address address;

    @Column(nullable = false)
    private boolean isDefault;

    protected UserAddress() {}

    public UserAddress(UUID userId, String label, Address address, boolean isDefault) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.label = label;
        this.address = address;
        this.isDefault = isDefault;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLabel() {
        return label;
    }

    public Address getAddress() {
        return address;
    }

    public boolean isDefault() {
        return isDefault;
    }
}

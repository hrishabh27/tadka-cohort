package com.tadka.api.domain.valueobjects;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Address {
    private String line1;
    private String line2;
    private String city;
    private String postalCode;

    protected Address() {}

    public Address(String line1, String line2, String city, String postalCode) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.postalCode = postalCode;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(line1, address.line1) &&
               Objects.equals(line2, address.line2) &&
               Objects.equals(city, address.city) &&
               Objects.equals(postalCode, address.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line1, line2, city, postalCode);
    }
}

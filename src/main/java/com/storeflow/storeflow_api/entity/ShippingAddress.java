package com.storeflow.storeflow_api.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.*;

/**
 * ShippingAddress is an embeddable value object for order shipping details.
 * Embedded directly in the Order entity, not stored as a separate table.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {

    @Column(name = "shipping_street", length = 255)
    private String street;

    @Column(name = "shipping_city", length = 100)
    private String city;

    @Column(name = "shipping_state", length = 100)
    private String state;

    @Column(name = "shipping_postal_code", length = 20)
    private String postalCode;

    @Column(name = "shipping_country", length = 100)
    private String country;

    @Override
    public String toString() {
        return String.format("%s, %s, %s %s %s", street, city, state, postalCode, country);
    }
}

package com.hoolistem.hoolistem.model.address;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.ecommerce.Order;
import jakarta.persistence.*;
import lombok.*;

/**
 * Order address - snapshot of delivery address at time of order.
 * 
 * Intentionally denormalized to preserve address even if user
 * updates or deletes the original address.
 */
@Entity
@Table(name = "order_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddress extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_order_addresses_order"))
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_address_id",
                foreignKey = @ForeignKey(name = "fk_order_addresses_user_addr"))
    private UserAddress userAddress;

    @Column(name = "recipient_name", nullable = false, length = 255)
    private String recipientName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    // ========================
    // HELPER METHODS
    // ========================

    /**
     * Creates an order address from a user address
     */
    public static OrderAddress fromUserAddress(UserAddress userAddress, Order order, 
                                                String recipientName, String phone) {
        return OrderAddress.builder()
                .order(order)
                .userAddress(userAddress)
                .recipientName(recipientName)
                .phone(phone)
                .addressLine1(userAddress.getAddressLine1())
                .addressLine2(userAddress.getAddressLine2())
                .city(userAddress.getCity())
                .state(userAddress.getState())
                .postalCode(userAddress.getPostalCode())
                .country(userAddress.getCountry())
                .build();
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(addressLine1);
        if (addressLine2 != null && !addressLine2.isBlank()) {
            sb.append(", ").append(addressLine2);
        }
        sb.append(", ").append(city);
        sb.append(", ").append(state);
        sb.append(" - ").append(postalCode);
        sb.append(", ").append(country);
        return sb.toString();
    }
}

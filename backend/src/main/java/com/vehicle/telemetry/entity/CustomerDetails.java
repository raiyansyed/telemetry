package com.vehicle.telemetry.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * CustomerDetails - Extra profile information for users with the CUSTOMER role.
 *
 * WHY A SEPARATE TABLE?
 * - Same reason as OwnerDetails: customer-specific fields (license number, address) are kept
 *   in their own table instead of cluttering the User table with nullable columns.
 *
 * RELATIONSHIP: One User (with role=CUSTOMER) has exactly one CustomerDetails row.
 * - @OneToOne + @JoinColumn creates a foreign key in customer_details pointing to users.
 * - CascadeType.ALL means saving CustomerDetails also saves the linked User.
 *
 * HOW IT'S USED:
 * - Created during registration (AuthController) when role=CUSTOMER.
 * - Referenced by Vehicle.assignedCustomer to track which customer is driving which vehicle.
 * - Referenced by AssignmentRequest to track which customer is requesting which vehicle.
 */
@Entity
@Table(name = "customer_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetails {

    /** Primary key - auto-generated unique ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Link to the User table - One-to-One relationship.
     * CascadeType.ALL means saving this entity also saves/updates the linked User.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    /** The customer's driving license number (e.g., "TN-01-AB-1234"). */
    private String licenseNumber;

    /** The customer's address (e.g., "T. Nagar, Chennai"). */
    private String address;
}
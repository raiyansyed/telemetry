package com.vehicle.telemetry.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vehicle.telemetry.enums.RentalStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Rental - Represents a rental agreement between an owner and a customer for a vehicle.
 *
 * NOTE: This is a LEGACY entity. The current system primarily uses the "direct assignment" model
 * (Vehicle.assignedCustomer) rather than formal Rental records. However, the DatabaseSeedUtility
 * still creates one sample rental for the "anynomo" test user, and the customer dashboard can
 * display rental information if it exists.
 *
 * A Rental has:
 * - A vehicle being rented
 * - A customer who is renting it
 * - An owner who owns the vehicle
 * - Start and end dates for the rental period
 * - A status (ACTIVE, COMPLETED, or CANCELLED)
 */
@Entity
@Table(name = "rentals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental {

    /** Primary key - auto-generated unique ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The vehicle being rented - Many-to-One (many rentals can reference one vehicle over time) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vehicle vehicle;

    /** The customer who is renting the vehicle */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CustomerDetails customer;

    /** The owner of the vehicle being rented */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OwnerDetails owner;

    /** When the rental period starts */
    private LocalDateTime startDate;

    /** When the rental period ends */
    private LocalDateTime endDate;

    /**
     * Current state of this rental - ACTIVE, COMPLETED, or CANCELLED.
     * @Enumerated(STRING) stores as readable text in the database.
     */
    @Enumerated(EnumType.STRING)
    private RentalStatus status;
}
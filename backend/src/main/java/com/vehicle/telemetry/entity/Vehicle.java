package com.vehicle.telemetry.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vehicle.telemetry.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Vehicle - Represents a vehicle in the fleet (maps to the "vehicles" database table).
 *
 * This is the central entity in the system. Each vehicle:
 * - Belongs to one Owner (via the "owner" field - Many-to-One relationship).
 * - May be assigned to one Customer (via "assignedCustomer" - Many-to-One, nullable).
 * - Has many VehicleReading records (telemetry snapshots generated every 3 seconds).
 * - Has many FleetActivity records (alerts/logs about this vehicle).
 *
 * RELATIONSHIPS EXPLAINED:
 * - @ManyToOne = "many vehicles can belong to one owner" (foreign key in vehicles table).
 * - FetchType.EAGER = load the related entity immediately (not lazily on first access).
 * - @JsonIgnoreProperties = prevents infinite JSON loops when serializing
 *   (e.g., Vehicle -> Owner -> User, but we don't want User to loop back to details).
 *
 * VEHICLE LIFECYCLE:
 * 1. Owner adds vehicle via POST /api/owner/vehicles -> status = ACTIVE
 * 2. Owner assigns to customer OR customer requests assignment -> status = RENTED
 * 3. Customer releases vehicle -> status = ACTIVE again
 * 4. Owner deletes vehicle -> removed from database
 */
@Entity
@Table(name = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    /** Primary key - auto-generated unique ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Vehicle Identification Number - a unique identifier like a license plate (e.g., "TN04-FE-0001") */
    private String vin;

    /** Vehicle manufacturer (e.g., "Toyota", "Honda", "BMW") */
    private String make;

    /** Vehicle model name (e.g., "Camry", "Civic", "X5") */
    private String model;

    /** Manufacturing year (e.g., 2024) */
    private int year;

    /**
     * Current status of the vehicle.
     * ACTIVE = available for assignment, RENTED = currently assigned to a customer.
     * @Enumerated(STRING) stores as text in the database for readability.
     */
    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    /**
     * The owner who owns this vehicle - Many-to-One relationship.
     * "Many vehicles can belong to one owner."
     * FetchType.EAGER means the owner data is loaded immediately with the vehicle.
     * @JsonIgnoreProperties prevents infinite JSON serialization loops.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OwnerDetails owner;

    /**
     * The customer currently assigned to drive this vehicle (nullable).
     * When null = vehicle is unassigned (ACTIVE status).
     * When set = vehicle is being driven by this customer (RENTED status).
     * @ManyToOne because one customer could theoretically have multiple vehicles (future feature).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_customer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CustomerDetails assignedCustomer;

    /**
     * The city where this vehicle is located (e.g., "Chennai").
     * Used to match vehicles with customers in the same city.
     * Inherited from the owner's location when the vehicle is created.
     */
    private String location;
}
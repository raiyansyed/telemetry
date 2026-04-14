package com.vehicle.telemetry.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * AssignmentRequest - A request from a customer to be assigned to a specific vehicle.
 *
 * WORKFLOW:
 * 1. Customer sees available vehicles in their city on the customer dashboard.
 * 2. Customer clicks "Request Assignment" -> creates a new AssignmentRequest with status="PENDING".
 * 3. The owner sees the pending request in their "Pending Assignment Requests" panel
 *    (and also in the combined alerts panel as an INFO-type alert).
 * 4. Owner can:
 *    - APPROVE: assigns the vehicle to the customer (Vehicle.assignedCustomer = customer, status=RENTED).
 *    - REJECT: denies the request (status="REJECTED", vehicle stays unassigned).
 *
 * STATUS VALUES:
 * - "PENDING": waiting for owner decision
 * - "APPROVED": owner approved, vehicle is now assigned to the customer
 * - "REJECTED": owner denied the request
 *
 * CONSTRAINTS:
 * - A customer can only have one pending request per vehicle (checked in CustomerController).
 * - A customer cannot request a vehicle if they already have one assigned (must release first).
 * - Only vehicles in the customer's city are shown as requestable.
 */
@Entity
@Table(name = "assignment_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {

    /** Primary key - auto-generated unique ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The customer making the request */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CustomerDetails customer;

    /** The vehicle being requested */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vehicle vehicle;

    /** The owner of the vehicle (for easy querying of requests by owner) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OwnerDetails owner;

    /** Current status: "PENDING", "APPROVED", or "REJECTED" */
    private String status;

    /** When the request was created */
    private LocalDateTime createdAt;

    /** When the owner approved or rejected the request (null while PENDING) */
    private LocalDateTime resolvedAt;
}
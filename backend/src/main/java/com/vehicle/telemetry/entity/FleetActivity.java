package com.vehicle.telemetry.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * FleetActivity - A log entry that records important events for a fleet owner.
 *
 * Think of this as an "event log" or "notification history" for the owner. Every time something
 * noteworthy happens (vehicle added, customer assigned, speed alert triggered), a new FleetActivity
 * row is created. The owner sees these in the "Fleet Alerts" panel on their dashboard.
 *
 * EXAMPLES OF LOGGED EVENTS:
 * - "Vehicle added: Toyota Camry (TN04-FE-0001)" [type=INFO]
 * - "WARNING on TN04-FE-0001: speed 85.0 km/h, engine 95.0 C" [type=WARNING]
 * - "CRITICAL on TN04-FE-0001: speed 115.0 km/h, engine 112.0 C" [type=CRITICAL]
 * - "Assigned to @customer" [type=INFO]
 * - "Vehicle released by customer @customer" [type=INFO]
 *
 * KEY FIELDS:
 * - message: human-readable description of what happened
 * - alertType: "INFO", "WARNING", or "CRITICAL"
 * - read: whether the owner has marked this alert as read (for UI visual state)
 * - vin: the vehicle's VIN for quick reference without joining tables
 */
@Entity
@Table(name = "fleet_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetActivity {

    /** Primary key - auto-generated unique ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** When this event occurred */
    private LocalDateTime createdAt;

    /** The owner this event belongs to - Many-to-One relationship */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OwnerDetails owner;

    /** The vehicle involved in this event (nullable - some events don't have a vehicle) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vehicle vehicle;

    /** The vehicle's VIN - stored directly for quick access without database joins */
    private String vin;

    /** Human-readable description (e.g., "Vehicle added: Toyota Camry (TN04-FE-0001)") */
    private String message;

    /** Alert severity: "INFO", "WARNING", or "CRITICAL" */
    private String alertType;

    /** Whether the owner has marked this alert as read in the UI */
    private boolean read;
}
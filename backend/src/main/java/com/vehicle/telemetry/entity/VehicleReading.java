package com.vehicle.telemetry.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vehicle.telemetry.enums.AlertLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * VehicleReading - A single telemetry snapshot for a vehicle at a moment in time.
 *
 * Every 3 seconds, the VehicleJourneySimulator creates a new VehicleReading for each vehicle.
 * This is the raw telemetry data that powers all the charts, gauges, and alerts in the frontend.
 *
 * WHAT'S IN EACH READING:
 * - speed: current speed in km/h (0 if vehicle is not assigned/rented)
 * - temperature: engine temperature in degrees Celsius
 * - latitude/longitude: GPS coordinates (simulated, centered around Chennai)
 * - alertLevel: NONE, WARNING, or CRITICAL based on speed/temperature thresholds
 * - timestamp: when this reading was captured
 *
 * RELATIONSHIP:
 * - @ManyToOne to Vehicle: "many readings belong to one vehicle"
 * - Each vehicle accumulates thousands of readings over time
 *
 * DATA FLOW:
 *   VehicleJourneySimulator -> creates VehicleReading -> saves to database
 *   Frontend polls API -> gets latest readings -> displays on gauges and charts
 */
@Entity
@Table(name = "vehicle_readings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleReading {

    /** Primary key - auto-generated unique ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** When this reading was captured (e.g., "2024-01-15T14:30:00") */
    private LocalDateTime timestamp;

    /** Current speed in km/h (0.0 to ~200.0). 0 for unassigned vehicles. */
    private Double speed;

    /** Engine temperature in degrees Celsius (~60-120 range). Higher speed = higher temp. */
    private Double temperature;

    /** GPS latitude coordinate (simulated, near Chennai: ~13.08) */
    private Double latitude;

    /** GPS longitude coordinate (simulated, near Chennai: ~80.27) */
    private Double longitude;

    /**
     * Alert severity based on speed and temperature thresholds.
     * NONE = safe, WARNING = approaching limits, CRITICAL = dangerous.
     * Evaluated by AlertService every time a reading is generated.
     */
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;

    /**
     * The vehicle this reading belongs to - Many-to-One relationship.
     * @JsonIgnoreProperties prevents infinite JSON loops when the API returns readings.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vehicle vehicle;
}
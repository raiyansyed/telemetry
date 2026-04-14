package com.vehicle.telemetry.enums;

/**
 * VehicleStatus — Represents the current state of a vehicle in the fleet.
 *
 * Each vehicle always has exactly one of these statuses. The status determines
 * how the vehicle behaves in the simulator and what actions can be taken on it.
 *
 * STATUS LIFECYCLE:
 *   1. When a vehicle is first added → ACTIVE (available for assignment)
 *   2. When assigned to a customer → RENTED (simulator generates live telemetry)
 *   3. When the customer releases it → back to ACTIVE
 *   4. INACTIVE and MAINTENANCE are reserved for future use (not currently used in the app)
 */
public enum VehicleStatus {
    ACTIVE,         // Vehicle is available and not assigned to anyone. Simulator shows 0 speed.
    INACTIVE,       // Vehicle is deactivated (reserved for future use).
    MAINTENANCE,    // Vehicle is under maintenance (reserved for future use).
    RENTED          // Vehicle is currently assigned to a customer. Simulator generates live telemetry.
}

package com.vehicle.telemetry.enums;

/**
 * AlertLevel — Represents the severity of a telemetry alert for a vehicle.
 *
 * HOW ALERTS ARE DETERMINED (see AlertService.java):
 * - NONE:     Speed < 70 km/h AND temperature < 90°C → everything is normal.
 * - WARNING:  Speed >= 70 km/h OR temperature >= 90°C → approaching danger limits.
 * - CRITICAL: Speed >= 110 km/h OR temperature >= 110°C → dangerously high values.
 *
 * WHERE IT'S USED:
 * - Stored in each VehicleReading record so every telemetry snapshot has an alert level.
 * - The VehicleJourneySimulator evaluates alert levels every 3 seconds.
 * - The frontend displays color-coded banners: green (NONE), yellow (WARNING), red (CRITICAL).
 * - When an alert level changes to WARNING or CRITICAL, a FleetActivity log entry is created
 *   so the owner sees it in their alerts panel.
 */
public enum AlertLevel {
    NONE,       // Normal operation — speed and temperature within safe limits
    WARNING,    // Approaching limits — speed >= 70 km/h or temp >= 90°C
    CRITICAL    // Dangerous levels — speed >= 110 km/h or temp >= 110°C
}

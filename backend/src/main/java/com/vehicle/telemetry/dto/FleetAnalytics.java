package com.vehicle.telemetry.dto;

import lombok.Builder;
import lombok.Data;

/**
 * FleetAnalytics - Summary statistics for an owner's entire fleet, displayed on the owner dashboard.
 *
 * These are the 4 big numbers shown at the top of the Fleet Owner Dashboard:
 * - averageSpeed: the average speed across all vehicles (from recent readings)
 * - averageTemperature: the average engine temperature across all vehicles
 * - vehicleCount: total number of vehicles the owner has
 * - activeRentals: how many vehicles are currently assigned to customers
 *
 * Computed by VehicleService.getFleetAnalytics() using database queries.
 * USED BY: GET /api/owner/analytics (OwnerController)
 */
@Data
@Builder
public class FleetAnalytics {
    /** Average speed (km/h) across all of the owner's vehicles */
    private Double averageSpeed;
    /** Average engine temperature (C) across all vehicles */
    private Double averageTemperature;
    /** Total number of vehicles in the owner's fleet */
    private long vehicleCount;
    /** Number of vehicles currently assigned to customers (status = RENTED) */
    private long activeRentals;
}
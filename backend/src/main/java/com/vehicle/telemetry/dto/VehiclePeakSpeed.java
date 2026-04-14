package com.vehicle.telemetry.dto;

import lombok.Builder;
import lombok.Data;

/**
 * VehiclePeakSpeed - Represents the highest speed recorded today for a single vehicle.
 *
 * The owner dashboard shows a "Peak Speed Leaderboard" - the top 5 fastest vehicles today.
 * Each entry shows the vehicle's VIN, make/model, peak speed, engine temp at that moment,
 * GPS location, and which customer was driving.
 *
 * Computed by VehicleService.getTopVehiclePeakSpeeds() which:
 * 1. Gets all readings since midnight today for the owner's vehicles.
 * 2. Groups by vehicle and finds the reading with the highest speed per vehicle.
 * 3. Sorts by speed descending and takes the top 5.
 *
 * USED BY: GET /api/owner/peak-speeds (OwnerController)
 */
@Data
@Builder
public class VehiclePeakSpeed {
    /** The vehicle's database ID */
    private Long vehicleId;
    /** Vehicle Identification Number (e.g., "TN04-FE-0001") */
    private String vin;
    /** Vehicle manufacturer (e.g., "Toyota") */
    private String make;
    /** Vehicle model (e.g., "Camry") */
    private String model;
    /** The highest speed recorded today in km/h */
    private Double peakSpeed;
    /** Engine temperature at the moment of peak speed */
    private Double avgTemperature;
    /** GPS latitude at the moment of peak speed */
    private Double latitude;
    /** GPS longitude at the moment of peak speed */
    private Double longitude;
    /** When the peak speed was recorded (ISO datetime string) */
    private String timestamp;
    /** Username of the customer who was driving at that moment (null if unassigned) */
    private String assignedDriverUsername;
}
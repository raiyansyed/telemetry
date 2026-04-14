package com.vehicle.telemetry.dto;

import lombok.Data;

/**
 * VehicleControlRequest - The data sent when a driver/customer controls a vehicle.
 *
 * The customer dashboard has GAS and BRAKE buttons (or keyboard controls W/S/arrows).
 * When pressed, the frontend sends a control command to the backend, which updates
 * the VehicleControlService state. The VehicleJourneySimulator then uses this state
 * to calculate the vehicle's next speed instead of generating random values.
 *
 * ACTIONS:
 * - "ACCELERATE": increase speed (multiplied by throttle value)
 * - "BRAKE": decrease speed (multiplied by throttle value)
 * - "IDLE": natural deceleration (friction slowdown)
 *
 * EXAMPLE JSON:
 *   { "action": "ACCELERATE", "throttle": 0.7 }
 *
 * USED BY: POST /api/driver/vehicle/{id}/control (DriverController.controlVehicle())
 */
@Data
public class VehicleControlRequest {
    /** The control action: "ACCELERATE", "BRAKE", or "IDLE" */
    private String action;
    /** Throttle intensity from 0.0 (minimum) to 1.0 (maximum) */
    private Double throttle;
}
package com.vehicle.telemetry.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VehicleControlService - Manages manual driving controls for vehicles.
 *
 * When a customer presses the GAS/BRAKE buttons on the dashboard, those commands
 * come through here. The VehicleJourneySimulator checks this service every 3 seconds
 * to decide whether to use manual controls or auto-generated random values.
 *
 * CONTROL FLOW:
 *   1. Customer presses GAS button -> frontend sends POST /api/driver/vehicle/{id}/control
 *   2. DriverController calls setThrottle(vehicleId, "ACCELERATE", 0.7)
 *   3. This service stores the throttle state in memory (ConcurrentHashMap)
 *   4. Next simulator tick (3 seconds later):
 *      - Checks isManualControl(vehicleId) -> true
 *      - Calls computeNextSpeed() which uses the stored throttle value
 *      - Speed changes based on the throttle: +5 km/h per tick at full throttle
 *   5. Customer releases button -> frontend sends "IDLE" action
 *   6. Speed naturally decreases by 1 km/h per tick (friction)
 *
 * WHY ConcurrentHashMap?
 * - Multiple threads access this data: the HTTP request thread (setting throttle)
 *   and the simulator thread (reading throttle). ConcurrentHashMap is thread-safe.
 *
 * NOTE: This data is in-memory only. If the server restarts, all manual control state is lost.
 */
@Service
public class VehicleControlService {

    /** vehicleId -> current throttle value. Range: -1.0 (full brake) to 1.0 (full throttle). 0 = idle. */
    private final Map<Long, Double> throttleMap = new ConcurrentHashMap<>();

    /** vehicleId -> whether manual control is active (true = manual, false/absent = auto) */
    private final Map<Long, Boolean> manualControlMap = new ConcurrentHashMap<>();

    /** vehicleId -> current manually-controlled speed (used for reference, not primary logic) */
    private final Map<Long, Double> currentSpeedMap = new ConcurrentHashMap<>();

    /**
     * Set the throttle for a vehicle based on a control action from the frontend.
     *
     * @param vehicleId   The vehicle being controlled.
     * @param action      "ACCELERATE", "BRAKE", or "IDLE".
     * @param throttleValue The intensity (0.0 to 1.0). Higher = stronger acceleration/braking.
     */
    public void setThrottle(Long vehicleId, String action, Double throttleValue) {
        // Activate manual control mode for this vehicle
        manualControlMap.put(vehicleId, true);

        switch (action.toUpperCase()) {
            case "ACCELERATE":
                // Positive throttle = accelerating. Clamped to max 1.0.
                throttleMap.put(vehicleId, Math.min(1.0, Math.abs(throttleValue)));
                break;
            case "BRAKE":
                // Negative throttle = braking. Clamped to min -1.0.
                throttleMap.put(vehicleId, -Math.min(1.0, Math.abs(throttleValue)));
                break;
            case "IDLE":
            default:
                // Zero throttle = coasting (natural deceleration from friction).
                throttleMap.put(vehicleId, 0.0);
                break;
        }
    }

    /** Check if a vehicle is currently under manual control (vs. auto-simulated). */
    public boolean isManualControl(Long vehicleId) {
        return manualControlMap.getOrDefault(vehicleId, false);
    }

    /** Get the current throttle value for a vehicle (-1.0 to 1.0). */
    public Double getThrottle(Long vehicleId) {
        return throttleMap.getOrDefault(vehicleId, 0.0);
    }

    /**
     * Calculate the next speed based on current speed + throttle input.
     *
     * PHYSICS MODEL:
     * - Full throttle (1.0): speed increases by up to 5 km/h per tick (3 seconds).
     * - Full brake (-1.0): speed decreases by up to 8 km/h per tick.
     * - Idle (0.0): speed decreases by 1 km/h per tick (simulating friction/drag).
     * - Speed is clamped between 0 and 200 km/h.
     *
     * @param vehicleId    The vehicle ID (to look up its throttle value).
     * @param currentSpeed The vehicle's current speed from the last reading.
     * @return The calculated next speed (0 to 200 km/h).
     */
    public double computeNextSpeed(Long vehicleId, double currentSpeed) {
        double throttle = getThrottle(vehicleId);
        double nextSpeed;

        if (throttle > 0) {
            // Accelerating: max +5 km/h per tick at full throttle
            nextSpeed = currentSpeed + (throttle * 5.0);
        } else if (throttle < 0) {
            // Braking: max -8 km/h per tick at full brake
            nextSpeed = currentSpeed + (throttle * 8.0);
        } else {
            // Idle: natural friction slowdown (-1 km/h per tick)
            nextSpeed = currentSpeed - 1.0;
        }

        // Clamp between 0 and 200 km/h
        return Math.max(0.0, Math.min(200.0, nextSpeed));
    }

    /**
     * Calculate engine temperature based on current speed.
     * Higher speed = higher temperature. Includes random variance for realism.
     *
     * Formula: 70 (base) + (speed * 0.35) + random(-1.5 to +1.5)
     * At 0 km/h: ~70C, at 100 km/h: ~105C, at 200 km/h: ~140C
     *
     * @param speed Current vehicle speed in km/h.
     * @return Calculated engine temperature in degrees Celsius.
     */
    public double computeTemperature(double speed) {
        return 70.0 + (speed * 0.35) + (Math.random() * 3 - 1.5);
    }

    /**
     * Clear manual control for a vehicle, returning it to auto-simulation mode.
     * Called when a customer releases their vehicle or switches to auto mode.
     */
    public void clearManualControl(Long vehicleId) {
        manualControlMap.remove(vehicleId);
        throttleMap.remove(vehicleId);
    }
}
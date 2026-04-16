package com.vehicle.telemetry.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the manual control state per vehicle.
 * When a driver sends throttle/brake commands via the API,
 * the simulator reads from here instead of generating random values.
 */
@Service
public class VehicleControlService {

    // vehicleId → current throttle value (-1.0 = full brake, 0 = idle, 1.0 = full throttle)
    private final Map<Long, Double> throttleMap = new ConcurrentHashMap<>();

    // vehicleId → whether manual control is active
    private final Map<Long, Boolean> manualControlMap = new ConcurrentHashMap<>();

    // vehicleId → current manual speed
    private final Map<Long, Double> currentSpeedMap = new ConcurrentHashMap<>();

    public void setThrottle(Long vehicleId, String action, Double throttleValue) {
        manualControlMap.put(vehicleId, true);

        switch (action.toUpperCase()) {
            case "ACCELERATE":
                throttleMap.put(vehicleId, Math.min(1.0, Math.abs(throttleValue)));
                break;
            case "BRAKE":
                throttleMap.put(vehicleId, -Math.min(1.0, Math.abs(throttleValue)));
                break;
            case "IDLE":
            default:
                throttleMap.put(vehicleId, 0.0);
                break;
        }
    }

    public boolean isManualControl(Long vehicleId) {
        return manualControlMap.getOrDefault(vehicleId, false);
    }

    public Double getThrottle(Long vehicleId) {
        return throttleMap.getOrDefault(vehicleId, 0.0);
    }

    /**
     * Calculates the next speed based on current speed + throttle input.
     * Acceleration: up to +5 km/h per tick at full throttle.
     * Braking: up to -8 km/h per tick at full brake.
     * Idle: natural deceleration of -1 km/h per tick (friction).
     */
    public double computeNextSpeed(Long vehicleId, double currentSpeed) {
        double throttle = getThrottle(vehicleId);
        double nextSpeed;

        if (throttle > 0) {
            // Accelerating — max +15 km/h per tick at full throttle
            nextSpeed = currentSpeed + (throttle * 15.0);
        } else if (throttle < 0) {
            // Braking — max -15 km/h per tick at full brake
            nextSpeed = currentSpeed + (throttle * 15.0);
        } else {
            // Idle — natural friction slowdown
            nextSpeed = currentSpeed - 2.0;
        }

        // Clamp between 0 and 200
        return Math.max(0.0, Math.min(200.0, nextSpeed));
    }

    /**
     * Temperature correlates with speed:
     * base 70°C + (speed * 0.35) with some variance
     */
    public double computeTemperature(double speed) {
        return 70.0 + (speed * 0.35) + (Math.random() * 3 - 1.5);
    }

    /**
     * Clears manual control for a vehicle, returning it to auto mode.
     */
    public void clearManualControl(Long vehicleId) {
        manualControlMap.remove(vehicleId);
        throttleMap.remove(vehicleId);
    }
}

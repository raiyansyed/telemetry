package com.vehicle.telemetry.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VehicleControlService {

    private final Map<Long, Double> throttleMap = new ConcurrentHashMap<>();

    private final Map<Long, Boolean> manualControlMap = new ConcurrentHashMap<>();

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

    public double computeNextSpeed(Long vehicleId, double currentSpeed) {
        double throttle = getThrottle(vehicleId);
        double nextSpeed;

        if (throttle > 0) {
            nextSpeed = currentSpeed + (throttle * 15.0);
        } else if (throttle < 0) {
            nextSpeed = currentSpeed + (throttle * 15.0);
        } else {
            nextSpeed = currentSpeed - 2.0;
        }

        return Math.max(0.0, Math.min(200.0, nextSpeed));
    }

    public double computeTemperature(double speed) {
        return 70.0 + (speed * 0.35) + (Math.random() * 3 - 1.5);
    }

    public void clearManualControl(Long vehicleId) {
        manualControlMap.remove(vehicleId);
        throttleMap.remove(vehicleId);
    }
}

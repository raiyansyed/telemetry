package com.vehicle.telemetry.service;

import com.vehicle.telemetry.enums.AlertLevel;
import org.springframework.stereotype.Service;

/**
 * AlertService - Evaluates vehicle telemetry readings and determines the alert severity level.
 *
 * This is a simple rules engine that checks speed and temperature against thresholds:
 *
 * THRESHOLDS:
 *   +------------------+-------------------+------------------+
 *   | Condition        | Speed (km/h)      | Temperature (C)  |
 *   +------------------+-------------------+------------------+
 *   | CRITICAL         | >= 110            | >= 110           |
 *   | WARNING          | >= 70             | >= 90            |
 *   | NONE             | < 70              | < 90             |
 *   +------------------+-------------------+------------------+
 *
 * NOTE: CRITICAL takes priority. If EITHER speed OR temperature hits the threshold, that level applies.
 *
 * CALLED BY: VehicleJourneySimulator every 3 seconds for each vehicle.
 * The result is stored in VehicleReading.alertLevel and used to trigger FleetActivity alerts.
 *
 * @Service marks this class as a Spring-managed service bean.
 * Spring creates one instance and injects it wherever needed (dependency injection).
 */
@Service
public class AlertService {

    /**
     * Evaluate speed and temperature and return the appropriate alert level.
     *
     * @param speed       Current vehicle speed in km/h.
     * @param temperature Current engine temperature in degrees Celsius.
     * @return CRITICAL if dangerously high, WARNING if approaching limits, NONE if safe.
     */
    public AlertLevel evaluateAndGetAlertLevel(Double speed, Double temperature) {
        // Check CRITICAL first (highest priority)
        if (speed >= 110 || temperature >= 110) {
            return AlertLevel.CRITICAL;
        }
        // Then check WARNING
        else if (speed >= 70 || temperature >= 90) {
            return AlertLevel.WARNING;
        }
        // Everything else is normal
        return AlertLevel.NONE;
    }
}
package com.vehicle.telemetry.service;

import com.vehicle.telemetry.enums.AlertLevel;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    public AlertLevel evaluateAndGetAlertLevel(Double speed, Double temperature) {
        if (speed >= 110 || temperature >= 110) {
            return AlertLevel.CRITICAL;
        } else if (speed >= 70 || temperature >= 90) {
            return AlertLevel.WARNING;
        }
        return AlertLevel.NONE;
    }
}

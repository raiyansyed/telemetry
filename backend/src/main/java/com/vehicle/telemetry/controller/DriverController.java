package com.vehicle.telemetry.controller;

import com.vehicle.telemetry.dto.VehicleControlRequest;
import com.vehicle.telemetry.entity.VehicleReading;
import com.vehicle.telemetry.service.VehicleControlService;
import com.vehicle.telemetry.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DriverController - Provides vehicle telemetry readings and manual driving controls.
 *
 * ENDPOINTS:
 *   GET  /api/driver/vehicle/{id}/readings        -> Last 20 telemetry readings (for chart)
 *   GET  /api/driver/vehicle/{id}/readings/latest  -> Single most recent reading (for gauges)
 *   POST /api/driver/vehicle/{id}/control          -> Send accelerate/brake/idle command
 *
 * NOTE: Despite the name "driver", this controller is used by BOTH the customer dashboard
 * and the legacy driver dashboard. The customer dashboard calls these endpoints to display
 * live telemetry and to send manual driving controls.
 *
 * MANUAL CONTROL FLOW:
 *   1. Customer holds GAS button -> frontend sends POST /control with action="ACCELERATE"
 *   2. This controller calls VehicleControlService.setThrottle()
 *   3. Next simulator tick uses the throttle value to calculate new speed
 *   4. Customer releases button -> frontend sends action="IDLE"
 *   5. Speed naturally decelerates due to friction
 */
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final VehicleService vehicleService;
    private final VehicleControlService vehicleControlService;

    @GetMapping("/vehicle/{id}/readings")
    public ResponseEntity<List<VehicleReading>> getRecentReadings(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getRecentReadings(id));
    }

    @GetMapping("/vehicle/{id}/readings/latest")
    public ResponseEntity<VehicleReading> getLatestReading(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getLatestReading(id));
    }

    @PostMapping("/vehicle/{id}/control")
    public ResponseEntity<Map<String, String>> controlVehicle(
            @PathVariable Long id,
            @RequestBody VehicleControlRequest request
    ) {
        vehicleControlService.setThrottle(id, request.getAction(), request.getThrottle());
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "action", request.getAction(),
                "throttle", String.valueOf(request.getThrottle())
        ));
    }
}

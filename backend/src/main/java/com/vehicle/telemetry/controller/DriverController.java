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

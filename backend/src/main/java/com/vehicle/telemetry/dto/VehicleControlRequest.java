package com.vehicle.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleControlRequest {
    private String action; // ACCELERATE, BRAKE, IDLE
    private Double throttle; // 0.0 to 1.0
}

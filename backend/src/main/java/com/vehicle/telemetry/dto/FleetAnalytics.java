package com.vehicle.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FleetAnalytics {
    private Double averageSpeed;
    private Double averageTemperature;
    private Long vehicleCount;
    private Long activeRentals;
}

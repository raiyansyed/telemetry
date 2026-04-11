package com.vehicle.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehiclePeakSpeed {
    private Long vehicleId;
    private String vin;
    private String make;
    private String model;
    private Double peakSpeed;
    private Double avgTemperature;
    private Double latitude;
    private Double longitude;
    private String timestamp;
    private String assignedDriverUsername;
}

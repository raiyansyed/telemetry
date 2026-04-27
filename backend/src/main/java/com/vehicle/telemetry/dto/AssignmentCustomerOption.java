package com.vehicle.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCustomerOption {
    private String username;
    private boolean hasOtherVehicle;
    private Long otherVehicleId;
    private String otherVehicleVin;
}

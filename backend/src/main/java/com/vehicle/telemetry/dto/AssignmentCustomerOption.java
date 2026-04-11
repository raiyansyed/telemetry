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
    /** True if this customer already has a different vehicle assigned */
    private boolean hasOtherVehicle;
    private Long otherVehicleId;
    private String otherVehicleVin;
}

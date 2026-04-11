package com.vehicle.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleAssignRequest {
    private String customerUsername;
    /** When true, unassigns the customer's other vehicle (if any) before assigning this one */
    private Boolean swap;
}

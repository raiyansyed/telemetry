package com.vehicle.telemetry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetAlertResponse {
    private Long id;
    private String message;
    private String type;
    private String triggeredAt;
    @JsonProperty("isRead")
    private boolean read;
    private String licensePlate;
}

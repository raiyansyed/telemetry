package com.vehicle.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String role;         // OWNER, DRIVER, CUSTOMER
    private String companyName;  // Only for OWNER
    private String licenseNumber;// Only for CUSTOMER
    private String address;      // Only for CUSTOMER
    private String location;     // City/area for both roles
}

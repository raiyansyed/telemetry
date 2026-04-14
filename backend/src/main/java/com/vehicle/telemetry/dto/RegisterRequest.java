package com.vehicle.telemetry.dto;

import lombok.Data;

/**
 * RegisterRequest - The data sent by the Angular frontend when a new user registers.
 *
 * Contains all the fields from the registration form. Some fields are role-specific:
 * - If role="OWNER": companyName is used (e.g., "Chennai Fleet Co.").
 * - If role="CUSTOMER": licenseNumber and address are used.
 *
 * EXAMPLE JSON (customer registration):
 *   { "username": "john", "password": "pass123", "role": "CUSTOMER",
 *     "location": "Chennai", "licenseNumber": "TN-01-AB-1234", "address": "T. Nagar" }
 *
 * USED BY: POST /api/auth/register (AuthController.register())
 */
@Data  // Lombok: generates getters, setters, toString, equals, hashCode
public class RegisterRequest {
    /** Desired username (must be unique) */
    private String username;
    /** Desired password (will be BCrypt-hashed before storing) */
    private String password;
    /** Role selection: "OWNER" or "CUSTOMER" */
    private String role;
    /** City selection from the dropdown (e.g., "Chennai") */
    private String location;
    /** Owner-only: company/fleet name */
    private String companyName;
    /** Customer-only: driving license number */
    private String licenseNumber;
    /** Customer-only: home address */
    private String address;
}
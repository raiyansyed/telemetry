package com.vehicle.telemetry.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AuthResponse - The data sent back to the Angular frontend after a successful login or registration.
 *
 * After the backend validates the username/password, it creates a JWT token and sends back
 * this response. The Angular frontend stores these values in localStorage and uses them to:
 * - Attach the JWT token to every subsequent API request (via the AuthInterceptor).
 * - Determine which dashboard to show (owner vs customer) based on the role.
 * - Display the username and location in the navbar.
 *
 * EXAMPLE RESPONSE JSON:
 *   { "token": "eyJhbGci...", "role": "OWNER", "location": "Chennai", "username": "owner" }
 *
 * USED BY: POST /api/auth/login and POST /api/auth/register (AuthController)
 */
@Data     // Lombok: generates getters, setters, toString, equals, hashCode
@Builder  // Lombok: enables AuthResponse.builder().token("...").role("OWNER").build()
public class AuthResponse {
    /** The JWT token - a long encoded string that proves the user is authenticated.
     *  The frontend sends this in every API request as: Authorization: Bearer <token> */
    private String token;
    /** The user's role: "OWNER" or "CUSTOMER". Determines which dashboard the frontend shows. */
    private String role;
    /** The user's registered city (e.g., "Chennai"). Displayed in the navbar location selector. */
    private String location;
    /** The user's display name. Shown in the navbar as "Welcome, <username>". */
    private String username;
}
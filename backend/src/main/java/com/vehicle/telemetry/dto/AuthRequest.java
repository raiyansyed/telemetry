package com.vehicle.telemetry.dto;

import lombok.Data;

/**
 * AuthRequest - The data sent by the Angular frontend when a user tries to log in.
 *
 * WHAT IS A DTO?
 * - DTO stands for "Data Transfer Object". It's a simple container for data that gets
 *   sent between the frontend and backend via HTTP requests/responses.
 * - Unlike entities (which map to database tables), DTOs are just used for communication.
 * - Spring automatically converts the JSON body from the frontend into this Java object
 *   using a library called Jackson (JSON <-> Java conversion).
 *
 * EXAMPLE: When the Angular login form sends this JSON:
 *   { "username": "owner", "password": "password" }
 * Spring converts it into an AuthRequest object with username="owner", password="password".
 *
 * USED BY: POST /api/auth/login (AuthController.login())
 */
@Data  // Lombok: generates getters, setters, toString, equals, hashCode
public class AuthRequest {
    /** The username entered in the login form */
    private String username;
    /** The password entered in the login form (sent as plain text, validated against BCrypt hash) */
    private String password;
}
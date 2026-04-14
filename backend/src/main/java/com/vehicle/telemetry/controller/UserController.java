package com.vehicle.telemetry.controller;

import com.vehicle.telemetry.entity.User;
import com.vehicle.telemetry.entity.Vehicle;
import com.vehicle.telemetry.enums.Role;
import com.vehicle.telemetry.repository.UserRepository;
import com.vehicle.telemetry.repository.VehicleRepository;
import com.vehicle.telemetry.repository.CustomerDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * UserController - Handles user profile and location management.
 *
 * ENDPOINTS (all require JWT authentication):
 *   GET  /api/user/profile   -> Returns the logged-in user's profile (username, role, location, isAssigned).
 *   PUT  /api/user/location  -> Updates the user's city location.
 *
 * LOCATION RULES:
 * - Owners CANNOT change their location after registration (locked permanently).
 * - Customers can change location ONLY when they don't have an assigned vehicle.
 *   (Because a vehicle is tied to a city, and you can't drive a Chennai vehicle from Mumbai.)
 *
 * HOW Principal WORKS:
 * - Spring Security injects a Principal object into controller methods.
 * - principal.getName() returns the username of the currently authenticated user.
 * - This is extracted from the JWT token by JwtAuthenticationFilter.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerDetailsRepository customerDetailsRepository;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        boolean isAssigned = false;
        if (user.getRole() == Role.CUSTOMER) {
            var customer = customerDetailsRepository.findByUserId(user.getId()).orElse(null);
            if (customer != null) {
                List<Vehicle> assigned = vehicleRepository.findByAssignedCustomerId(customer.getId());
                isAssigned = !assigned.isEmpty();
            }
        }
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "location", user.getLocation() != null ? user.getLocation() : "",
                "isAssigned", isAssigned
        ));
    }

    @PutMapping("/location")
    public ResponseEntity<Map<String, String>> updateLocation(
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        // Owners cannot change location after registration
        if (user.getRole() == Role.OWNER) {
            return ResponseEntity.badRequest().body(Map.of("error", "Owner location is locked after registration"));
        }

        // Customers with assigned vehicles cannot change location
        if (user.getRole() == Role.CUSTOMER) {
            var customer = customerDetailsRepository.findByUserId(user.getId()).orElse(null);
            if (customer != null) {
                List<Vehicle> assigned = vehicleRepository.findByAssignedCustomerId(customer.getId());
                if (!assigned.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Cannot change location while assigned to a vehicle"));
                }
            }
        }

        String location = body.get("location");
        if (location == null || location.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location is required"));
        }
        user.setLocation(location);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("location", location));
    }
}

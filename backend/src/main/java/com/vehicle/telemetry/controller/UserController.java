package com.vehicle.telemetry.controller;

import com.vehicle.telemetry.entity.User;
import com.vehicle.telemetry.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getProfile(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "location", user.getLocation() != null ? user.getLocation() : ""
        ));
    }

    @PutMapping("/location")
    public ResponseEntity<Map<String, String>> updateLocation(
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        String location = body.get("location");
        if (location == null || location.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location is required"));
        }
        user.setLocation(location);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("location", location));
    }
}

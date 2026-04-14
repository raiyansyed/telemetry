package com.vehicle.telemetry.controller;

import com.vehicle.telemetry.config.AppLocations;
import com.vehicle.telemetry.dto.AuthRequest;
import com.vehicle.telemetry.dto.AuthResponse;
import com.vehicle.telemetry.dto.RegisterRequest;
import com.vehicle.telemetry.entity.CustomerDetails;
import com.vehicle.telemetry.entity.OwnerDetails;
import com.vehicle.telemetry.entity.User;
import com.vehicle.telemetry.enums.Role;
import com.vehicle.telemetry.repository.CustomerDetailsRepository;
import com.vehicle.telemetry.repository.OwnerDetailsRepository;
import com.vehicle.telemetry.repository.UserRepository;
import com.vehicle.telemetry.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuthController - Handles user authentication (login) and registration.
 *
 * ENDPOINTS (all public - no JWT required):
 *   GET  /api/auth/locations  -> Returns the list of supported cities for the location dropdown.
 *   POST /api/auth/login      -> Validates username+password, returns JWT token + user info.
 *   POST /api/auth/register   -> Creates a new user account, returns JWT token + user info.
 *
 * HOW LOGIN WORKS:
 *   1. Frontend sends { username, password } as JSON.
 *   2. AuthenticationManager validates credentials against the database (BCrypt hash comparison).
 *   3. If valid, JwtService generates a JWT token.
 *   4. Response includes: token, role, location, username.
 *   5. Frontend stores these in localStorage and redirects to the appropriate dashboard.
 *
 * HOW REGISTRATION WORKS:
 *   1. Frontend sends username, password, role, location, and role-specific fields.
 *   2. Controller checks for duplicate username.
 *   3. Creates User entity with BCrypt-hashed password.
 *   4. Creates role-specific profile (OwnerDetails or CustomerDetails).
 *   5. Generates JWT token and returns same response as login.
 *
 * @RestController = @Controller + @ResponseBody (all methods return JSON, not HTML views).
 * @RequestMapping("/api/auth") = all endpoints in this class start with /api/auth.
 * @RequiredArgsConstructor = Lombok generates constructor for dependency injection.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OwnerDetailsRepository ownerDetailsRepository;
    private final CustomerDetailsRepository customerDetailsRepository;

    @GetMapping("/locations")
    public ResponseEntity<List<String>> supportedLocations() {
        return ResponseEntity.ok(AppLocations.SUPPORTED_CITIES);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .location(user.getLocation())
                .username(user.getUsername())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .location(request.getLocation() != null && !request.getLocation().isBlank()
                        ? request.getLocation().trim()
                        : AppLocations.DEFAULT_CITY)
                .build();

        user = userRepository.save(user);

        // Create role-specific details
        if (role == Role.OWNER) {
            OwnerDetails ownerDetails = OwnerDetails.builder()
                    .user(user)
                    .companyName(request.getCompanyName() != null ? request.getCompanyName() : "Default Fleet")
                    .fleetSize(0)
                    .build();
            ownerDetailsRepository.save(ownerDetails);
        } else if (role == Role.CUSTOMER) {
            CustomerDetails customerDetails = CustomerDetails.builder()
                    .user(user)
                    .licenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "N/A")
                    .address(request.getAddress() != null ? request.getAddress() : "N/A")
                    .build();
            customerDetailsRepository.save(customerDetails);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .role(role.name())
                .location(user.getLocation())
                .username(user.getUsername())
                .build());
    }
}

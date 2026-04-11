package com.vehicle.telemetry.controller;

import com.vehicle.telemetry.entity.CustomerDetails;
import com.vehicle.telemetry.entity.Rental;
import com.vehicle.telemetry.entity.User;
import com.vehicle.telemetry.entity.Vehicle;
import com.vehicle.telemetry.entity.VehicleReading;
import com.vehicle.telemetry.repository.CustomerDetailsRepository;
import com.vehicle.telemetry.repository.RentalRepository;
import com.vehicle.telemetry.repository.UserRepository;
import com.vehicle.telemetry.repository.VehicleRepository;
import com.vehicle.telemetry.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final UserRepository userRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;

    @GetMapping("/rentals")
    public ResponseEntity<List<Rental>> getRentals(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();
        return ResponseEntity.ok(rentalRepository.findByCustomerId(customer.getId()));
    }

    @GetMapping("/assigned-vehicles")
    public ResponseEntity<List<Vehicle>> getAssignedVehicles(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();
        List<Vehicle> vehicles = vehicleRepository.findByAssignedCustomerId(customer.getId());
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/assigned-vehicle/latest")
    public ResponseEntity<?> getAssignedVehicleLatest(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();
        List<Vehicle> vehicles = vehicleRepository.findByAssignedCustomerId(customer.getId());
        if (vehicles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        VehicleReading latest = vehicleService.getLatestReading(vehicles.get(0).getId());
        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.noContent().build();
    }
}

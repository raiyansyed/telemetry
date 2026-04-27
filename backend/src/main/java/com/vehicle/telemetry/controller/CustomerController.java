package com.vehicle.telemetry.controller;

import com.vehicle.telemetry.entity.*;
import com.vehicle.telemetry.enums.VehicleStatus;
import com.vehicle.telemetry.repository.*;
import com.vehicle.telemetry.service.FleetActivityService;
import com.vehicle.telemetry.service.VehicleControlService;
import com.vehicle.telemetry.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final UserRepository userRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final VehicleControlService vehicleControlService;
    private final FleetActivityService fleetActivityService;
    private final AssignmentRequestRepository assignmentRequestRepository;

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

    /** Customer releases their assigned vehicle */
    @PostMapping("/release-vehicle")
    @Transactional
    public ResponseEntity<?> releaseVehicle(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();
        List<Vehicle> vehicles = vehicleRepository.findByAssignedCustomerId(customer.getId());
        if (vehicles.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No vehicle assigned"));
        }
        Vehicle vehicle = vehicles.get(0);
        OwnerDetails owner = vehicle.getOwner();

        // Clear manual control state for the released vehicle
        vehicleControlService.clearManualControl(vehicle.getId());

        vehicle.setAssignedCustomer(null);
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicleRepository.save(vehicle);

        fleetActivityService.log(owner, vehicle, vehicle.getVin(),
                "Vehicle released by customer @" + user.getUsername(), "INFO");

        return ResponseEntity.ok(Map.of("status", "released", "vehicleVin", vehicle.getVin()));
    }

    // Customer requests assignment to a specific vehicle 
    @PostMapping("/request-vehicle/{vehicleId}")
    @Transactional
    public ResponseEntity<?> requestVehicle(@PathVariable Long vehicleId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();

        // Cannot request if already assigned
        List<Vehicle> assigned = vehicleRepository.findByAssignedCustomerId(customer.getId());
        if (!assigned.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "You already have an assigned vehicle. Release it first."));
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        // Vehicle must be in the customer's location
        String vLoc = vehicle.getLocation() != null ? vehicle.getLocation().trim() : "";
        String cLoc = user.getLocation() != null ? user.getLocation().trim() : "";
        if (!vLoc.equalsIgnoreCase(cLoc)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vehicle is not in your location"));
        }

        // Vehicle must be available (ACTIVE status, no assigned customer)
        if (vehicle.getAssignedCustomer() != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Vehicle is already assigned to another customer"));
        }

        // Check for duplicate pending request
        var existingReq = assignmentRequestRepository.findByCustomer_IdAndVehicle_IdAndStatus(
                customer.getId(), vehicleId, "PENDING");
        if (existingReq.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "You already have a pending request for this vehicle"));
        }

        AssignmentRequest req = AssignmentRequest.builder()
                .customer(customer)
                .vehicle(vehicle)
                .owner(vehicle.getOwner())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        assignmentRequestRepository.save(req);

        return ResponseEntity.ok(Map.of("status", "requested", "vehicleVin", vehicle.getVin()));
    }

    // List available (unassigned) vehicles in the customer's location 
    @GetMapping("/available-vehicles")
    public ResponseEntity<?> getAvailableVehicles(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        String loc = user.getLocation() != null ? user.getLocation().trim() : "";
        List<Vehicle> all = vehicleRepository.findAll();
        List<Vehicle> available = all.stream()
                .filter(v -> v.getAssignedCustomer() == null)
                .filter(v -> v.getStatus() == VehicleStatus.ACTIVE)
                .filter(v -> {
                    String vLoc = v.getLocation() != null ? v.getLocation().trim() : "";
                    return vLoc.equalsIgnoreCase(loc);
                })
                .toList();
        return ResponseEntity.ok(available);
    }

    /** Get alerts for the customer's assigned vehicle */
    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();
        List<Vehicle> vehicles = vehicleRepository.findByAssignedCustomerId(customer.getId());
        if (vehicles.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(fleetActivityService.listForVehicleDirect(vehicles.get(0).getId()));
    }

    /** Mark an alert as read (customer) */
    @PutMapping("/alerts/{id}/read")
    public ResponseEntity<?> markAlertRead(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();
        List<Vehicle> vehicles = vehicleRepository.findByAssignedCustomerId(customer.getId());
        if (!vehicles.isEmpty()) {
            fleetActivityService.markReadByVehicle(id, vehicles.get(0).getId());
        }
        return ResponseEntity.ok(Map.of("status", "read"));
    }

    /** Get customer's pending assignment requests */
    @GetMapping("/pending-requests")
    public ResponseEntity<?> getPendingRequests(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();
        List<AssignmentRequest> pending = assignmentRequestRepository
                .findByCustomer_IdOrderByCreatedAtDesc(customer.getId())
                .stream().filter(r -> "PENDING".equals(r.getStatus())).toList();
        return ResponseEntity.ok(pending.stream().map(r -> Map.of(
                "id", r.getId(),
                "vehicleId", r.getVehicle().getId(),
                "vehicleVin", r.getVehicle().getVin(),
                "status", r.getStatus())).toList());
    }

    /** Switch vehicle back to auto (clear manual control) */
    @PostMapping("/switch-to-auto")
    public ResponseEntity<?> switchToAuto(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        CustomerDetails customer = customerDetailsRepository.findByUserId(user.getId()).orElseThrow();
        List<Vehicle> vehicles = vehicleRepository.findByAssignedCustomerId(customer.getId());
        if (vehicles.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No vehicle assigned"));
        }
        vehicleControlService.clearManualControl(vehicles.get(0).getId());
        return ResponseEntity.ok(Map.of("status", "auto"));
    }
}

package com.vehicle.telemetry.controller;

import com.vehicle.telemetry.config.AppLocations;
import com.vehicle.telemetry.dto.AssignmentCustomerOption;
import com.vehicle.telemetry.dto.FleetAlertResponse;
import com.vehicle.telemetry.dto.FleetAnalytics;
import com.vehicle.telemetry.dto.VehicleAssignRequest;
import com.vehicle.telemetry.dto.VehiclePeakSpeed;
import com.vehicle.telemetry.dto.VehicleRequest;
import com.vehicle.telemetry.entity.*;
import com.vehicle.telemetry.enums.Role;
import com.vehicle.telemetry.enums.VehicleStatus;
import com.vehicle.telemetry.repository.*;
import com.vehicle.telemetry.service.FleetActivityService;
import com.vehicle.telemetry.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OwnerController - The main API for fleet owners. Handles vehicle management, analytics, alerts,
 * and assignment request processing.
 *
 * This is the LARGEST controller with ~400 lines covering all owner dashboard functionality.
 *
 * ENDPOINTS (all require JWT with OWNER role):
 *
 * ANALYTICS & DATA:
 *   GET /api/owner/analytics          -> Fleet-wide summary stats (avg speed, temp, counts)
 *   GET /api/owner/trends             -> Last 50 readings for fleet trend charts
 *   GET /api/owner/peak-speeds        -> Top 5 fastest vehicles today
 *
 * VEHICLE MANAGEMENT:
 *   GET    /api/owner/vehicles             -> List all vehicles
 *   POST   /api/owner/vehicles             -> Add a new vehicle
 *   DELETE /api/owner/vehicles/{id}        -> Remove a vehicle (and all its data)
 *   GET    /api/owner/vehicles/{id}/latest -> Latest telemetry reading for a vehicle
 *   GET    /api/owner/vehicles/{id}/hourly -> Hourly aggregation chart data
 *   GET    /api/owner/vehicles/{id}/alerts -> Alerts specific to a vehicle
 *
 * VEHICLE ASSIGNMENT:
 *   GET  /api/owner/vehicles/{id}/assignment-options -> Eligible customers for assignment
 *   POST /api/owner/vehicles/{id}/assign             -> Assign vehicle to a customer
 *   POST /api/owner/vehicles/{id}/unassign           -> Remove customer from vehicle
 *
 * ALERTS:
 *   GET /api/owner/alerts              -> All fleet alerts
 *   PUT /api/owner/alerts/{id}/read    -> Mark one alert as read
 *   PUT /api/owner/alerts/mark-all-read -> Mark all alerts as read
 *
 * ASSIGNMENT REQUESTS:
 *   GET  /api/owner/assignment-requests         -> Pending customer requests
 *   POST /api/owner/assignment-requests/{id}/approve -> Approve a request
 *   POST /api/owner/assignment-requests/{id}/reject  -> Reject a request
 */
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final VehicleService vehicleService;
    private final FleetActivityService fleetActivityService;
    private final UserRepository userRepository;
    private final OwnerDetailsRepository ownerDetailsRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final VehicleReadingRepository vehicleReadingRepository;
    private final AssignmentRequestRepository assignmentRequestRepository;

    @GetMapping("/analytics")
    public ResponseEntity<FleetAnalytics> getAnalytics(Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        return ResponseEntity.ok(vehicleService.getFleetAnalytics(owner.getId()));
    }

    @GetMapping("/trends")
    public ResponseEntity<List<VehicleReading>> getTrends(Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        return ResponseEntity.ok(vehicleService.getFleetTrendReadings(owner.getId()));
    }

    @GetMapping("/peak-speeds")
    public ResponseEntity<List<VehiclePeakSpeed>> getPeakSpeeds(Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        return ResponseEntity.ok(vehicleService.getTopVehiclePeakSpeeds(owner.getId()));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getVehicles(Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        return ResponseEntity.ok(vehicleRepository.findByOwnerId(owner.getId()));
    }

    @PostMapping("/vehicles")
    @Transactional
    @SuppressWarnings("null")
    public ResponseEntity<?> addVehicle(@RequestBody VehicleRequest request, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        User ownerUser = resolveUser(principal);

        if (request.getVin() == null || request.getVin().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "VIN is required"));
        }

        String loc = ownerUser.getLocation();
        if (loc == null || loc.isBlank()) {
            loc = AppLocations.DEFAULT_CITY;
        }

        Vehicle vehicle = Vehicle.builder()
                .vin(request.getVin().trim())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear() != null ? request.getYear() : 2024)
                .status(VehicleStatus.ACTIVE)
                .owner(owner)
                .location(loc.trim())
                .build();

        Vehicle savedVehicle = Objects.requireNonNull(vehicleRepository.save(vehicle));

        owner.setFleetSize(vehicleRepository.findByOwnerId(owner.getId()).size());
        ownerDetailsRepository.save(owner);

        fleetActivityService.log(owner, savedVehicle, savedVehicle.getVin(),
            "Vehicle added: " + savedVehicle.getMake() + " " + savedVehicle.getModel() + " (" + savedVehicle.getVin() + ")", "INFO");

        return ResponseEntity.ok(savedVehicle);
    }

    @DeleteMapping("/vehicles/{id}")
    @Transactional
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        Vehicle vehicle = vehicleRepository.findByOwnerIdAndId(owner.getId(), id)
                .orElse(null);

        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        String vin = vehicle.getVin();
        assignmentRequestRepository.deleteByVehicleId(id);
        fleetActivityService.deleteForVehicle(id);
        vehicleReadingRepository.deleteByVehicleId(id);
        vehicleRepository.delete(vehicle);

        owner.setFleetSize(vehicleRepository.findByOwnerId(owner.getId()).size());
        ownerDetailsRepository.save(owner);

        fleetActivityService.log(owner, null, vin, "Vehicle removed from fleet: " + vin, "INFO");

        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @GetMapping("/vehicles/{id}/latest")
    public ResponseEntity<VehicleReading> getVehicleLatest(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        Vehicle vehicle = vehicleRepository.findByOwnerIdAndId(owner.getId(), id).orElse(null);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }
        VehicleReading latest = vehicleService.getLatestReading(id);
        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/{id}/hourly")
    public ResponseEntity<?> getVehicleHourly(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        Vehicle vehicle = vehicleRepository.findByOwnerIdAndId(owner.getId(), id).orElse(null);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vehicleService.getHourlyAggregation(id));
    }

    @GetMapping("/vehicles/{id}/alerts")
    public ResponseEntity<List<FleetAlertResponse>> getVehicleAlerts(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        Vehicle vehicle = vehicleRepository.findByOwnerIdAndId(owner.getId(), id).orElse(null);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fleetActivityService.listForVehicle(owner.getId(), id));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<FleetAlertResponse>> getAllAlerts(Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        return ResponseEntity.ok(fleetActivityService.listForOwner(owner.getId()));
    }

    @PutMapping("/alerts/{id}/read")
    public ResponseEntity<?> markAlertRead(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        fleetActivityService.markRead(id, owner.getId());
        return ResponseEntity.ok(Map.of("status", "read"));
    }

    @PutMapping("/alerts/mark-all-read")
    public ResponseEntity<?> markAllAlertsRead(Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        fleetActivityService.markAllRead(owner.getId());
        return ResponseEntity.ok(Map.of("status", "all_read"));
    }

    // ---- Assignment Requests Management ----

    @GetMapping("/assignment-requests")
    public ResponseEntity<?> getAssignmentRequests(Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        List<AssignmentRequest> requests = assignmentRequestRepository.findByOwner_IdAndStatusOrderByCreatedAtDesc(owner.getId(), "PENDING");
        return ResponseEntity.ok(requests.stream().map(r -> Map.of(
                "id", r.getId(),
                "customerUsername", r.getCustomer().getUser().getUsername(),
                "vehicleVin", r.getVehicle().getVin(),
                "vehicleMake", r.getVehicle().getMake(),
                "vehicleModel", r.getVehicle().getModel(),
                "vehicleId", r.getVehicle().getId(),
                "status", r.getStatus(),
                "createdAt", r.getCreatedAt().toString()
        )).collect(Collectors.toList()));
    }

    @PostMapping("/assignment-requests/{id}/approve")
    @Transactional
    public ResponseEntity<?> approveAssignmentRequest(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        AssignmentRequest req = assignmentRequestRepository.findById(id).orElse(null);
        if (req == null || !req.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.notFound().build();
        }
        if (!"PENDING".equals(req.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Request is no longer pending"));
        }

        Vehicle vehicle = req.getVehicle();
        CustomerDetails customer = req.getCustomer();

        // Unassign any existing vehicle from this customer
        List<Vehicle> existing = vehicleRepository.findByAssignedCustomerId(customer.getId());
        for (Vehicle other : existing) {
            other.setAssignedCustomer(null);
            other.setStatus(VehicleStatus.ACTIVE);
            vehicleRepository.save(other);
        }

        vehicle.setAssignedCustomer(customer);
        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        req.setStatus("APPROVED");
        req.setResolvedAt(LocalDateTime.now());
        assignmentRequestRepository.save(req);

        fleetActivityService.log(owner, vehicle, vehicle.getVin(),
                "Assignment request approved: @" + customer.getUser().getUsername(), "INFO");

        return ResponseEntity.ok(Map.of("status", "approved"));
    }

    @PostMapping("/assignment-requests/{id}/reject")
    @Transactional
    public ResponseEntity<?> rejectAssignmentRequest(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        AssignmentRequest req = assignmentRequestRepository.findById(id).orElse(null);
        if (req == null || !req.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.notFound().build();
        }
        if (!"PENDING".equals(req.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Request is no longer pending"));
        }

        req.setStatus("REJECTED");
        req.setResolvedAt(LocalDateTime.now());
        assignmentRequestRepository.save(req);

        fleetActivityService.log(owner, req.getVehicle(), req.getVehicle().getVin(),
                "Assignment request rejected: @" + req.getCustomer().getUser().getUsername(), "INFO");

        return ResponseEntity.ok(Map.of("status", "rejected"));
    }

    /**
     * Customers registered in the same location as the vehicle, with flags for swap UX.
     */
    @GetMapping("/vehicles/{id}/assignment-options")
    public ResponseEntity<?> getAssignmentOptions(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        Vehicle vehicle = vehicleRepository.findByOwnerIdAndId(owner.getId(), id).orElse(null);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        String loc = vehicle.getLocation();
        if (loc == null || loc.isBlank()) {
            loc = AppLocations.DEFAULT_CITY;
        }

        List<User> users = userRepository.findByRoleAndLocationIgnoreCase(Role.CUSTOMER, loc);
        List<AssignmentCustomerOption> options = new ArrayList<>();

        for (User u : users) {
            CustomerDetails cd = customerDetailsRepository.findByUserId(u.getId()).orElse(null);
            if (cd == null) {
                continue;
            }
            List<Vehicle> assigned = vehicleRepository.findByAssignedCustomerId(cd.getId());
            Vehicle other = assigned.stream()
                    .filter(v -> !v.getId().equals(vehicle.getId()))
                    .findFirst()
                    .orElse(null);

            options.add(AssignmentCustomerOption.builder()
                    .username(u.getUsername())
                    .hasOtherVehicle(other != null)
                    .otherVehicleId(other != null ? other.getId() : null)
                    .otherVehicleVin(other != null ? other.getVin() : null)
                    .build());
        }

        return ResponseEntity.ok(options);
    }

    @PostMapping("/vehicles/{id}/assign")
    @Transactional
    public ResponseEntity<?> assignVehicle(
            @PathVariable Long id,
            @RequestBody VehicleAssignRequest request,
            Principal principal
    ) {
        OwnerDetails owner = resolveOwner(principal);
        Vehicle vehicle = vehicleRepository.findByOwnerIdAndId(owner.getId(), id).orElse(null);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getCustomerUsername() == null || request.getCustomerUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Customer username is required"));
        }

        User customerUser = userRepository.findByUsername(request.getCustomerUsername().trim()).orElse(null);
        if (customerUser == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User '" + request.getCustomerUsername() + "' not found"));
        }

        CustomerDetails customer = customerDetailsRepository.findByUserId(customerUser.getId()).orElse(null);
        if (customer == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User '" + request.getCustomerUsername() + "' is not a customer"));
        }

        String vLoc = vehicle.getLocation() != null ? vehicle.getLocation().trim() : "";
        String cLoc = customerUser.getLocation() != null ? customerUser.getLocation().trim() : "";
        if (vLoc.isEmpty()) {
            vLoc = AppLocations.DEFAULT_CITY;
        }
        if (!vLoc.equalsIgnoreCase(cLoc)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Customer must be registered in the vehicle's location: " + vLoc));
        }

        boolean swap = Boolean.TRUE.equals(request.getSwap());
        List<Vehicle> existing = vehicleRepository.findByAssignedCustomerId(customer.getId()).stream()
                .filter(v -> !v.getId().equals(vehicle.getId()))
                .collect(Collectors.toList());

        if (!existing.isEmpty()) {
            if (!swap) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "This customer already has another vehicle assigned. Enable swap to move them to this vehicle."));
            }
            for (Vehicle other : existing) {
                other.setAssignedCustomer(null);
                other.setStatus(VehicleStatus.ACTIVE);
                vehicleRepository.save(other);
                fleetActivityService.log(owner, other, other.getVin(),
                        "Unassigned " + other.getVin() + " (customer moved to " + vehicle.getVin() + ")", "INFO");
            }
        }

        vehicle.setAssignedCustomer(customer);
        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        fleetActivityService.log(owner, vehicle, vehicle.getVin(),
                "Assigned to @" + customerUser.getUsername(), "INFO");

        return ResponseEntity.ok(Map.of(
                "status", "assigned",
                "vehicleVin", vehicle.getVin(),
                "assignedTo", customerUser.getUsername()
        ));
    }

    @PostMapping("/vehicles/{id}/unassign")
    @Transactional
    public ResponseEntity<?> unassignVehicle(@PathVariable Long id, Principal principal) {
        OwnerDetails owner = resolveOwner(principal);
        Vehicle vehicle = vehicleRepository.findByOwnerIdAndId(owner.getId(), id).orElse(null);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        String prev = vehicle.getAssignedCustomer() != null && vehicle.getAssignedCustomer().getUser() != null
                ? vehicle.getAssignedCustomer().getUser().getUsername()
                : null;

        vehicle.setAssignedCustomer(null);
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicleRepository.save(vehicle);

        fleetActivityService.log(owner, vehicle, vehicle.getVin(),
                prev != null ? ("Unassigned from @" + prev) : "Vehicle unassigned", "INFO");

        return ResponseEntity.ok(Map.of("status", "unassigned"));
    }

    private OwnerDetails resolveOwner(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return ownerDetailsRepository.findByUserId(user.getId()).orElseThrow();
    }

    private User resolveUser(Principal principal) {
        return userRepository.findByUsername(principal.getName()).orElseThrow();
    }
}

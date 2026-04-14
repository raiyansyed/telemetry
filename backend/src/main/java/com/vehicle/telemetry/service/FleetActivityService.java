package com.vehicle.telemetry.service;

import com.vehicle.telemetry.dto.FleetAlertResponse;
import com.vehicle.telemetry.entity.FleetActivity;
import com.vehicle.telemetry.entity.OwnerDetails;
import com.vehicle.telemetry.entity.Vehicle;
import com.vehicle.telemetry.repository.FleetActivityRepository;
import com.vehicle.telemetry.repository.OwnerDetailsRepository;
import com.vehicle.telemetry.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FleetActivityService - Manages the creation and retrieval of fleet alert/activity logs.
 *
 * This service is the "notification system" of the app. Every important event gets logged here:
 * - Vehicle added/removed from fleet
 * - Customer assigned/unassigned
 * - Speed/temperature alerts from the simulator
 * - Assignment request approved/rejected
 *
 * The owner sees these in the "Fleet Alerts" panel, and customers see vehicle-specific
 * alerts in their dashboard. Alerts can be marked as read (individually or all at once).
 *
 * TWO log() METHODS:
 * - log(): used in controller methods where we have entity objects (owner, vehicle).
 * - logByIds(): used in the simulator thread where we only have IDs (safer for non-request threads).
 *   The simulator runs on a background thread that doesn't have access to the HTTP request context,
 *   so we use getReferenceById() to create proxy references without loading full entities.
 */
@Service
@RequiredArgsConstructor
public class FleetActivityService {

    private final FleetActivityRepository fleetActivityRepository;
    private final OwnerDetailsRepository ownerDetailsRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public void log(OwnerDetails owner, Vehicle vehicle, String vin, String message, String alertType) {
        logByIds(owner.getId(), vehicle != null ? vehicle.getId() : null,
                vin != null ? vin : (vehicle != null ? vehicle.getVin() : null), message, alertType);
    }

    /**
     * Persists fleet activity using only ids (safe from non-request threads e.g. simulator).
     */
    @Transactional
    public void logByIds(Long ownerDetailsId, Long vehicleId, String vin, String message, String alertType) {
        OwnerDetails ownerRef = ownerDetailsRepository.getReferenceById(ownerDetailsId);
        Vehicle vehicleRef = vehicleId != null ? vehicleRepository.getReferenceById(vehicleId) : null;
        FleetActivity row = FleetActivity.builder()
                .createdAt(LocalDateTime.now())
                .owner(ownerRef)
                .vehicle(vehicleRef)
                .vin(vin)
                .message(message)
                .alertType(alertType)
                .read(false)
                .build();
        fleetActivityRepository.save(row);
    }

    public List<FleetAlertResponse> listForOwner(Long ownerId) {
        return fleetActivityRepository.findTop100ByOwner_IdOrderByCreatedAtDesc(ownerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FleetAlertResponse> listForVehicle(Long ownerId, Long vehicleId) {
        return fleetActivityRepository.findTop100ByOwner_IdOrderByCreatedAtDesc(ownerId).stream()
                .filter(a -> a.getVehicle() != null && a.getVehicle().getId().equals(vehicleId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns alerts for a specific vehicle (used by customer dashboard).
     */
    public List<FleetAlertResponse> listForVehicleDirect(Long vehicleId) {
        return fleetActivityRepository.findTop50ByVehicle_IdOrderByCreatedAtDesc(vehicleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark an alert as read by vehicle ownership check (for customers).
     */
    @Transactional
    public void markReadByVehicle(Long activityId, Long vehicleId) {
        fleetActivityRepository.findById(activityId).ifPresent(a -> {
            if (a.getVehicle() != null && a.getVehicle().getId().equals(vehicleId)) {
                a.setRead(true);
                fleetActivityRepository.save(a);
            }
        });
    }

    @Transactional
    public void markRead(Long activityId, Long ownerId) {
        fleetActivityRepository.findById(activityId).ifPresent(a -> {
            if (a.getOwner().getId().equals(ownerId)) {
                a.setRead(true);
                fleetActivityRepository.save(a);
            }
        });
    }

    @Transactional
    public void markAllRead(Long ownerId) {
        List<FleetActivity> unread = fleetActivityRepository.findTop100ByOwner_IdOrderByCreatedAtDesc(ownerId)
                .stream().filter(a -> !a.isRead()).collect(Collectors.toList());
        for (FleetActivity a : unread) {
            a.setRead(true);
        }
        fleetActivityRepository.saveAll(unread);
    }

    @Transactional
    public void deleteForVehicle(Long vehicleId) {
        fleetActivityRepository.deleteByVehicleId(vehicleId);
    }

    private FleetAlertResponse toResponse(FleetActivity a) {
        return FleetAlertResponse.builder()
                .id(a.getId())
                .message(a.getMessage())
                .type(a.getAlertType())
                .triggeredAt(a.getCreatedAt().toString())
                .read(a.isRead())
                .licensePlate(a.getVin())
                .build();
    }
}

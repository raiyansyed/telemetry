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

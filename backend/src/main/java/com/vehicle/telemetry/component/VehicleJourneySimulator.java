package com.vehicle.telemetry.component;

import com.vehicle.telemetry.entity.Vehicle;
import com.vehicle.telemetry.entity.VehicleReading;
import com.vehicle.telemetry.enums.AlertLevel;
import com.vehicle.telemetry.repository.VehicleReadingRepository;
import com.vehicle.telemetry.repository.VehicleRepository;
import com.vehicle.telemetry.service.AlertService;
import com.vehicle.telemetry.service.FleetActivityService;
import com.vehicle.telemetry.service.VehicleControlService;
import com.vehicle.telemetry.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@Order(2)
@RequiredArgsConstructor
public class VehicleJourneySimulator implements CommandLineRunner {

    /** Approximate Chennai center for new / seeded vehicles */
    private static final double DEFAULT_LAT = 13.0827;
    private static final double DEFAULT_LON = 80.2707;

    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;
    private final VehicleReadingRepository vehicleReadingRepository;
    private final AlertService alertService;
    private final VehicleControlService vehicleControlService;
    private final FleetActivityService fleetActivityService;
    private final Random random = new Random();

    private final Map<Long, VehicleState> vehicleStates = new HashMap<>();

    @Override
    public void run(String... args) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                while (!Thread.currentThread().isInterrupted()) {
                    List<Vehicle> vehicles = vehicleService.findAllVehiclesForSimulation();
                    if (vehicles.isEmpty()) {
                        Thread.sleep(3000);
                        continue;
                    }
                    for (Vehicle v : vehicles) {
                        vehicleStates.computeIfAbsent(v.getId(), id -> initialState());
                    }
                    vehicleStates.keySet().removeIf(id ->
                            vehicles.stream().noneMatch(ve -> ve.getId().equals(id)));
                    simulateTick(vehicles);
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "vehicle-journey-simulator").start();
    }

    private VehicleState initialState() {
        return new VehicleState(
                DEFAULT_LAT + (random.nextDouble() * 0.08 - 0.04),
                DEFAULT_LON + (random.nextDouble() * 0.08 - 0.04),
                60.0 + random.nextDouble() * 10,
                70.0 + random.nextDouble() * 10,
                AlertLevel.NONE
        );
    }

    private void simulateTick(List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            VehicleState state = vehicleStates.get(vehicle.getId());
            if (state == null) {
                continue;
            }
            Long vehicleId = vehicle.getId();
            boolean isRented = vehicle.getAssignedCustomer() != null;

            if (isRented) {
                // Full live telemetry for rented vehicles
                if (vehicleControlService.isManualControl(vehicleId)) {
                    state.speed = vehicleControlService.computeNextSpeed(vehicleId, state.speed);
                    state.temperature = vehicleControlService.computeTemperature(state.speed);
                } else {
                    state.speed = Math.max(0.0, Math.min(130.0, state.speed + (random.nextDouble() * 10 - 5)));
                    state.temperature = Math.max(60.0, Math.min(120.0, state.temperature + (random.nextDouble() * 4 - 2)));
                }

                state.latitude += (random.nextDouble() * 0.001 - 0.0005);
                state.longitude += (random.nextDouble() * 0.001 - 0.0005);
            } else {
                // Unassigned vehicles: static GPS, zero speed, idle temperature
                state.speed = 0.0;
                state.temperature = 70.0;
            }

            AlertLevel level = alertService.evaluateAndGetAlertLevel(state.speed, state.temperature);

            var vehicleRef = vehicleRepository.getReferenceById(vehicleId);
            VehicleReading reading = VehicleReading.builder()
                    .timestamp(LocalDateTime.now())
                    .speed(formatValue(state.speed))
                    .temperature(formatValue(state.temperature))
                    .latitude(formatValue(state.latitude))
                    .longitude(formatValue(state.longitude))
                    .alertLevel(level)
                    .vehicle(vehicleRef)
                    .build();

            vehicleReadingRepository.save(reading);

            if (isRented && level != AlertLevel.NONE && state.lastTelemetryAlertLogged != level) {
                String type = level == AlertLevel.CRITICAL ? "CRITICAL" : "WARNING";
                String msg = String.format("%s on %s: speed %.1f km/h, engine %.1f °C",
                        level.name(), vehicle.getVin(), state.speed, state.temperature);
                fleetActivityService.logByIds(vehicle.getOwner().getId(), vehicleId, vehicle.getVin(), msg, type);
                state.lastTelemetryAlertLogged = level;
            } else if (level == AlertLevel.NONE) {
                state.lastTelemetryAlertLogged = AlertLevel.NONE;
            }

            if (isRented) {
                String mode = vehicleControlService.isManualControl(vehicleId) ? "MANUAL" : "AUTO";
                System.out.printf("[%s] [VIN: %s] Speed: %.2f km/h | Temp: %.2f°C | Alert: %s%n",
                        mode, vehicle.getVin(), state.speed, state.temperature, level.name());
            }
        }
    }

    private Double formatValue(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class VehicleState {
        double latitude;
        double longitude;
        double speed;
        double temperature;
        AlertLevel lastTelemetryAlertLogged;

        VehicleState(Double lat, Double lon, double s, double t, AlertLevel lastTelemetryAlertLogged) {
            this.latitude = lat;
            this.longitude = lon;
            this.speed = s;
            this.temperature = t;
            this.lastTelemetryAlertLogged = lastTelemetryAlertLogged;
        }
    }
}

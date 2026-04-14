package com.vehicle.telemetry.service;

import com.vehicle.telemetry.dto.FleetAnalytics;
import com.vehicle.telemetry.dto.VehiclePeakSpeed;
import com.vehicle.telemetry.entity.Vehicle;
import com.vehicle.telemetry.entity.VehicleReading;
import com.vehicle.telemetry.repository.VehicleReadingRepository;
import com.vehicle.telemetry.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * VehicleService - Core business logic for vehicle analytics, telemetry queries, and data aggregation.
 *
 * This service is the "brain" behind the owner dashboard's charts and statistics.
 * It doesn't generate data (that's the simulator's job) - it QUERIES and TRANSFORMS existing data
 * into formats the frontend can display.
 *
 * KEY FEATURES:
 * 1. Fleet Analytics: calculates average speed, average temp, vehicle count, active rentals.
 * 2. Fleet Trends: gets the 50 most recent readings for real-time fleet trend charts.
 * 3. Peak Speeds: finds today's top 5 fastest vehicles for the leaderboard.
 * 4. Hourly Aggregation: groups readings by hour for the vehicle detail popup chart.
 * 5. Recent Readings: gets the last 20 readings for the driver/customer telemetry history chart.
 *
 * ANNOTATIONS:
 * - @Service: marks this as a Spring-managed service bean.
 * - @RequiredArgsConstructor: Lombok generates a constructor for final fields (dependency injection).
 * - @Transactional(readOnly = true): optimizes database reads by telling Hibernate
 *   "this method won't modify data" - allows performance optimizations.
 */
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleReadingRepository vehicleReadingRepository;

    /**
     * Calculate fleet-wide summary statistics for the owner dashboard.
     * Returns average speed, average temperature, vehicle count, and active rental count.
     *
     * @param ownerId The owner_details.id (NOT the user.id) of the fleet owner.
     * @return FleetAnalytics DTO with the 4 summary numbers.
     */
    public FleetAnalytics getFleetAnalytics(Long ownerId) {
        Double avgSpeed = vehicleReadingRepository.getAverageSpeedByOwner(ownerId);
        Double avgTemp = vehicleReadingRepository.getAverageTemperatureByOwner(ownerId);
        List<Vehicle> ownerVehicles = vehicleRepository.findByOwnerId(ownerId);
        long vehicleCount = ownerVehicles.size();
        // Count vehicles that have an assigned customer (status RENTED) — not legacy rental rows
        long activeRentals = ownerVehicles.stream()
                .filter(v -> v.getAssignedCustomer() != null)
                .count();

        return FleetAnalytics.builder()
                .averageSpeed(avgSpeed != null ? formatToTwoDecimals(avgSpeed) : 0.0)
                .averageTemperature(avgTemp != null ? formatToTwoDecimals(avgTemp) : 0.0)
                .vehicleCount(vehicleCount)
                .activeRentals(activeRentals)
                .build();
    }

    /**
     * Get the 20 most recent telemetry readings for a specific vehicle (newest first).
     * Used by the driver/customer dashboard's "Telemetry History" chart.
     */
    public List<VehicleReading> getRecentReadings(Long vehicleId) {
        return vehicleReadingRepository.findTop20ByVehicleIdOrderByTimestampDesc(vehicleId);
    }

    /** Get the single most recent reading for a vehicle (for speedometer/gauge display). */
    public VehicleReading getLatestReading(Long vehicleId) {
        return vehicleReadingRepository.findFirstByVehicleIdOrderByTimestampDesc(vehicleId);
    }

    /**
     * Load all vehicles with their owner data for the simulator.
     * Uses JOIN FETCH to avoid N+1 query problems (loads owners in same SQL query).
     * @Transactional(readOnly = true) optimizes the database read.
     */
    @Transactional(readOnly = true)
    public List<Vehicle> findAllVehiclesForSimulation() {
        return vehicleRepository.findAllJoinFetchOwner();
    }

    /**
     * Returns the latest 50 fleet-wide readings for chart trends.
     */
    public List<VehicleReading> getFleetTrendReadings(Long ownerId) {
        List<VehicleReading> readings = vehicleReadingRepository.findTop50ByOwner(ownerId);
        if (readings.size() > 50) {
            readings = readings.subList(0, 50);
        }
        Collections.reverse(readings); // oldest first for chart x-axis
        return readings;
    }

    /**
     * Returns peak speed today for the top 5 vehicles belonging to an owner.
     * Matches the dataset example: peak speed, avg engine temp, GPS location, timestamp.
     * Now includes the assigned driver/customer username.
     */
    public List<VehiclePeakSpeed> getTopVehiclePeakSpeeds(Long ownerId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<VehicleReading> readings = vehicleReadingRepository.findReadingsByOwnerSince(ownerId, startOfDay);

        // Group by vehicle, find the reading with max speed per vehicle
        Map<Long, VehicleReading> peakByVehicle = new LinkedHashMap<>();
        for (VehicleReading r : readings) {
            Long vid = r.getVehicle().getId();
            if (!peakByVehicle.containsKey(vid) || r.getSpeed() > peakByVehicle.get(vid).getSpeed()) {
                peakByVehicle.put(vid, r);
            }
        }

        // Sort by peak speed descending and take top 5
        return peakByVehicle.values().stream()
                .sorted(Comparator.comparingDouble(VehicleReading::getSpeed).reversed())
                .limit(5)
                .map(r -> {
                    String assignedDriver = null;
                    if (r.getVehicle().getAssignedCustomer() != null
                            && r.getVehicle().getAssignedCustomer().getUser() != null) {
                        assignedDriver = r.getVehicle().getAssignedCustomer().getUser().getUsername();
                    }
                    return VehiclePeakSpeed.builder()
                            .vehicleId(r.getVehicle().getId())
                            .vin(r.getVehicle().getVin())
                            .make(r.getVehicle().getMake())
                            .model(r.getVehicle().getModel())
                            .peakSpeed(formatToTwoDecimals(r.getSpeed()))
                            .avgTemperature(formatToTwoDecimals(r.getTemperature()))
                            .latitude(r.getLatitude())
                            .longitude(r.getLongitude())
                            .timestamp(r.getTimestamp().toString())
                            .assignedDriverUsername(assignedDriver)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns hourly aggregation for a specific vehicle (today's data).
     * Returns a map of { hours: [...], avgSpeeds: [...], avgTemps: [...] }
     */
    /**
     * Calculate hourly averages for a vehicle's speed and temperature today.
     * Returns data formatted for Chart.js: arrays of hours, average speeds, and average temperatures.
     * Used by the vehicle detail popup's hourly telemetry chart on the owner dashboard.
     *
     * EXAMPLE RETURN:
     *   { "hours": [9, 10, 11], "avgSpeeds": [65.5, 72.3, 80.1], "avgTemps": [85.2, 89.1, 95.3] }
     */
    public Map<String, Object> getHourlyAggregation(Long vehicleId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<VehicleReading> readings = vehicleReadingRepository.findByVehicleIdSince(vehicleId, startOfDay);

        // Group readings by hour
        Map<Integer, List<VehicleReading>> byHour = new TreeMap<>();
        for (VehicleReading r : readings) {
            int hour = r.getTimestamp().getHour();
            byHour.computeIfAbsent(hour, k -> new ArrayList<>()).add(r);
        }

        List<Integer> hours = new ArrayList<>();
        List<Double> avgSpeeds = new ArrayList<>();
        List<Double> avgTemps = new ArrayList<>();

        for (Map.Entry<Integer, List<VehicleReading>> entry : byHour.entrySet()) {
            hours.add(entry.getKey());
            double avgS = entry.getValue().stream().mapToDouble(VehicleReading::getSpeed).average().orElse(0);
            double avgT = entry.getValue().stream().mapToDouble(VehicleReading::getTemperature).average().orElse(0);
            avgSpeeds.add(formatToTwoDecimals(avgS));
            avgTemps.add(formatToTwoDecimals(avgT));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hours", hours);
        result.put("avgSpeeds", avgSpeeds);
        result.put("avgTemps", avgTemps);
        return result;
    }

    /** Round a Double value to 2 decimal places (e.g., 85.6789 -> 85.68). */
    private Double formatToTwoDecimals(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double formatToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

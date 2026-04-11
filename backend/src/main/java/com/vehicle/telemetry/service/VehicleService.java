package com.vehicle.telemetry.service;

import com.vehicle.telemetry.dto.FleetAnalytics;
import com.vehicle.telemetry.dto.VehiclePeakSpeed;
import com.vehicle.telemetry.entity.Vehicle;
import com.vehicle.telemetry.entity.VehicleReading;
import com.vehicle.telemetry.enums.RentalStatus;
import com.vehicle.telemetry.repository.RentalRepository;
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
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleReadingRepository vehicleReadingRepository;
    private final RentalRepository rentalRepository;

    public FleetAnalytics getFleetAnalytics(Long ownerId) {
        Double avgSpeed = vehicleReadingRepository.getAverageSpeedByOwner(ownerId);
        Double avgTemp = vehicleReadingRepository.getAverageTemperatureByOwner(ownerId);
        long vehicleCount = vehicleRepository.findByOwnerId(ownerId).size();
        long activeRentals = rentalRepository.findByOwnerIdAndStatus(ownerId, RentalStatus.ACTIVE).size();

        return FleetAnalytics.builder()
                .averageSpeed(avgSpeed != null ? formatToTwoDecimals(avgSpeed) : 0.0)
                .averageTemperature(avgTemp != null ? formatToTwoDecimals(avgTemp) : 0.0)
                .vehicleCount(vehicleCount)
                .activeRentals(activeRentals)
                .build();
    }

    public List<VehicleReading> getRecentReadings(Long vehicleId) {
        return vehicleReadingRepository.findTop20ByVehicleIdOrderByTimestampDesc(vehicleId);
    }

    public VehicleReading getLatestReading(Long vehicleId) {
        return vehicleReadingRepository.findFirstByVehicleIdOrderByTimestampDesc(vehicleId);
    }

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

    private Double formatToTwoDecimals(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double formatToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

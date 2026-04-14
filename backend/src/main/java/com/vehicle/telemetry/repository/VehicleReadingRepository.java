package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.VehicleReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VehicleReadingRepository - Database operations for telemetry readings (VehicleReading entity).
 *
 * This is one of the most important repositories because it handles ALL the telemetry data
 * that powers the charts, gauges, and analytics in the frontend.
 *
 * QUERY TYPES:
 * 1. Auto-generated queries (Spring Data JPA derives SQL from the method name):
 *    - findTop20ByVehicleIdOrderByTimestampDesc -> "SELECT * ... LIMIT 20 ORDER BY timestamp DESC"
 *    - findFirstByVehicleIdOrderByTimestampDesc -> gets the single most recent reading
 *
 * 2. Custom JPQL queries (@Query annotation):
 *    - getAverageSpeedByOwner -> calculates fleet-wide average speed for analytics
 *    - findTop50ByOwner -> gets recent readings across all of an owner's vehicles for trend charts
 *    - findReadingsByOwnerSince -> gets readings since a specific time for peak speed calculation
 *
 * USED BY: VehicleService (analytics, charts), VehicleJourneySimulator (saves readings),
 *          DriverController (live telemetry), OwnerController (vehicle detail popup)
 */
public interface VehicleReadingRepository extends JpaRepository<VehicleReading, Long> {

    /**
     * Get the 20 most recent readings for a vehicle (newest first).
     * Used by the "Telemetry History" line chart in the driver/customer dashboard.
     * "Top20" = LIMIT 20, "OrderByTimestampDesc" = ORDER BY timestamp DESC.
     */
    List<VehicleReading> findTop20ByVehicleIdOrderByTimestampDesc(Long vehicleId);

    /**
     * Get the single most recent reading for a vehicle.
     * Used by the speedometer gauge and "latest reading" API endpoints.
     * "First" = LIMIT 1, "OrderByTimestampDesc" = most recent.
     */
    VehicleReading findFirstByVehicleIdOrderByTimestampDesc(Long vehicleId);

    /**
     * Calculate the average speed across ALL vehicles belonging to a specific owner.
     * Used for the "Average Speed" statistic on the owner dashboard.
     * JPQL (Java Persistence Query Language) is like SQL but uses entity names instead of table names.
     */
    @Query("SELECT AVG(vr.speed) FROM VehicleReading vr WHERE vr.vehicle.owner.id = :ownerId")
    Double getAverageSpeedByOwner(@Param("ownerId") Long ownerId);

    /**
     * Calculate the average engine temperature across ALL vehicles belonging to a specific owner.
     * Used for the "Average Temperature" statistic on the owner dashboard.
     */
    @Query("SELECT AVG(vr.temperature) FROM VehicleReading vr WHERE vr.vehicle.owner.id = :ownerId")
    Double getAverageTemperatureByOwner(@Param("ownerId") Long ownerId);

    /**
     * Get the 50 most recent readings across ALL of an owner's vehicles (for fleet trend charts).
     * The owner dashboard shows two trend line charts (speed and temperature over time).
     * These readings are reversed in VehicleService so the chart shows oldest-to-newest (left-to-right).
     */
    @Query("SELECT vr FROM VehicleReading vr WHERE vr.vehicle.owner.id = :ownerId ORDER BY vr.timestamp DESC")
    List<VehicleReading> findTop50ByOwner(@Param("ownerId") Long ownerId);

    /**
     * Get all readings for an owner's vehicles since a specific timestamp.
     * Used by getTopVehiclePeakSpeeds() to find today's peak speeds.
     */
    @Query("SELECT vr FROM VehicleReading vr WHERE vr.vehicle.owner.id = :ownerId AND vr.timestamp >= :since ORDER BY vr.timestamp DESC")
    List<VehicleReading> findReadingsByOwnerSince(@Param("ownerId") Long ownerId, @Param("since") LocalDateTime since);

    /**
     * Get all readings for a specific vehicle since a specific timestamp.
     * Used by getHourlyAggregation() for the vehicle detail popup's hourly chart.
     */
    @Query("SELECT vr FROM VehicleReading vr WHERE vr.vehicle.id = :vehicleId AND vr.timestamp >= :since ORDER BY vr.timestamp ASC")
    List<VehicleReading> findByVehicleIdSince(@Param("vehicleId") Long vehicleId, @Param("since") LocalDateTime since);

    /**
     * Delete all readings for a specific vehicle.
     * @Modifying marks this as an UPDATE/DELETE query (not a SELECT).
     * Called when an owner deletes a vehicle - all its telemetry data is removed too.
     */
    @Modifying
    @Query("DELETE FROM VehicleReading vr WHERE vr.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);
}
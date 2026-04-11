package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.VehicleReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleReadingRepository extends JpaRepository<VehicleReading, Long> {

    List<VehicleReading> findTop20ByVehicleIdOrderByTimestampDesc(Long vehicleId);

    @Query("SELECT AVG(v.speed) FROM VehicleReading v WHERE v.vehicle.owner.id = :ownerId")
    Double getAverageSpeedByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT AVG(v.temperature) FROM VehicleReading v WHERE v.vehicle.owner.id = :ownerId")
    Double getAverageTemperatureByOwner(@Param("ownerId") Long ownerId);

    VehicleReading findFirstByVehicleIdOrderByTimestampDesc(Long vehicleId);

    // For owner speed/temp trends — latest 50 readings across all fleet vehicles
    @Query("SELECT vr FROM VehicleReading vr WHERE vr.vehicle.owner.id = :ownerId ORDER BY vr.timestamp DESC")
    List<VehicleReading> findTop50ByOwner(@Param("ownerId") Long ownerId);

    // For peak speed today per vehicle — max speed reading per vehicle belonging to an owner
    @Query("SELECT vr FROM VehicleReading vr WHERE vr.vehicle.owner.id = :ownerId AND vr.timestamp >= :since ORDER BY vr.speed DESC")
    List<VehicleReading> findReadingsByOwnerSince(@Param("ownerId") Long ownerId, @Param("since") LocalDateTime since);

    // Readings for a specific vehicle since a timestamp (for hourly aggregation)
    @Query("SELECT vr FROM VehicleReading vr WHERE vr.vehicle.id = :vehicleId AND vr.timestamp >= :since ORDER BY vr.timestamp ASC")
    List<VehicleReading> findByVehicleIdSince(@Param("vehicleId") Long vehicleId, @Param("since") LocalDateTime since);

    @Modifying
    @Transactional
    @Query("DELETE FROM VehicleReading vr WHERE vr.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);
}

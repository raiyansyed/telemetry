package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.FleetActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FleetActivityRepository extends JpaRepository<FleetActivity, Long> {

    List<FleetActivity> findTop100ByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FleetActivity f WHERE f.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);
}

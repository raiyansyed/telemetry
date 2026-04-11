package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByOwnerId(Long ownerId);

    List<Vehicle> findByAssignedCustomerId(Long customerId);

    java.util.Optional<Vehicle> findByOwnerIdAndId(Long ownerId, Long vehicleId);

    @Query("SELECT DISTINCT v FROM Vehicle v JOIN FETCH v.owner")
    List<Vehicle> findAllJoinFetchOwner();
}

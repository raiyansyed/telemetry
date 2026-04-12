package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.AssignmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRequestRepository extends JpaRepository<AssignmentRequest, Long> {

    List<AssignmentRequest> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    List<AssignmentRequest> findByOwner_IdAndStatusOrderByCreatedAtDesc(Long ownerId, String status);

    List<AssignmentRequest> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    Optional<AssignmentRequest> findByCustomer_IdAndVehicle_IdAndStatus(Long customerId, Long vehicleId, String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM AssignmentRequest ar WHERE ar.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);
}

package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.Rental;
import com.vehicle.telemetry.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByOwnerIdAndStatus(Long ownerId, RentalStatus status);
    List<Rental> findByCustomerId(Long customerId);
}

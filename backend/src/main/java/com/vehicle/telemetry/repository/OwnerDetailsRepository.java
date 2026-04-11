package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.OwnerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerDetailsRepository extends JpaRepository<OwnerDetails, Long> {
    Optional<OwnerDetails> findByUserId(Long userId);
}

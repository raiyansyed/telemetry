package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.CustomerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerDetailsRepository extends JpaRepository<CustomerDetails, Long> {
    Optional<CustomerDetails> findByUserId(Long userId);
}

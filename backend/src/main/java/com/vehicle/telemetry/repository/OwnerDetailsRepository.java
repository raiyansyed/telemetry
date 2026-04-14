package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.OwnerDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * OwnerDetailsRepository - Database operations for the OwnerDetails entity.
 *
 * Extends JpaRepository which gives standard CRUD for free.
 * The custom method findByUserId() links OwnerDetails back to the User table.
 *
 * USED BY: AuthController (registration), OwnerController (resolve owner from JWT principal)
 */
public interface OwnerDetailsRepository extends JpaRepository<OwnerDetails, Long> {

    /**
     * Find the OwnerDetails record for a given User ID.
     * Spring generates: SELECT * FROM owner_details WHERE user_id = ?
     * Used when we know the logged-in user's ID and need their owner profile.
     */
    Optional<OwnerDetails> findByUserId(Long userId);
}
package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.CustomerDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * CustomerDetailsRepository - Database operations for the CustomerDetails entity.
 *
 * Extends JpaRepository which gives standard CRUD for free.
 * The custom method findByUserId() links CustomerDetails back to the User table.
 *
 * USED BY: AuthController (registration), CustomerController (resolve customer from JWT principal),
 *          OwnerController (find customer details when assigning vehicles)
 */
public interface CustomerDetailsRepository extends JpaRepository<CustomerDetails, Long> {

    /**
     * Find the CustomerDetails record for a given User ID.
     * Spring generates: SELECT * FROM customer_details WHERE user_id = ?
     * Used when we know the logged-in user's ID and need their customer profile.
     */
    Optional<CustomerDetails> findByUserId(Long userId);
}
package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * RentalRepository - Database operations for the (legacy) Rental entity.
 *
 * NOTE: The rental system is largely legacy. The current app uses direct vehicle assignment
 * (Vehicle.assignedCustomer) rather than formal Rental records.
 *
 * USED BY: CustomerController (to show rental info if any exist), DatabaseSeedUtility (seeds one demo rental)
 */
public interface RentalRepository extends JpaRepository<Rental, Long> {

    /** Get all rentals for a specific customer, used by the customer dashboard */
    List<Rental> findByCustomerId(Long customerId);
}
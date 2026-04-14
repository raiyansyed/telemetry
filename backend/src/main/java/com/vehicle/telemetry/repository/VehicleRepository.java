package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * VehicleRepository - Database operations for the Vehicle entity.
 *
 * QUERY METHODS:
 * - findByOwnerId: gets all vehicles belonging to a specific owner.
 * - findByOwnerIdAndId: gets a specific vehicle, but only if it belongs to the given owner
 *   (security: prevents owners from accessing other owners' vehicles).
 * - findByAssignedCustomerId: finds vehicles assigned to a specific customer.
 * - findAllJoinFetchOwner: a custom JPQL query that eagerly loads the owner relationship
 *   to avoid N+1 query problems in the simulator.
 *
 * USED BY: OwnerController, CustomerController, VehicleJourneySimulator, VehicleService
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /** Get all vehicles owned by a specific owner (by owner_details.id) */
    List<Vehicle> findByOwnerId(Long ownerId);

    /**
     * Get a specific vehicle by ID, but only if it belongs to the given owner.
     * This is a security check: ensures an owner can only access their own vehicles.
     */
    Optional<Vehicle> findByOwnerIdAndId(Long ownerId, Long id);

    /** Get all vehicles currently assigned to a specific customer */
    List<Vehicle> findByAssignedCustomerId(Long customerId);

    /**
     * Custom JPQL query: load all vehicles with their owner data in a single query.
     * "JOIN FETCH" tells Hibernate to load the owner relationship immediately in the same SQL query,
     * instead of making a separate query for each vehicle's owner (N+1 problem).
     * This is crucial for the VehicleJourneySimulator which processes ALL vehicles every 3 seconds.
     */
    @Query("SELECT v FROM Vehicle v JOIN FETCH v.owner")
    List<Vehicle> findAllJoinFetchOwner();
}
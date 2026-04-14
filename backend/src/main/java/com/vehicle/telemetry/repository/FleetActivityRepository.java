package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.FleetActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * FleetActivityRepository - Database operations for fleet activity/alert logs.
 *
 * Provides queries for:
 * - Getting alerts for an owner (newest first, limited to 100)
 * - Getting alerts for a specific vehicle (used by customer dashboard)
 * - Deleting alerts when a vehicle is removed
 *
 * USED BY: FleetActivityService, OwnerController (indirectly)
 */
public interface FleetActivityRepository extends JpaRepository<FleetActivity, Long> {

    /**
     * Get the 100 most recent alerts for a specific owner (newest first).
     * "Top100" = LIMIT 100, "ByOwner_Id" = WHERE owner.id = ?, "OrderByCreatedAtDesc" = newest first.
     * The underscore in "Owner_Id" tells Spring to navigate the relationship: FleetActivity -> owner -> id.
     */
    List<FleetActivity> findTop100ByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    /**
     * Get the 50 most recent alerts for a specific vehicle (newest first).
     * Used by the customer dashboard to show alerts for their assigned vehicle.
     */
    List<FleetActivity> findTop50ByVehicle_IdOrderByCreatedAtDesc(Long vehicleId);

    /**
     * Delete all alert records for a specific vehicle.
     * Called when an owner deletes a vehicle - cleans up associated alert data.
     * @Modifying tells Spring this is a DELETE (not a SELECT) operation.
     */
    @Modifying
    @Query("DELETE FROM FleetActivity fa WHERE fa.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);
}
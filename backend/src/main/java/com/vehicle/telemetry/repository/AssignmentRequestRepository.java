package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.AssignmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * AssignmentRequestRepository - Database operations for customer-to-vehicle assignment requests.
 *
 * Provides queries for:
 * - Finding pending requests for an owner (to show in "Pending Assignment Requests" panel)
 * - Finding requests by a customer (to show pending status on "Available Vehicles" list)
 * - Checking for duplicate requests (prevent same customer requesting same vehicle twice)
 * - Deleting requests when a vehicle is removed
 *
 * USED BY: OwnerController (approve/reject), CustomerController (create request, view pending)
 */
public interface AssignmentRequestRepository extends JpaRepository<AssignmentRequest, Long> {

    /**
     * Find all requests for a specific owner with a given status (e.g., "PENDING"), newest first.
     * Used by the owner dashboard to show the list of pending assignment requests.
     * Spring generates this from the method name:
     *   SELECT * FROM assignment_requests WHERE owner_id = ? AND status = ? ORDER BY created_at DESC
     */
    List<AssignmentRequest> findByOwner_IdAndStatusOrderByCreatedAtDesc(Long ownerId, String status);

    /**
     * Find all requests by a specific customer, newest first.
     * Used by the customer dashboard to show their pending request status.
     */
    List<AssignmentRequest> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    /**
     * Check if a specific customer already has a request for a specific vehicle with a given status.
     * Used to prevent duplicate PENDING requests (a customer can't request the same vehicle twice).
     */
    Optional<AssignmentRequest> findByCustomer_IdAndVehicle_IdAndStatus(Long customerId, Long vehicleId, String status);

    /**
     * Delete all assignment requests for a specific vehicle.
     * Called when a vehicle is deleted - cleans up any pending/resolved requests.
     */
    @Modifying
    @Query("DELETE FROM AssignmentRequest ar WHERE ar.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);
}
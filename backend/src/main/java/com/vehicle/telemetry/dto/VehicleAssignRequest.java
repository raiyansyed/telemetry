package com.vehicle.telemetry.dto;

import lombok.Data;

/**
 * VehicleAssignRequest - The data sent when an owner manually assigns a vehicle to a customer.
 *
 * The owner selects a customer from the dropdown in the "Assign Vehicle" modal.
 * If the customer already has another vehicle, the owner can enable "swap" mode
 * which unassigns the old vehicle and assigns this new one.
 *
 * EXAMPLE JSON:
 *   { "customerUsername": "customer1", "swap": true }
 *
 * USED BY: POST /api/owner/vehicles/{id}/assign (OwnerController.assignVehicle())
 */
@Data
public class VehicleAssignRequest {
    /** The username of the customer to assign the vehicle to */
    private String customerUsername;
    /**
     * Whether to perform a "swap" assignment.
     * If true and the customer already has another vehicle:
     *   - The old vehicle is unassigned (set back to ACTIVE)
     *   - This vehicle is assigned to the customer (set to RENTED)
     * If false and the customer has another vehicle:
     *   - The request is rejected with an error message.
     */
    private Boolean swap;
}
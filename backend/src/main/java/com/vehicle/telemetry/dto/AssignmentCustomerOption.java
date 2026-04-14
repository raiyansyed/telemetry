package com.vehicle.telemetry.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AssignmentCustomerOption - Represents one customer in the "Assign Vehicle" dropdown.
 *
 * When an owner wants to assign a vehicle, the frontend shows a dropdown of eligible customers.
 * This DTO provides information about each customer, including whether they already have
 * a vehicle (for the "swap" feature).
 *
 * FILTERING RULES:
 * - Only customers registered in the same city as the vehicle are included.
 * - If swap mode is OFF: customers with existing vehicles are hidden from the dropdown.
 * - If swap mode is ON: all customers are shown, with info about their current vehicle.
 *
 * USED BY: GET /api/owner/vehicles/{id}/assignment-options (OwnerController)
 */
@Data
@Builder
public class AssignmentCustomerOption {
    /** The customer's username */
    private String username;
    /** Whether this customer currently has another vehicle assigned to them */
    private boolean hasOtherVehicle;
    /** The ID of the customer's current vehicle (null if they don't have one) */
    private Long otherVehicleId;
    /** The VIN of the customer's current vehicle (null if they don't have one) */
    private String otherVehicleVin;
}
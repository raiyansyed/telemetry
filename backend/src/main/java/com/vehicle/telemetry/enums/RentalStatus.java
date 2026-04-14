package com.vehicle.telemetry.enums;

/**
 * RentalStatus — Represents the state of a rental record.
 *
 * NOTE: The rental system is a legacy feature. The current app primarily uses
 * the "assignment" model (Vehicle.assignedCustomer) rather than Rental records.
 * However, the DatabaseSeedUtility still creates one sample rental for demo purposes.
 *
 * STATUSES:
 * - ACTIVE: The rental is currently ongoing (customer has the vehicle).
 * - COMPLETED: The rental period has ended and the vehicle was returned.
 * - CANCELLED: The rental was cancelled before completion.
 */
public enum RentalStatus {
    ACTIVE,     // Rental is currently in progress
    COMPLETED,  // Rental has ended normally
    CANCELLED   // Rental was cancelled
}

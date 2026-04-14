package com.vehicle.telemetry.dto;

import lombok.Data;

/**
 * VehicleRequest - The data sent when an owner adds a new vehicle to their fleet.
 *
 * The owner fills out the "Add Vehicle" modal in the frontend with VIN, make, model, and year.
 * Spring converts the JSON body into this DTO automatically.
 *
 * EXAMPLE JSON:
 *   { "vin": "TN04-FE-0006", "make": "Tesla", "model": "Model 3", "year": 2024 }
 *
 * USED BY: POST /api/owner/vehicles (OwnerController.addVehicle())
 */
@Data
public class VehicleRequest {
    /** Vehicle Identification Number - must be provided (validated in controller) */
    private String vin;
    /** Vehicle manufacturer (e.g., "Tesla") */
    private String make;
    /** Vehicle model name (e.g., "Model 3") */
    private String model;
    /** Manufacturing year (optional - defaults to 2024 if not provided) */
    private Integer year;
}
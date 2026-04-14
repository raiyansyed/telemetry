package com.vehicle.telemetry.dto;

import lombok.Builder;
import lombok.Data;

/**
 * FleetAlertResponse - The data sent to the Angular frontend for each alert/notification.
 *
 * This DTO converts FleetActivity entity records into a frontend-friendly format.
 * The frontend displays these in the "Fleet Alerts" panel (owner) or "Vehicle Alerts" panel (customer).
 *
 * Each alert has a colored left border based on type:
 * - "CRITICAL" = red border (dangerous speed/temperature)
 * - "WARNING" = yellow border (approaching limits)
 * - "INFO" = blue border (vehicle added, assigned, released, etc.)
 *
 * USED BY: GET /api/owner/alerts, GET /api/customer/alerts, and other alert endpoints.
 */
@Data
@Builder
public class FleetAlertResponse {
    /** Database ID of the FleetActivity record */
    private Long id;
    /** Human-readable alert message (e.g., "CRITICAL on TN04-FE-0001: speed 115 km/h") */
    private String message;
    /** Alert severity: "CRITICAL", "WARNING", or "INFO" */
    private String type;
    /** When the alert was triggered (ISO datetime string for frontend display) */
    private String triggeredAt;
    /** Whether the user has marked this alert as read */
    private boolean read;
    /** The vehicle's VIN for display purposes */
    private String licensePlate;
}
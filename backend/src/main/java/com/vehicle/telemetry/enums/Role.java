package com.vehicle.telemetry.enums;

/**
 * Role — Defines the two types of users in the system.
 *
 * HOW ENUMS WORK:
 * - An enum is a special Java type that represents a fixed set of constants.
 * - Instead of using strings like "OWNER" or "CUSTOMER" everywhere (which could have typos),
 *   we use Role.OWNER and Role.CUSTOMER for type safety.
 *
 * THE TWO ROLES:
 * - OWNER: A fleet owner who manages vehicles, views analytics, assigns drivers, and monitors alerts.
 *          They see the "Fleet Owner Dashboard" in the frontend.
 * - CUSTOMER: A driver/customer who gets assigned to a vehicle, can control it (accelerate/brake),
 *            and views their vehicle's live telemetry. They see the "My Vehicle Dashboard".
 *
 * WHERE IT'S USED:
 * - Stored in the User entity's "role" column in the database.
 * - Checked by Spring Security (RoleGuard) to control which API endpoints a user can access.
 * - Sent to the frontend in the JWT login response so Angular knows which dashboard to show.
 */
public enum Role {
    OWNER,      // Fleet owner — manages vehicles and views fleet-wide analytics
    CUSTOMER    // Driver/customer — drives an assigned vehicle and views personal telemetry
}

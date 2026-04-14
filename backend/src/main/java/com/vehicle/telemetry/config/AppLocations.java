package com.vehicle.telemetry.config;

import java.util.List;

/**
 * AppLocations — A configuration class that holds the list of cities supported by the platform.
 *
 * WHY THIS EXISTS:
 * - The system is location-aware: vehicles belong to a city, and customers can only see/request
 *   vehicles in their own city. This class is the single source of truth for all supported cities.
 * - The Angular frontend fetches this list via GET /api/auth/locations (see AuthController)
 *   so the dropdown menus in the UI always stay in sync with the backend.
 *
 * HOW IT'S USED:
 * - During user registration: the user picks a city from this list (or gets the default).
 * - During vehicle creation: the vehicle inherits the owner's city.
 * - During assignment: only customers in the same city as the vehicle are shown.
 *
 * NOTE: This is a "utility class" — it only has static fields and cannot be instantiated
 * (the constructor is private). It's not a Spring bean; it's just a holder for constants.
 */
public final class AppLocations {

    /** Private constructor prevents anyone from doing "new AppLocations()" — this is a utility class. */
    private AppLocations() {
    }

    /** The fallback city when a user doesn't specify one during registration. */
    public static final String DEFAULT_CITY = "Chennai";

    /**
     * The complete list of cities where the platform operates.
     * List.of() creates an immutable (unmodifiable) list — you can't add or remove cities at runtime.
     * To add a new city, add it here and restart the backend.
     * The Angular frontend fetches this list via the /api/auth/locations endpoint.
     */
    public static final List<String> SUPPORTED_CITIES = List.of(
            "Chennai",
            "Mumbai",
            "Delhi",
            "Bangalore",
            "Hyderabad",
            "Kolkata",
            "Pune",
            "Ahmedabad",
            "New York",
            "London"
    );
}

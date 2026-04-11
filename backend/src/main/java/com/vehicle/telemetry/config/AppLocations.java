package com.vehicle.telemetry.config;

import java.util.List;

/**
 * Canonical list of supported cities and the application default (Chennai).
 */
public final class AppLocations {

    private AppLocations() {
    }

    public static final String DEFAULT_CITY = "Chennai";

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

  package com.vehicle.telemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TelemetryApplication — The main entry point for the entire Spring Boot backend.
 *
 * HOW IT WORKS:
 * - @SpringBootApplication is a shortcut that combines three annotations:
 *     1. @Configuration  — Marks this class as a source of bean definitions (objects managed by Spring).
 *     2. @EnableAutoConfiguration — Tells Spring Boot to auto-configure based on libraries on classpath
 *        (e.g., it detects MySQL + JPA jars and sets up the database connection automatically).
 *     3. @ComponentScan — Scans this package (com.vehicle.telemetry) and all sub-packages for classes
 *        annotated with @Component, @Service, @Repository, @Controller, etc., and registers them.
 *
 * - @EnableScheduling — Enables Spring's scheduled task execution (e.g., @Scheduled methods).
 *
 * STARTUP SEQUENCE:
 *   1. Starts an embedded Tomcat web server on port 9090 (set in application.properties).
 *   2. Connects to the MySQL database (localhost:3306/newtestdb).
 *   3. Creates/updates database tables automatically (Hibernate ddl-auto=update).
 *   4. Runs CommandLineRunner beans in @Order:
 *      - Order(1): DatabaseSeedUtility — seeds demo users/vehicles if DB is empty.
 *      - Order(2): VehicleJourneySimulator — starts a background thread that generates telemetry every 3s.
 *   5. Begins accepting HTTP requests from the Angular frontend.
 */
@SpringBootApplication
@EnableScheduling
public class TelemetryApplication {

    /**
     * The main() method — Java's standard entry point.
     * SpringApplication.run() boots the entire application: starts the server, loads config,
     * initializes the database, and begins accepting API requests.
     *
     * @param args Command-line arguments (not used in this app).
     */
    public static void main(String[] args) {
        SpringApplication.run(TelemetryApplication.class, args);
    }
}

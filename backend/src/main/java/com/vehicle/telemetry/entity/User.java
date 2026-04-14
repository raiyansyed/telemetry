package com.vehicle.telemetry.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vehicle.telemetry.enums.Role;

/**
 * User - Represents a registered user in the system (maps to the "users" database table).
 *
 * WHAT IS AN ENTITY?
 * - An @Entity class is a Java class that maps directly to a database table.
 * - Each instance of this class = one row in the "users" table.
 * - Each field = one column in that table.
 * - JPA (Java Persistence API) handles converting between Java objects and SQL rows automatically.
 *
 * LOMBOK ANNOTATIONS (auto-generate boilerplate code at compile time):
 * - @Data: generates getters, setters, toString(), equals(), and hashCode() for all fields.
 * - @Builder: lets you do User.builder().username("owner").build() instead of using setters.
 * - @NoArgsConstructor: generates a no-argument constructor (required by JPA).
 * - @AllArgsConstructor: generates a constructor with all fields as parameters.
 *
 * TWO ROLES:
 * - OWNER: can manage vehicles, view fleet analytics, approve/reject assignment requests.
 * - CUSTOMER: can request vehicles, drive assigned vehicles, view personal telemetry.
 */
@Data                    // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor       // Lombok: generates User() constructor (required by JPA/Hibernate)
@AllArgsConstructor      // Lombok: generates User(id, username, password, role, location) constructor
@Builder                 // Lombok: enables User.builder().username("x").password("y").build()
@Entity                  // JPA: marks this class as a database entity (a table)
@Table(name = "users")   // JPA: specifies the table name in the database
public class User {

    /**
     * Primary key - auto-incremented unique ID for each user.
     * @GeneratedValue(IDENTITY) means MySQL generates this value automatically (AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The login username - must be unique across all users.
     * @Column(unique = true) adds a UNIQUE constraint in the database.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * The hashed password - stored using BCrypt encryption (never plain text).
     * BCrypt adds a random "salt" so even identical passwords produce different hashes.
     */
    @Column(nullable = false)
    private String password;

    /**
     * The user's role - either OWNER or CUSTOMER.
     * @Enumerated(STRING) stores it as text ("OWNER") instead of a number (0, 1).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * The city where the user is registered (e.g., "Chennai", "Mumbai").
     * Determines which vehicles they can see and interact with.
     * Owners: location is locked after registration.
     * Customers: can change location only when not assigned to a vehicle.
     */
    private String location;
}
package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.User;
import com.vehicle.telemetry.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Provides database operations for the User entity.
 *
 * WHAT IS A REPOSITORY?
 * - A repository is an interface (not a class!) that Spring Data JPA implements automatically.
 * - By extending JpaRepository<User, Long>, you get all standard CRUD methods for free:
 *   save(), findById(), findAll(), delete(), count(), etc.
 * - The <User, Long> means: this repository manages User entities, and their primary key is Long.
 *
 * CUSTOM QUERY METHODS:
 * - Spring Data JPA uses "method name conventions" to auto-generate SQL queries.
 * - findByUsername(String) -> SELECT * FROM users WHERE username = ?
 * - findByRoleAndLocationIgnoreCase(Role, String) -> SELECT * FROM users WHERE role = ? AND LOWER(location) = LOWER(?)
 * - You don't write any SQL! Spring generates it from the method name.
 *
 * USED BY: AuthController (login/register), OwnerController, CustomerController, UserController
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their exact username.
     * Returns Optional<User> because the user might not exist.
     * Optional is Java's way of handling "might be null" - you use .orElseThrow() or .orElse(null).
     */
    Optional<User> findByUsername(String username);

    /**
     * Find all users with a specific role in a specific city (case-insensitive location match).
     * Used by OwnerController to find eligible customers for vehicle assignment.
     * "IgnoreCase" at the end means the location comparison is case-insensitive.
     */
    List<User> findByRoleAndLocationIgnoreCase(Role role, String location);
}
package com.vehicle.telemetry.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * OwnerDetails - Extra profile information for users with the OWNER role.
 *
 * WHY A SEPARATE TABLE?
 * - Not all users are owners. Customers don't have a company name or fleet size.
 * - So instead of putting owner-specific fields in the User table (where they'd be NULL for customers),
 *   we use a separate table linked to User via a foreign key. This is called "table-per-role" design.
 *
 * RELATIONSHIP: One User (with role=OWNER) has exactly one OwnerDetails row.
 * - @OneToOne means one OwnerDetails links to exactly one User.
 * - @JoinColumn(name="user_id") creates a column in the owner_details table that stores the User's ID.
 * - CascadeType.ALL means saving OwnerDetails automatically saves the linked User too.
 *
 * HOW IT'S USED:
 * - Created during registration (AuthController) when role=OWNER.
 * - Used throughout the owner API to identify which vehicles, alerts, etc. belong to this owner.
 * - The fleetSize field is updated whenever vehicles are added or removed.
 */
@Entity
@Table(name = "owner_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDetails {

    /** Primary key - auto-generated unique ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Link to the User table - One-to-One relationship.
     * CascadeType.ALL means saving this entity also saves/updates the linked User.
     * @JoinColumn(name="user_id") creates a foreign key column "user_id" in owner_details table.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    /** The name of the fleet company (e.g., "Chennai Fleet Co."). Set during registration. */
    private String companyName;

    /**
     * The number of vehicles this owner currently has in their fleet.
     * Updated every time a vehicle is added or deleted (see OwnerController).
     */
    private int fleetSize;
}
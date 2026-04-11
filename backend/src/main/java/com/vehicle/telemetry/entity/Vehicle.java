package com.vehicle.telemetry.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vehicle.telemetry.enums.VehicleStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String vin;

    private String make;
    private String model;
    private Integer year;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private OwnerDetails owner;

    @ManyToOne
    @JoinColumn(name = "assigned_customer_id")
    private CustomerDetails assignedCustomer;

    private String imageUrl;

    /** Operating city; assignment dropdown filters customers in this location */
    private String location;
}

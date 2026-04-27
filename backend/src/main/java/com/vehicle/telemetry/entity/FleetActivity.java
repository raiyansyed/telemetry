package com.vehicle.telemetry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fleet_activities")
public class FleetActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private OwnerDetails owner;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private String vin;

    @Column(length = 1024, nullable = false)
    private String message;

    @Column(nullable = false, length = 32)
    private String alertType;

    @Column(name = "is_read", nullable = false)
    private boolean read;
}

package com.vehicle.telemetry.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vehicle.telemetry.enums.RentalStatus;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private RentalStatus status;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerDetails customer;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private OwnerDetails owner;
}

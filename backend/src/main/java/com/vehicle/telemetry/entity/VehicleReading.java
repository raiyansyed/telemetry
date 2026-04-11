package com.vehicle.telemetry.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vehicle.telemetry.enums.AlertLevel;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicle_readings")
public class VehicleReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    @Column(columnDefinition = "DOUBLE(10,2)")
    private Double speed;

    @Column(columnDefinition = "DOUBLE(10,2)")
    private Double temperature;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
}

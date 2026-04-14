package com.vehicle.telemetry.component;

import com.vehicle.telemetry.config.AppLocations;
import com.vehicle.telemetry.entity.*;
import com.vehicle.telemetry.enums.*;
import com.vehicle.telemetry.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Runs once on startup when the database has no users: seeds demo owners, customers,
 * vehicles (Chennai), and one active assignment so the UI is never empty on first boot.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class DatabaseSeedUtility implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OwnerDetailsRepository ownerDetailsRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String city = AppLocations.DEFAULT_CITY;

        User ownerUser = User.builder()
                .username("owner")
                .password(encoder.encode("password"))
                .role(Role.OWNER)
                .location(city)
                .build();

        User customerUser = User.builder()
                .username("customer")
                .password(encoder.encode("password"))
                .role(Role.CUSTOMER)
                .location(city)
                .build();

        User customer1 = User.builder()
                .username("customer1")
                .password(encoder.encode("password"))
                .role(Role.CUSTOMER)
                .location(city)
                .build();

        User customer2 = User.builder()
                .username("customer2")
                .password(encoder.encode("password"))
                .role(Role.CUSTOMER)
                .location(city)
                .build();

        User customer3 = User.builder()
                .username("customer3")
                .password(encoder.encode("password"))
                .role(Role.CUSTOMER)
                .location(city)
                .build();

        OwnerDetails ownerDetails = OwnerDetails.builder()
                .user(ownerUser)
                .companyName("Chennai Fleet Co.")
                .fleetSize(5)
                .build();
        ownerDetails = ownerDetailsRepository.save(ownerDetails);

        CustomerDetails customerDetails = CustomerDetails.builder()
                .user(customerUser)
                .licenseNumber("TN-01-AB-1234")
                .address("T. Nagar, " + city)
                .build();
        customerDetails = customerDetailsRepository.save(customerDetails);

        CustomerDetails cd1 = CustomerDetails.builder()
                .user(customer1)
                .licenseNumber("TN-01-XY-4321")
                .address("Velachery, " + city)
                .build();
        customerDetailsRepository.save(cd1);

        CustomerDetails cd2 = CustomerDetails.builder()
                .user(customer2)
                .licenseNumber("TN-02-CD-5678")
                .address("Adyar, " + city)
                .build();
        customerDetailsRepository.save(cd2);

        CustomerDetails cd3 = CustomerDetails.builder()
                .user(customer3)
                .licenseNumber("TN-03-EF-9012")
                .address("OMR, " + city)
                .build();
        customerDetailsRepository.save(cd3);

        User anynomoUser = User.builder()
                .username("anynomo")
                .password(encoder.encode("password"))
                .role(Role.CUSTOMER)
                .location(city)
                .build();

        CustomerDetails anynomoDetails = CustomerDetails.builder()
                .user(anynomoUser)
                .licenseNumber("TN-99-ZZ-9999")
                .address("Any Street, " + city)
                .build();
        customerDetailsRepository.save(anynomoDetails);

        String[][] vehicleData = {
                {"Toyota", "Camry", "2023"},
                {"Honda", "Civic", "2024"},
                {"Mahindra", "Thar", "2023"},
                {"BMW", "X5", "2023"},
                {"Mercedes", "C-Class", "2024"}
        };

        for (int i = 1; i <= 5; i++) {
            Vehicle vehicle = Vehicle.builder()
                    .vin("TN04-FE-000" + i)
                    .make(vehicleData[i - 1][0])
                    .model(vehicleData[i - 1][1])
                    .year(Integer.parseInt(vehicleData[i - 1][2]))
                    .status(i == 1 ? VehicleStatus.RENTED : VehicleStatus.ACTIVE)
                    .owner(ownerDetails)
                    .assignedCustomer(i == 1 ? customerDetails : null)
                    .location(city)
                    .build();
            vehicle = vehicleRepository.save(vehicle);

            if (i == 5) {
                Rental rental = Rental.builder()
                        .vehicle(vehicle)
                        .customer(anynomoDetails)
                        .owner(ownerDetails)
                        .startDate(LocalDateTime.now().minusDays(1))
                        .endDate(LocalDateTime.now().plusDays(2))
                        .status(RentalStatus.ACTIVE)
                        .build();
                rentalRepository.save(rental);
            }
        }

        System.out.println("DatabaseSeedUtility: seeded demo data (default location " + city + ").");
    }
}

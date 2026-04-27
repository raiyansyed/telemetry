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
                                .username("Kapoor")
                                .password(encoder.encode("password"))
                                .role(Role.OWNER)
                                .location(city)
                                .build();

                User customerUser = User.builder()
                                .username("Raiyan")
                                .password(encoder.encode("password"))
                                .role(Role.CUSTOMER)
                                .location(city)
                                .build();

                User customer1 = User.builder()
                                .username("Anvit")
                                .password(encoder.encode("password"))
                                .role(Role.CUSTOMER)
                                .location(city)
                                .build();

                User customer2 = User.builder()
                                .username("Rounak")
                                .password(encoder.encode("password"))
                                .role(Role.CUSTOMER)
                                .location(city)
                                .build();

                User customer3 = User.builder()
                                .username("Abhishek")
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
                                { "Hyundai", "Creta", "2018" },
                                { "Renault", "Triber", "2020" },
                                { "Maruti Suzuki", "WagonR", "2015" },
                                { "Honda", "Civic", "2024" },
                                { "Mahindra", "Thar", "2023" },
                                { "Hyundai", "i20", "2021" },
                                { "Maruti Suzuki", "Swift", "2016" }
                };

                for (int i = 1; i <= 7; i++) {
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

                // --- Seed one owner + 1-2 vehicles per non-default supported city ---
                String[][] citySeeds = {
                                // city, ownerName, companyName, vin1, make1, model1, year1, vin2, make2,
                                // model2, year2
                                { "Mumbai", "Deshmukh", "Mumbai Fleet Pvt.", "MH01-AB-0001", "Tata", "Nexon", "2022",
                                                "MH01-AB-0002", "Mahindra", "XUV700", "2023" },
                                { "Delhi", "Sharma", "Delhi DriveHub", "DL04-CD-0001", "Maruti Suzuki", "Baleno",
                                                "2021", "DL04-CD-0002", "Hyundai", "Venue", "2022" },
                                { "Bangalore", "Reddy", "Namma Fleet Co.", "KA01-EF-0001", "Toyota", "Innova", "2020",
                                                "KA01-EF-0002", "Kia", "Seltos", "2023" },
                                { "Hyderabad", "Rao", "Hyderabad Cabs Ltd.", "TS09-GH-0001", "Honda", "City", "2021",
                                                "TS09-GH-0002", "Tata", "Harrier", "2024" },
                                { "Kolkata", "Banerjee", "Kolkata Rides", "WB06-IJ-0001", "Maruti Suzuki", "Dzire",
                                                "2019", "", "", "", "" },
                                { "Pune", "Patil", "Pune AutoRent", "MH12-KL-0001", "Hyundai", "Verna", "2022",
                                                "MH12-KL-0002", "Tata", "Punch", "2023" },
                                { "Ahmedabad", "Patel", "Ahmedabad Fleet", "GJ01-MN-0001", "Maruti Suzuki", "Ertiga",
                                                "2021", "", "", "", "" },
                                { "New York", "Smith", "NYC Fleet Inc.", "NY-0001", "Tesla", "Model 3", "2023",
                                                "NY-0002", "Ford", "Mustang Mach-E", "2022" },
                                { "London", "Williams", "London Drive Ltd.", "LDN-0001", "Jaguar", "E-Pace", "2022",
                                                "LDN-0002", "Land Rover", "Defender", "2023" },
                };

                for (String[] seed : citySeeds) {
                        String seedCity = seed[0];

                        // Create owner user for this city
                        User cityOwnerUser = User.builder()
                                        .username(seed[1])
                                        .password(encoder.encode("password"))
                                        .role(Role.OWNER)
                                        .location(seedCity)
                                        .build();

                        OwnerDetails cityOwner = OwnerDetails.builder()
                                        .user(cityOwnerUser)
                                        .companyName(seed[2])
                                        .fleetSize(seed[7].isEmpty() ? 1 : 2)
                                        .build();
                        cityOwner = ownerDetailsRepository.save(cityOwner);

                        // Vehicle 1
                        Vehicle v1 = Vehicle.builder()
                                        .vin(seed[3])
                                        .make(seed[4])
                                        .model(seed[5])
                                        .year(Integer.parseInt(seed[6]))
                                        .status(VehicleStatus.ACTIVE)
                                        .owner(cityOwner)
                                        .location(seedCity)
                                        .build();
                        vehicleRepository.save(v1);

                        // Vehicle 2 (if present)
                        if (!seed[7].isEmpty()) {
                                Vehicle v2 = Vehicle.builder()
                                                .vin(seed[7])
                                                .make(seed[8])
                                                .model(seed[9])
                                                .year(Integer.parseInt(seed[10]))
                                                .status(VehicleStatus.ACTIVE)
                                                .owner(cityOwner)
                                                .location(seedCity)
                                                .build();
                                vehicleRepository.save(v2);
                        }
                }

                System.out.println("DatabaseSeedUtility: seeded demo data for all supported cities.");
        }
}

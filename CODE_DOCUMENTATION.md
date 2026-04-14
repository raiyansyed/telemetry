# Vehicle Telemetry System — Complete Code Documentation

> **Purpose of this document:** This is a line-by-line, file-by-file explanation of every piece of code in the project. It is written for someone who does NOT know Angular, TypeScript, Java, or Spring Boot. Every concept is explained from scratch.

---

## Table of Contents

1. [What This Project Does](#1-what-this-project-does)
2. [How The System Works (Big Picture)](#2-how-the-system-works-big-picture)
3. [Technology Stack Explained](#3-technology-stack-explained)
4. [Backend Documentation (Java / Spring Boot)](#4-backend-documentation)
   - [Configuration Files](#41-configuration-files)
   - [Main Application Entry Point](#42-main-application-entry-point)
   - [Enums (Fixed Value Types)](#43-enums)
   - [Entities (Database Tables)](#44-entities)
   - [DTOs (Data Transfer Objects)](#45-dtos)
   - [Repositories (Database Queries)](#46-repositories)
   - [Security (JWT Authentication)](#47-security)
   - [Services (Business Logic)](#48-services)
   - [Controllers (API Endpoints)](#49-controllers)
   - [Components (Startup & Simulation)](#410-components)
5. [Frontend Documentation (Angular / TypeScript)](#5-frontend-documentation)
   - [Configuration Files](#51-configuration-files)
   - [Entry Point & Global Styles](#52-entry-point--global-styles)
   - [App Root (Shell)](#53-app-root-shell)
   - [Core Services](#54-core-services)
   - [Login & Registration](#55-login--registration)
   - [Owner Dashboard](#56-owner-dashboard)
   - [Customer Dashboard](#57-customer-dashboard)
   - [Driver Dashboard](#58-driver-dashboard)
   - [Shared Components](#59-shared-components)
6. [How Everything Connects](#6-how-everything-connects)
7. [API Reference](#7-api-reference)

---

## 1. What This Project Does

This is a **Connected Vehicle Telemetry Monitoring System**. Think of it like a dashboard for a car rental company:

- A **Fleet Owner** registers, adds vehicles to their fleet, and monitors them in real-time (speed, temperature, GPS location)
- **Customers** can browse available vehicles, request one, and once assigned, can drive it (controlling speed via gas/brake buttons)
- The system generates **live telemetry data** every 3 seconds (simulating real car sensors)
- **Alerts** are triggered when a vehicle is speeding or overheating
- Everything runs in a web browser — the frontend talks to the backend through an API

---

## 2. How The System Works (Big Picture)

```
┌─────────────────────┐         HTTP Requests        ┌─────────────────────┐
│                     │  ──────────────────────────►  │                     │
│   Angular Frontend  │                               │  Spring Boot Backend │
│   (Web Browser)     │  ◄──────────────────────────  │  (Java Server)       │
│                     │         JSON Responses        │                     │
└─────────────────────┘                               └──────────┬──────────┘
                                                                 │
                                                                 │ SQL Queries
                                                                 ▼
                                                      ┌─────────────────────┐
                                                      │   MySQL Database     │
                                                      │   (Stores all data)  │
                                                      └─────────────────────┘
```

**Flow:**
1. User opens the website in their browser (Angular frontend)
2. They login → Angular sends username/password to the backend
3. Backend checks credentials, returns a **JWT token** (like a digital ID card)
4. Angular stores this token and includes it in every future request
5. Backend verifies the token, checks the user's role, and returns the right data
6. A background **simulator thread** generates fake car telemetry every 3 seconds

---

## 3. Technology Stack Explained

### Backend
| Technology | What It Is |
|-----------|-----------|
| **Java 17** | The programming language |
| **Spring Boot 3.2.4** | A framework that makes it easy to build web servers in Java |
| **Spring Security** | Handles login, passwords, and role-based access control |
| **Spring Data JPA** | Lets you interact with the database using Java objects instead of writing SQL |
| **MySQL** | The database that stores all data (users, vehicles, readings, etc.) |
| **JWT (JSON Web Token)** | A secure token system for authentication (replaces cookies/sessions) |
| **Lombok** | A Java library that auto-generates repetitive code (getters, setters, constructors) |
| **Maven** | The build tool — downloads dependencies and compiles the code |

### Frontend
| Technology | What It Is |
|-----------|-----------|
| **Angular 16** | A TypeScript framework for building web apps — handles routing, templates, data binding |
| **TypeScript** | A typed version of JavaScript — catches errors before you run the code |
| **Tailwind CSS** | A utility-first CSS framework — you style things by adding class names like `text-red-500` |
| **Chart.js + ng2-charts** | A chart library for rendering speed/temperature graphs |
| **RxJS** | A reactive programming library — handles async HTTP calls and event streams |

---

## 4. Backend Documentation

---

### 4.1 Configuration Files

---

#### `backend/pom.xml` — Maven Build Configuration

This file tells Maven (the build tool) everything about the project:

```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.4</version>
</parent>
```
- **What this does:** Says "this project is based on Spring Boot version 3.2.4". The parent POM provides default settings, dependency versions, and plugin configurations.

```xml
<properties>
    <java.version>17</java.version>
    <jjwt.version>0.11.5</jjwt.version>
</properties>
```
- **What this does:** Sets variables used throughout the file. Java 17 is the compiler target. `jjwt.version` is used for the JWT library version.

**Dependencies explained:**
| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-web` | Enables building REST APIs (handles HTTP requests/responses) |
| `spring-boot-starter-data-jpa` | Enables database access using Java objects (ORM) |
| `spring-boot-starter-security` | Adds authentication/authorization (login, role-checking) |
| `spring-boot-starter-validation` | Enables input validation with annotations |
| `spring-boot-starter-actuator` | Adds health check endpoints (e.g., `/actuator/health`) |
| `mysql-connector-j` | The JDBC driver that lets Java talk to MySQL |
| `lombok` | Auto-generates boilerplate code (getters, setters, builders, constructors) |
| `jjwt-api`, `jjwt-impl`, `jjwt-jackson` | The JWT library for creating and validating authentication tokens |

---

#### `backend/src/main/resources/application.properties` — App Settings

```properties
server.port=9090
```
- The backend server runs on port 9090 (so you access it at `http://localhost:9090`)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/newtestdb?createDatabaseIfNotExist=true...
spring.datasource.username=root
spring.datasource.password=root
```
- **Database connection:** Connects to MySQL running on the same machine, port 3306. The database name is `newtestdb`. If the database doesn't exist, it will be created automatically.

```properties
spring.jpa.hibernate.ddl-auto=update
```
- **Schema management:** `update` means Hibernate will automatically create or alter database tables to match the Java entity classes. It never drops existing data.

```properties
app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
app.jwt.expirationMs=86400000
```
- **JWT config:** The `secret` is a cryptographic key used to sign tokens (ensures nobody can forge a token). `86400000` milliseconds = 24 hours (how long a login session lasts).

---

### 4.2 Main Application Entry Point

#### `TelemetryApplication.java`

```java
@SpringBootApplication    // Combines 3 annotations: auto-configuration, component scanning, configuration
@EnableScheduling         // Allows scheduled tasks (cron jobs, periodic tasks)
public class TelemetryApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelemetryApplication.class, args);
        // This single line starts the entire application:
        // - Scans all packages for components/controllers/services
        // - Creates the Spring container (manages all objects)
        // - Starts the embedded web server (Tomcat) on port 9090
        // - Runs any CommandLineRunner beans (database seeding, simulator)
    }
}
```

---

### 4.3 Enums

Enums are fixed sets of values — like a dropdown with predefined choices.

#### `Role.java`
```java
public enum Role {
    OWNER,      // Fleet owner — manages vehicles, views analytics
    CUSTOMER    // Customer/driver — rents and drives vehicles
}
```

#### `VehicleStatus.java`
```java
public enum VehicleStatus {
    ACTIVE,       // Vehicle is available (nobody is using it)
    INACTIVE,     // Vehicle is disabled/offline
    MAINTENANCE,  // Vehicle is being serviced
    RENTED        // Vehicle is currently assigned to a customer
}
```

#### `AlertLevel.java`
```java
public enum AlertLevel {
    NONE,       // Everything is normal
    WARNING,    // Speed ≥ 70 km/h OR temperature ≥ 90°C
    CRITICAL    // Speed ≥ 110 km/h OR temperature ≥ 110°C
}
```

#### `RentalStatus.java`
```java
public enum RentalStatus {
    ACTIVE,      // Rental is currently ongoing
    COMPLETED,   // Rental has finished
    CANCELLED    // Rental was cancelled
}
```

---

### 4.4 Entities

Entities are Java classes that **map directly to database tables**. Each field becomes a column. Each object instance becomes a row.

---

#### `User.java` — The Users Table

```java
@Entity                           // Tells JPA "this class is a database table"
@Table(name = "users")            // The table name in MySQL
@Data                             // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor                // Lombok: creates an empty constructor: new User()
@AllArgsConstructor               // Lombok: creates a constructor with ALL fields
@Builder                          // Lombok: allows User.builder().username("bob").build() pattern
public class User {

    @Id                           // This field is the primary key (unique identifier)
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment: MySQL generates the ID
    private Long id;              // Column: id BIGINT AUTO_INCREMENT PRIMARY KEY

    @Column(unique = true, nullable = false)  // Column: username VARCHAR NOT NULL UNIQUE
    private String username;

    @Column(nullable = false)     // Column: password VARCHAR NOT NULL
    private String password;      // Stored as a BCrypt hash (not plain text!)

    @Enumerated(EnumType.STRING)  // Store the enum as a string ("OWNER" or "CUSTOMER"), not a number
    @Column(nullable = false)
    private Role role;            // Column: role VARCHAR NOT NULL

    private String location;      // Column: location VARCHAR (nullable) — the user's city
}
```

**What the resulting SQL table looks like:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    location VARCHAR(255)
);
```

---

#### `OwnerDetails.java` — Extra Info for Fleet Owners

```java
@Entity
@Table(name = "owner_details")
public class OwnerDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;     // e.g., "Chennai Fleet Co."
    private Integer fleetSize;      // Number of vehicles owned (auto-updated)

    @OneToOne(cascade = CascadeType.ALL)     // One OwnerDetails links to exactly one User
    @JoinColumn(name = "user_id", referencedColumnName = "id")  // Foreign key column
    private User user;
    // cascade = ALL means: if you save/delete this OwnerDetails, also save/delete the User
}
```

---

#### `CustomerDetails.java` — Extra Info for Customers

```java
@Entity
@Table(name = "customer_details")
public class CustomerDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licenseNumber;   // Driving license number
    private String address;         // Physical address

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;              // Links to the User record
}
```

---

#### `Vehicle.java` — The Vehicles Table

```java
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String vin;            // Vehicle Identification Number (unique per car)

    private String make;           // Manufacturer: "Toyota", "BMW", etc.
    private String model;          // Model name: "Camry", "X5", etc.
    private Integer year;          // Manufacturing year: 2023, 2024
    private String imageUrl;       // Optional image URL (not currently used)
    private String location;       // Operating city: "Chennai", "Mumbai", etc.

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;  // ACTIVE, RENTED, INACTIVE, MAINTENANCE

    @ManyToOne                     // Many vehicles can belong to one owner
    @JoinColumn(name = "owner_id")
    private OwnerDetails owner;    // Foreign key → owner_details table

    @ManyToOne                     // A vehicle can be assigned to one customer (or null)
    @JoinColumn(name = "assigned_customer_id")
    private CustomerDetails assignedCustomer;  // null = unassigned, otherwise = rented
}
```

**Key concept — `@ManyToOne`:** This creates a foreign key relationship. "Many vehicles can have one owner" means the `vehicles` table has an `owner_id` column pointing to `owner_details.id`.

---

#### `VehicleReading.java` — Telemetry Data (Sensor Readings)

```java
@Entity
@Table(name = "vehicle_readings")
public class VehicleReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;  // When this reading was recorded
    private Double speed;             // km/h
    private Double temperature;       // Engine temperature in °C
    private Double latitude;          // GPS latitude
    private Double longitude;         // GPS longitude

    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;    // NONE, WARNING, or CRITICAL

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;          // Which vehicle this reading belongs to
}
```
- A new reading is created every 3 seconds by the simulator for each rented vehicle.

---

#### `FleetActivity.java` — Alert / Activity Log

```java
@Entity
@Table(name = "fleet_activities")
public class FleetActivity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;  // When the event happened

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private OwnerDetails owner;       // Which owner this alert belongs to

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;          // Which vehicle triggered it (nullable)

    private String vin;               // VIN string for display even after vehicle deletion
    private String message;           // Human-readable: "CRITICAL on TN04-FE-0001: speed 115.0 km/h"
    private String alertType;         // "INFO", "WARNING", or "CRITICAL"

    @Column(name = "is_read", nullable = false)  // "read" is a MySQL reserved word, so we use "is_read"
    private boolean read;             // Has the owner seen this alert?
}
```

---

#### `AssignmentRequest.java` — Vehicle Request from Customer

```java
@Entity
@Table(name = "assignment_requests")
public class AssignmentRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "customer_id")
    private CustomerDetails customer;  // Who is requesting

    @ManyToOne @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;           // Which vehicle they want

    @ManyToOne @JoinColumn(name = "owner_id")
    private OwnerDetails owner;        // The vehicle's owner (who must approve/reject)

    private String status;             // "PENDING", "APPROVED", or "REJECTED"
    private LocalDateTime createdAt;   // When the request was submitted
    private LocalDateTime resolvedAt;  // When the owner approved/rejected (null if pending)
}
```

---

#### `Rental.java` — Legacy Rental Records

```java
@Entity
@Table(name = "rentals")
public class Rental {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private RentalStatus status;       // ACTIVE, COMPLETED, CANCELLED

    @ManyToOne @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne @JoinColumn(name = "customer_id")
    private CustomerDetails customer;

    @ManyToOne @JoinColumn(name = "owner_id")
    private OwnerDetails owner;
}
```
- This is a **legacy** entity. The system now uses `Vehicle.assignedCustomer` instead. One seed rental is created for demo purposes.

---

### 4.5 DTOs

DTOs (Data Transfer Objects) are simple data containers used to send/receive data through the API. They prevent exposing internal entity details to the outside world.

#### `AuthRequest.java` — Login Payload
```java
// What the frontend sends when the user clicks "Login"
private String username;    // e.g., "owner"
private String password;    // e.g., "password"
```

#### `AuthResponse.java` — Login Response
```java
// What the backend sends back after successful login
private String token;      // JWT token string (long encoded string)
private String role;       // "OWNER" or "CUSTOMER"
private String location;   // "Chennai", "Mumbai", etc.
private String username;   // "owner", "customer", etc.
```

#### `RegisterRequest.java` — Registration Payload
```java
private String username;
private String password;
private String role;           // "OWNER" or "CUSTOMER"
private String companyName;    // Only for OWNER role
private String licenseNumber;  // Only for CUSTOMER role
private String address;        // Only for CUSTOMER role
private String location;       // City selection
```

#### `FleetAnalytics.java` — Fleet Summary Numbers
```java
private Double averageSpeed;        // Average speed across all owner's vehicles
private Double averageTemperature;  // Average engine temperature
private Long vehicleCount;          // Total vehicles in the fleet
private Long activeRentals;         // How many are currently assigned to customers
```

#### `VehiclePeakSpeed.java` — Top Speed Record
```java
private Long vehicleId;                // Which vehicle
private String vin, make, model;       // Vehicle details
private Double peakSpeed;              // Highest speed recorded today
private Double avgTemperature;         // Temperature at peak speed moment
private Double latitude, longitude;    // GPS at peak speed moment
private String timestamp;              // When the peak happened
private String assignedDriverUsername;  // Who was driving (or null)
```

#### `FleetAlertResponse.java` — Alert Data
```java
private Long id;
private String message;        // "CRITICAL on TN04-FE-0001: speed 115.0 km/h"
private String type;           // "CRITICAL", "WARNING", or "INFO"
private String triggeredAt;    // ISO timestamp string
@JsonProperty("isRead")        // In JSON output, this field is called "isRead" (not "read")
private boolean read;          // Has it been marked as read?
private String licensePlate;   // VIN for display purposes
```

#### `VehicleControlRequest.java` — Driver Control Input
```java
private String action;     // "ACCELERATE", "BRAKE", or "IDLE"
private Double throttle;   // 0.0 to 1.0 (how hard to press gas/brake)
```

#### `AssignmentCustomerOption.java` — Customer Dropdown Item
```java
private String username;           // Customer's username
private Boolean hasOtherVehicle;   // Does this customer already have a vehicle?
private Long otherVehicleId;       // If yes, which vehicle?
private String otherVehicleVin;    // That vehicle's VIN
```

---

### 4.6 Repositories

Repositories are **interfaces** that Spring Data JPA implements automatically. You just declare the method name, and Spring generates the SQL query for you.

#### `UserRepository.java`
```java
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    // Spring auto-generates: SELECT * FROM users WHERE username = ?

    List<User> findByRoleAndLocationIgnoreCase(Role role, String location);
    // Spring auto-generates: SELECT * FROM users WHERE role = ? AND LOWER(location) = LOWER(?)
    // Used to find customers in the same city as a vehicle (for assignment dropdown)
}
```

**How Spring Data naming works:**
- `findBy` → SELECT ... WHERE
- `Username` → column name
- `And` → AND condition
- `IgnoreCase` → case-insensitive comparison

#### `VehicleRepository.java`
```java
Optional<Vehicle> findByOwnerIdAndId(Long ownerId, Long vehicleId);
// SELECT * FROM vehicles WHERE owner_id = ? AND id = ?
// Used to verify the owner actually owns this vehicle before any operation

List<Vehicle> findByAssignedCustomerId(Long customerId);
// SELECT * FROM vehicles WHERE assigned_customer_id = ?
// Used to find which vehicle a customer is currently using

@Query("SELECT v FROM Vehicle v JOIN FETCH v.owner")
List<Vehicle> findAllJoinFetchOwner();
// Custom JPQL query: loads all vehicles AND their owner details in ONE query
// JOIN FETCH prevents the "N+1 query problem" (avoids separate queries per vehicle)
```

#### `VehicleReadingRepository.java`
```java
List<VehicleReading> findTop20ByVehicleIdOrderByTimestampDesc(Long vehicleId);
// Last 20 readings for a vehicle, newest first

@Query("SELECT AVG(vr.speed) FROM VehicleReading vr WHERE vr.vehicle.owner.id = :ownerId")
Double getAverageSpeedByOwner(@Param("ownerId") Long ownerId);
// Calculates the average speed across ALL readings for ALL vehicles owned by this owner

@Modifying @Transactional
void deleteByVehicleId(Long vehicleId);
// Deletes all readings for a vehicle (used when deleting a vehicle)
// @Modifying = this query changes data (not just reads)
// @Transactional = wrap this in a database transaction (all-or-nothing)
```

---

### 4.7 Security

The security layer ensures:
1. Users must login to access the system
2. Only OWNERs can access owner endpoints
3. Only CUSTOMERs can access customer/driver endpoints
4. Every request carries a valid JWT token

---

#### `SecurityConfig.java` — Security Rules

```java
@Configuration           // This class provides Spring beans (configuration objects)
@EnableWebSecurity       // Activates Spring Security
@EnableMethodSecurity    // Allows @PreAuthorize on individual methods
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CORS = Cross-Origin Resource Sharing
            // Allows the Angular frontend (port 4200) to call the backend (port 9090)
            // Without this, the browser would block the requests

            .csrf(csrf -> csrf.disable())
            // CSRF protection is disabled because we use JWT tokens (stateless)
            // CSRF is only needed for cookie-based sessions

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()       // Login & register: anyone can access
                .requestMatchers("/api/user/**").authenticated()   // Profile: any logged-in user
                .requestMatchers("/api/owner/**").hasRole("OWNER") // Owner endpoints: OWNER role only
                .requestMatchers("/api/driver/**").hasRole("CUSTOMER") // Driver endpoints: CUSTOMER role
                .requestMatchers("/api/customer/**").hasRole("CUSTOMER") // Customer endpoints: CUSTOMER role
                .anyRequest().authenticated()                      // Everything else: must be logged in
            )

            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // STATELESS = no server-side sessions. Every request must carry its own JWT token.
            // This is important because it means the server doesn't store login state in memory.

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            // Run our JWT filter BEFORE Spring's default auth filter
            // This intercepts every request and checks for a valid JWT token

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:*");
        // Allow requests from any localhost port (4200, 4201, etc.)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setAllowCredentials(true);
        // ...
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // BCrypt is a one-way hash algorithm for passwords
        // "password" becomes "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
        // You can verify a password against the hash, but you can never reverse it
    }
}
```

---

#### `JwtService.java` — Token Creation & Validation

```java
@Service
public class JwtService {

    @Value("${app.jwt.secret}")       // Reads from application.properties
    private String secretKey;

    @Value("${app.jwt.expirationMs}") // 86400000 ms = 24 hours
    private long jwtExpiration;

    // Extract the username from a token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
        // The "subject" claim is where we store the username
    }

    // Create a new JWT token for a user
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())           // Who this token is for
                .setIssuedAt(new Date(System.currentTimeMillis()))  // When it was created
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))  // When it expires
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)  // Sign with our secret key
                .compact();                                          // Build the final string
        // Result: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvd25..." (a long encoded string)
    }

    // Check if a token is valid
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        // Valid = username matches AND token hasn't expired
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
        // Converts the secret string into a cryptographic key object
    }
}
```

---

#### `JwtAuthenticationFilter.java` — Request Interceptor

This runs on **every single HTTP request** before it reaches any controller:

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter = guaranteed to run exactly once per request

    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {

        final String authHeader = request.getHeader("Authorization");
        // Step 1: Look for the "Authorization" header in the HTTP request
        // Example: "Authorization: Bearer eyJhbGciOiJIUzI1..."

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
            // No token found → skip this filter, let the request continue
            // Public endpoints (/api/auth/**) will still work
            // Protected endpoints will get a 403 Forbidden
        }

        jwt = authHeader.substring(7);
        // Step 2: Extract the token (remove "Bearer " prefix)

        username = jwtService.extractUsername(jwt);
        // Step 3: Decode the token to get the username

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // Step 4: Load the user from the database

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Step 5: If token is valid, create an Authentication object
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                // Step 6: Store in SecurityContext — now Spring knows who this user is
                // Controllers can access this via Principal parameter
            }
        }

        filterChain.doFilter(request, response);
        // Step 7: Continue to the next filter / controller
    }
}
```

---

#### `CustomUserDetailsService.java` — Loading Users from Database

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                // If role is OWNER → authority is "ROLE_OWNER"
                // Spring Security's hasRole("OWNER") checks for "ROLE_OWNER"
        );
    }
}
```

---

### 4.8 Services

Services contain **business logic** — the rules and calculations of the application. Controllers call services, services call repositories.

---

#### `AlertService.java` — Speed/Temperature Threshold Checker

```java
@Service
public class AlertService {
    public AlertLevel evaluateAndGetAlertLevel(Double speed, Double temperature) {
        if (speed >= 110 || temperature >= 110) {
            return AlertLevel.CRITICAL;    // DANGER! Very fast or very hot
        } else if (speed >= 70 || temperature >= 90) {
            return AlertLevel.WARNING;     // Caution — approaching limits
        }
        return AlertLevel.NONE;            // Everything is normal
    }
}
```

---

#### `VehicleControlService.java` — Manual Driving Controls

This service manages **manual driving mode** — when a customer presses gas/brake buttons on the frontend:

```java
@Service
public class VehicleControlService {

    // In-memory maps (not in the database) — fast access for the simulator
    private final Map<Long, Double> throttleMap = new ConcurrentHashMap<>();
    // vehicleId → throttle value (-1.0 = full brake, 0 = idle, 1.0 = full gas)

    private final Map<Long, Boolean> manualControlMap = new ConcurrentHashMap<>();
    // vehicleId → true if someone is manually controlling this vehicle

    public void setThrottle(Long vehicleId, String action, Double throttleValue) {
        manualControlMap.put(vehicleId, true);  // Mark as manually controlled

        switch (action.toUpperCase()) {
            case "ACCELERATE":
                throttleMap.put(vehicleId, Math.min(1.0, Math.abs(throttleValue)));
                // Positive value = accelerating
                break;
            case "BRAKE":
                throttleMap.put(vehicleId, -Math.min(1.0, Math.abs(throttleValue)));
                // Negative value = braking
                break;
            case "IDLE":
                throttleMap.put(vehicleId, 0.0);  // No input
                break;
        }
    }

    // Called by the simulator every 3 seconds to calculate the next speed
    public double computeNextSpeed(Long vehicleId, double currentSpeed) {
        double throttle = getThrottle(vehicleId);

        if (throttle > 0) {
            // Accelerating: gain up to +5 km/h per tick at full throttle
            return currentSpeed + (throttle * 5.0);
        } else if (throttle < 0) {
            // Braking: lose up to -8 km/h per tick at full brake
            return currentSpeed + (throttle * 8.0);
        } else {
            // Idle: natural friction, lose 1 km/h per tick
            return currentSpeed - 1.0;
        }
        // Final result is clamped between 0 and 200 km/h
    }

    // Temperature is derived from speed (faster = hotter engine)
    public double computeTemperature(double speed) {
        return 70.0 + (speed * 0.35) + (Math.random() * 3 - 1.5);
        // Base 70°C + speed contribution + small random noise
    }

    // When customer switches back to "auto" mode
    public void clearManualControl(Long vehicleId) {
        manualControlMap.remove(vehicleId);
        throttleMap.remove(vehicleId);
    }
}
```

---

#### `VehicleService.java` — Fleet Analytics & Data Access

```java
@Service
public class VehicleService {

    // Calculate fleet-wide statistics for the owner dashboard
    public FleetAnalytics getFleetAnalytics(Long ownerId) {
        Double avgSpeed = vehicleReadingRepository.getAverageSpeedByOwner(ownerId);
        Double avgTemp = vehicleReadingRepository.getAverageTemperatureByOwner(ownerId);
        List<Vehicle> ownerVehicles = vehicleRepository.findByOwnerId(ownerId);

        // Count "active rentals" = vehicles that have an assigned customer
        long activeRentals = ownerVehicles.stream()
                .filter(v -> v.getAssignedCustomer() != null)
                .count();

        return FleetAnalytics.builder()
                .averageSpeed(avgSpeed != null ? formatToTwoDecimals(avgSpeed) : 0.0)
                .averageTemperature(avgTemp != null ? formatToTwoDecimals(avgTemp) : 0.0)
                .vehicleCount((long) ownerVehicles.size())
                .activeRentals(activeRentals)
                .build();
    }

    // Returns the top 5 fastest vehicles today for the "Peak Speed" section
    public List<VehiclePeakSpeed> getTopVehiclePeakSpeeds(Long ownerId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<VehicleReading> readings = vehicleReadingRepository.findReadingsByOwnerSince(ownerId, startOfDay);

        // Step 1: Group readings by vehicle, keep the one with the highest speed
        Map<Long, VehicleReading> peakByVehicle = new LinkedHashMap<>();
        for (VehicleReading r : readings) {
            Long vid = r.getVehicle().getId();
            if (!peakByVehicle.containsKey(vid) || r.getSpeed() > peakByVehicle.get(vid).getSpeed()) {
                peakByVehicle.put(vid, r);
            }
        }

        // Step 2: Sort by peak speed (highest first) and take top 5
        return peakByVehicle.values().stream()
                .sorted(Comparator.comparingDouble(VehicleReading::getSpeed).reversed())
                .limit(5)
                .map(r -> /* ... convert to VehiclePeakSpeed DTO ... */)
                .collect(Collectors.toList());
    }

    // Aggregates readings by hour for the detail modal chart
    public Map<String, Object> getHourlyAggregation(Long vehicleId) {
        // Gets all readings for today, groups them by hour (0-23),
        // calculates average speed and temperature per hour
        // Returns: { hours: [8, 9, 10], avgSpeeds: [45.2, 67.8, 55.1], avgTemps: [82.0, 91.5, 85.3] }
    }
}
```

---

#### `FleetActivityService.java` — Alert Logging & Retrieval

```java
@Service
public class FleetActivityService {

    // Log a new fleet activity/alert
    public void log(OwnerDetails owner, Vehicle vehicle, String vin, String message, String alertType) {
        // Creates a FleetActivity record and saves it to the database
        // Example: "Vehicle added: Toyota Camry (TN04-FE-0001)", type: "INFO"
        // Example: "CRITICAL on TN04-FE-0001: speed 115.0 km/h", type: "CRITICAL"
    }

    // logByIds() does the same thing but uses IDs instead of full objects
    // This is needed for the simulator thread which runs outside the normal request context
    // Using full objects from another thread can cause "LazyInitializationException"

    // Retrieve the 100 most recent alerts for an owner
    public List<FleetAlertResponse> listForOwner(Long ownerId) { ... }

    // Mark a specific alert as "read" (owner clicked the ✓ Read button)
    public void markRead(Long activityId, Long ownerId) {
        // Finds the activity, verifies it belongs to this owner, sets read = true
    }

    // Mark ALL alerts as read (owner clicked "Mark All as Read")
    public void markAllRead(Long ownerId) {
        // Finds all unread activities for this owner and sets them all to read = true
    }
}
```

---

### 4.9 Controllers

Controllers define the **API endpoints** — the URLs that the frontend calls. Each method handles one URL.

---

#### `AuthController.java` — Login, Register, Locations

```java
@RestController                    // This class handles HTTP requests and returns JSON
@RequestMapping("/api/auth")       // All URLs in this class start with /api/auth
public class AuthController {

    // GET /api/auth/locations → Returns the list of supported cities
    @GetMapping("/locations")
    public ResponseEntity<List<String>> supportedLocations() {
        return ResponseEntity.ok(AppLocations.SUPPORTED_CITIES);
        // Returns: ["Chennai", "Mumbai", "Delhi", "Bangalore", ...]
    }

    // POST /api/auth/login → Authenticates a user and returns a JWT token
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // Step 1: Verify username + password against the database
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        // If password is wrong, Spring throws an exception → 401 Unauthorized

        // Step 2: Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        // Step 3: Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);

        // Step 4: Return token + user info
        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())      // "OWNER" or "CUSTOMER"
                .location(user.getLocation())     // "Chennai"
                .username(user.getUsername())      // "owner"
                .build());
    }

    // POST /api/auth/register → Creates a new user account
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // Step 1: Check username isn't taken
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build();  // 400 Bad Request
        }

        // Step 2: Create User with hashed password
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))  // Hash the password!
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .location(request.getLocation() != null ? request.getLocation() : "Chennai")
                .build();
        user = userRepository.save(user);

        // Step 3: Create role-specific details
        if (role == Role.OWNER) {
            // Create OwnerDetails with company name and fleet size 0
            ownerDetailsRepository.save(OwnerDetails.builder().user(user).companyName(...).build());
        } else {
            // Create CustomerDetails with license number and address
            customerDetailsRepository.save(CustomerDetails.builder().user(user).licenseNumber(...).build());
        }

        // Step 4: Generate token and return
        return ResponseEntity.ok(AuthResponse.builder().token(jwtToken).role(...).build());
    }
}
```

---

#### `UserController.java` — Profile & Location

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    // GET /api/user/profile → Returns current user's profile
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Principal principal) {
        // Principal = the currently logged-in user (extracted from JWT)
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        boolean isAssigned = false;
        if (user.getRole() == Role.CUSTOMER) {
            // Check if this customer has any assigned vehicles
            var customer = customerDetailsRepository.findByUserId(user.getId()).orElse(null);
            if (customer != null) {
                isAssigned = !vehicleRepository.findByAssignedCustomerId(customer.getId()).isEmpty();
            }
        }

        return ResponseEntity.ok(Map.of(
            "username", user.getUsername(),
            "role", user.getRole().name(),
            "location", user.getLocation(),
            "isAssigned", isAssigned    // Frontend uses this to lock/unlock the location selector
        ));
    }

    // PUT /api/user/location → Update the user's city
    @PutMapping("/location")
    public ResponseEntity<?> updateLocation(@RequestBody Map<String, String> body, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        // RULE 1: Owners can NEVER change their location
        if (user.getRole() == Role.OWNER) {
            return ResponseEntity.badRequest().body(Map.of("error", "Owner location is locked"));
        }

        // RULE 2: Customers with an assigned vehicle can't change location
        if (user.getRole() == Role.CUSTOMER) {
            // ... check if they have assigned vehicles ...
            if (!assigned.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot change while assigned"));
            }
        }

        // OK to change
        user.setLocation(body.get("location"));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("location", location));
    }
}
```

---

#### `OwnerController.java` — Full Fleet Management

This is the largest controller (285+ lines). Key endpoints:

```java
@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    // GET /api/owner/analytics → Fleet summary numbers
    // Returns: { averageSpeed: 72.5, averageTemperature: 89.3, vehicleCount: 5, activeRentals: 2 }

    // GET /api/owner/vehicles → List all owner's vehicles
    // Returns: Array of Vehicle objects with all details

    // POST /api/owner/vehicles → Add a new vehicle
    @Transactional  // All database changes succeed or fail together
    public ResponseEntity<?> addVehicle(@RequestBody VehicleRequest request, Principal principal) {
        // 1. Verify the user is an owner
        // 2. Create a Vehicle object with the owner's location
        // 3. Save it to the database
        // 4. Update the owner's fleet size count
        // 5. Log an INFO activity: "Vehicle added: Toyota Camry (TN04-FE-0006)"
    }

    // DELETE /api/owner/vehicles/{id} → Delete a vehicle
    @Transactional
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, Principal principal) {
        // 1. Verify ownership
        // 2. Delete assignment requests for this vehicle
        // 3. Delete fleet activities for this vehicle
        // 4. Delete all telemetry readings for this vehicle
        // 5. Delete the vehicle itself
        // 6. Update fleet size
        // 7. Log an INFO activity: "Vehicle removed from fleet: TN04-FE-0001"
    }

    // POST /api/owner/vehicles/{id}/assign → Assign vehicle to a customer
    @Transactional
    public ResponseEntity<?> assignVehicle(@PathVariable Long id, @RequestBody VehicleAssignRequest request, ...) {
        // 1. Verify ownership
        // 2. Find the customer by username
        // 3. Check customer is in the same city as the vehicle
        // 4. If customer already has a vehicle:
        //    - If swap=false → reject with error message
        //    - If swap=true → unassign their current vehicle first
        // 5. Set vehicle.assignedCustomer = customer
        // 6. Set vehicle.status = RENTED
        // 7. Log activity
    }

    // POST /api/owner/vehicles/{id}/unassign → Remove assignment
    // GET /api/owner/vehicles/{id}/assignment-options → Eligible customers for this vehicle
    // GET /api/owner/peak-speeds → Top 5 fastest vehicles today
    // GET /api/owner/trends → Last 50 fleet readings for charts
    // GET /api/owner/alerts → All fleet alerts
    // PUT /api/owner/alerts/{id}/read → Mark one alert as read
    // PUT /api/owner/alerts/mark-all-read → Mark all alerts as read
    // GET /api/owner/assignment-requests → Pending customer requests
    // POST /api/owner/assignment-requests/{id}/approve → Approve a request
    // POST /api/owner/assignment-requests/{id}/reject → Reject a request

    // Helper: resolves the current user to their OwnerDetails record
    private OwnerDetails resolveOwner(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return ownerDetailsRepository.findByUserId(user.getId()).orElseThrow();
    }
}
```

---

#### `DriverController.java` — Vehicle Telemetry & Control

```java
@RestController
@RequestMapping("/api/driver")
public class DriverController {

    // GET /api/driver/vehicle/{id}/readings → Last 20 telemetry readings
    // GET /api/driver/vehicle/{id}/readings/latest → Most recent reading

    // POST /api/driver/vehicle/{id}/control → Send gas/brake command
    public ResponseEntity<?> controlVehicle(@PathVariable Long id, @RequestBody VehicleControlRequest request) {
        vehicleControlService.setThrottle(id, request.getAction(), request.getThrottle());
        // Updates the in-memory control state
        // The simulator will pick this up on its next 3-second tick
        return ResponseEntity.ok(Map.of("status", "ok", "action", request.getAction()));
    }
}
```

---

#### `CustomerController.java` — Customer Vehicle Interaction

```java
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    // GET /api/customer/assigned-vehicles → Vehicles assigned to this customer
    // GET /api/customer/assigned-vehicle/latest → Latest telemetry for assigned vehicle
    // GET /api/customer/available-vehicles → Unassigned vehicles in customer's city
    // GET /api/customer/alerts → Alerts for the customer's vehicle
    // GET /api/customer/pending-requests → Customer's pending assignment requests

    // POST /api/customer/request-vehicle/{vehicleId} → Request a specific vehicle
    public ResponseEntity<?> requestVehicle(@PathVariable Long vehicleId, Principal principal) {
        // Validations:
        // 1. Customer must not already have a vehicle assigned
        // 2. Vehicle must exist
        // 3. Vehicle must be in customer's location
        // 4. Vehicle must be unassigned (no current customer)
        // 5. No duplicate pending request
        // Creates an AssignmentRequest with status "PENDING"
    }

    // POST /api/customer/release-vehicle → Give back the assigned vehicle
    public ResponseEntity<?> releaseVehicle(Principal principal) {
        // 1. Find the customer's assigned vehicle
        // 2. Clear manual control state (stop driving)
        // 3. Set vehicle.assignedCustomer = null
        // 4. Set vehicle.status = ACTIVE
        // 5. Log activity: "Vehicle released by customer @customer"
    }

    // POST /api/customer/switch-to-auto → Switch from manual to auto driving
    public ResponseEntity<?> switchToAuto(Principal principal) {
        // Clears the manual control maps for this vehicle
        // The simulator will resume generating random speed/temp
    }
}
```

---

### 4.10 Components

Components are **startup tasks** that run when the application boots.

---

#### `DatabaseSeedUtility.java` — Demo Data Seeder

```java
@Component
@Order(1)  // Runs FIRST (before the simulator)
public class DatabaseSeedUtility implements CommandLineRunner {
    // CommandLineRunner = Spring calls run() after the app context is ready

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;  // Database already has data — don't seed again
        }

        // Creates demo users:
        // - "owner" (password: "password", role: OWNER, location: Chennai)
        // - "customer" (password: "password", role: CUSTOMER, location: Chennai)
        // - "customer1", "customer2", "customer3" (same pattern)
        // - "anynomo" (extra test customer)

        // Creates 5 vehicles (all in Chennai):
        // 1. Toyota Camry 2023 (VIN: TN04-FE-0001) → assigned to "customer", status: RENTED
        // 2. Honda Civic 2024 (VIN: TN04-FE-0002) → unassigned, status: ACTIVE
        // 3. Mahindra Thar 2023 (VIN: TN04-FE-0003) → unassigned, status: ACTIVE
        // 4. BMW X5 2023 (VIN: TN04-FE-0004) → unassigned, status: ACTIVE
        // 5. Mercedes C-Class 2024 (VIN: TN04-FE-0005) → unassigned, status: ACTIVE

        // Creates 1 legacy rental for vehicle 5 → "anynomo"
    }
}
```

---

#### `VehicleJourneySimulator.java` — Live Telemetry Generator

This is the **heart of the real-time system**. It runs in a background thread and generates fake sensor data:

```java
@Component
@Order(2)  // Runs SECOND (after seeding)
public class VehicleJourneySimulator implements CommandLineRunner {

    // Chennai center coordinates for GPS simulation
    private static final double DEFAULT_LAT = 13.0827;
    private static final double DEFAULT_LON = 80.2707;

    @Override
    public void run(String... args) {
        new Thread(() -> {
            Thread.sleep(5000);  // Wait 5 seconds for the app to fully start

            while (!Thread.currentThread().isInterrupted()) {
                // Step 1: Get ALL vehicles (with owners pre-loaded via JOIN FETCH)
                List<Vehicle> vehicles = vehicleService.findAllVehiclesForSimulation();

                // Step 2: Create initial state for any new vehicles
                for (Vehicle v : vehicles) {
                    vehicleStates.computeIfAbsent(v.getId(), id -> initialState());
                    // computeIfAbsent = only create state if one doesn't exist yet
                }

                // Step 3: Remove state for deleted vehicles
                vehicleStates.keySet().removeIf(id ->
                        vehicles.stream().noneMatch(ve -> ve.getId().equals(id)));

                // Step 4: Generate telemetry for each vehicle
                simulateTick(vehicles);

                Thread.sleep(3000);  // Wait 3 seconds before next tick
            }
        }, "vehicle-journey-simulator").start();
    }

    private void simulateTick(List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            VehicleState state = vehicleStates.get(vehicle.getId());
            boolean isRented = vehicle.getAssignedCustomer() != null;

            if (isRented) {
                // === RENTED VEHICLE: Full live telemetry ===

                if (vehicleControlService.isManualControl(vehicleId)) {
                    // Manual mode: use the customer's gas/brake input
                    state.speed = vehicleControlService.computeNextSpeed(vehicleId, state.speed);
                    state.temperature = vehicleControlService.computeTemperature(state.speed);
                } else {
                    // Auto mode: random speed changes (±5 km/h), clamped 0-130
                    state.speed = Math.max(0, Math.min(130, state.speed + (random * 10 - 5)));
                    state.temperature = Math.max(60, Math.min(120, state.temperature + (random * 4 - 2)));
                }

                // GPS drift: small random movement (simulates driving around Chennai)
                state.latitude += (random * 0.001 - 0.0005);
                state.longitude += (random * 0.001 - 0.0005);

            } else {
                // === UNASSIGNED VEHICLE: Parked, no movement ===
                state.speed = 0.0;
                state.temperature = 70.0;  // Idle engine temperature
            }

            // Evaluate alert level based on current speed and temperature
            AlertLevel level = alertService.evaluateAndGetAlertLevel(state.speed, state.temperature);

            // Save the reading to the database
            VehicleReading reading = VehicleReading.builder()
                    .timestamp(LocalDateTime.now())
                    .speed(state.speed)
                    .temperature(state.temperature)
                    .latitude(state.latitude)
                    .longitude(state.longitude)
                    .alertLevel(level)
                    .vehicle(vehicleRef)
                    .build();
            vehicleReadingRepository.save(reading);

            // Log alert ONLY when the alert level CHANGES (prevents spam)
            if (isRented && level != AlertLevel.NONE && state.lastTelemetryAlertLogged != level) {
                fleetActivityService.logByIds(owner.getId(), vehicleId, vin,
                    "CRITICAL on TN04-FE-0001: speed 115.0 km/h, engine 95.0 °C", "CRITICAL");
                state.lastTelemetryAlertLogged = level;
            }
        }
    }
}
```

---

## 5. Frontend Documentation

---

### 5.1 Configuration Files

#### `frontend/package.json` — NPM Dependencies

```json
{
  "name": "frontend",
  "version": "0.0.0",
  "scripts": {
    "ng": "ng",           // Run Angular CLI commands
    "start": "ng serve",  // Start development server (http://localhost:4200)
    "build": "ng build",  // Compile for production
    "test": "ng test"     // Run unit tests
  },
  "dependencies": {
    "@angular/core": "^16.2.0",    // Angular framework
    "chart.js": "^4.5.1",          // Chart rendering library
    "ng2-charts": "^5.0.4",        // Angular wrapper for Chart.js
    "rxjs": "~7.8.0",              // Reactive programming (async data streams)
    "zone.js": "~0.13.0"           // Angular's change detection system
  },
  "devDependencies": {
    "tailwindcss": "^3.4.19",      // Utility-first CSS framework
    "autoprefixer": "^10.4.21",    // Auto-adds browser prefixes to CSS
    "postcss": "^8.5.3"            // CSS transformation pipeline
  }
}
```

#### `frontend/tailwind.config.js` — Tailwind CSS Configuration

```javascript
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  // Tell Tailwind which files to scan for CSS classes
  // It removes unused classes from the final build (smaller file size)

  darkMode: ['selector', '[data-theme="dark"]'],
  // Dark mode is activated by adding data-theme="dark" to the <html> element
  // This is controlled by ThemeService

  theme: {
    extend: {
      colors: {
        card: 'var(--card)',      // Use CSS variables for colors
        muted: 'var(--muted)',    // This allows the theme to change all colors at once
        border: 'var(--border)',
      }
    }
  }
}
```

---

### 5.2 Entry Point & Global Styles

#### `src/main.ts` — Application Bootstrap

```typescript
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppModule } from './app/app.module';

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
// This is the very first code that runs
// It tells Angular: "Start the application using AppModule as the root"
```

#### `src/index.html` — The HTML Shell

```html
<html lang="en">
<head>
  <base href="/">
  <!-- base href tells Angular's router that all routes start from the root -->

  <link rel="icon" type="image/png" href="assets/logo.png">
  <!-- The favicon (small icon in the browser tab) -->

  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
  <!-- Loads the Inter font from Google Fonts -->
</head>
<body>
  <app-root></app-root>
  <!-- This is where Angular injects the entire application -->
  <!-- app-root is the selector defined in AppComponent -->
</body>
</html>
```

#### `src/styles.scss` — Global Styles & Theme Variables

```scss
@tailwind base;       // Tailwind's CSS reset (normalizes browser defaults)
@tailwind components; // Tailwind's component classes
@tailwind utilities;  // Tailwind's utility classes (text-lg, bg-blue-500, etc.)

// CSS Custom Properties (variables) — the color palette
:root {
  // Light theme (default)
  --bg: #f8f9fa;          // Page background (light gray)
  --card: #ffffff;        // Card/panel background (white)
  --text: #111111;        // Primary text color (near black)
  --muted: #6b7280;       // Secondary text (gray)
  --border: #e5e7eb;      // Border color (light gray)
  --accent: #2563eb;      // Accent color (blue)
}

[data-theme="dark"] {
  // Dark theme (applied when ThemeService toggles)
  --bg: #0a0a0a;          // Page background (near black)
  --card: #1a1a1a;        // Card background (dark gray)
  --text: #f5f5f5;        // Primary text (near white)
  --muted: #9ca3af;       // Secondary text (medium gray)
  --border: #2d2d2d;      // Border color (dark gray)
  --accent: #3b82f6;      // Accent color (blue)
}
```

**How themes work:** When the user clicks the theme toggle, `ThemeService` adds/removes `data-theme="dark"` on the `<html>` element. All CSS variables instantly change, and every element using `var(--bg)`, `var(--text)`, etc. updates automatically.

---

### 5.3 App Root (Shell)

#### `src/app/app.module.ts` — Root Module

```typescript
@NgModule({
  declarations: [
    AppComponent,        // The shell (navbar + router outlet)
    LoginComponent,      // Login page
    RegisterComponent    // Registration page
  ],
  imports: [
    BrowserModule,       // Required for any Angular browser app
    AppRoutingModule,    // The routing configuration
    HttpClientModule,    // Enables making HTTP requests to the backend
    FormsModule          // Enables [(ngModel)] two-way data binding in forms
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
    // Register AuthInterceptor as an HTTP interceptor
    // "multi: true" means it's added alongside other interceptors, not replacing them
    // This interceptor adds the JWT token to every outgoing HTTP request
  ],
  bootstrap: [AppComponent]  // The component Angular renders first
})
export class AppModule { }
```

#### `src/app/app-routing.module.ts` — URL Routes

```typescript
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  {
    path: 'owner',
    loadChildren: () => import('./owner/owner.module').then(m => m.OwnerModule),
    // LAZY LOADING: The owner module is only downloaded when someone navigates to /owner
    // This keeps the initial page load fast
    canActivate: [AuthGuard, RoleGuard],
    // Guards: must be logged in (AuthGuard) AND have OWNER role (RoleGuard)
    data: { role: 'OWNER' }  // RoleGuard reads this to know which role is required
  },

  {
    path: 'customer',
    loadChildren: () => import('./customer/customer.module').then(m => m.CustomerModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'CUSTOMER' }
  },

  { path: '**', redirectTo: 'login' }
  // Wildcard: any unknown URL redirects to the login page
];
```

#### `src/app/app.component.ts` — The Application Shell

```typescript
export class AppComponent implements OnInit {
  activeLocation = '';        // Currently selected city
  showLocationMenu = false;   // Is the location dropdown open?
  locations: string[] = [];   // List of supported cities (from backend)
  username = '';               // Currently logged-in username
  isLocationLocked = false;   // Can the user change their location?

  constructor(
    public themeService: ThemeService,  // Injected: theme toggle logic
    private router: Router,             // Injected: navigation
    private apiService: ApiService      // Injected: HTTP calls to backend
  ) {}

  // Check if user is logged in (is there a token in localStorage?)
  get isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
    // !! converts any value to boolean: null → false, "abc" → true
  }

  // Get the user's role from localStorage
  get userRole(): string {
    return localStorage.getItem('role') || '';
  }

  ngOnInit(): void {
    // OnInit = runs once when the component is first created

    // Load the list of supported cities from the backend
    this.apiService.getSupportedLocations().subscribe({
      next: (list) => { this.locations = list; },
      error: () => { this.locations = ['Chennai']; }  // Fallback if API fails
    });

    // Load username and location from localStorage
    this.hydrateFromLocalStorage();

    // If logged in, fetch the full profile from the backend
    if (this.isLoggedIn) {
      this.loadProfile();
    }

    // IMPORTANT: Re-check on every navigation event
    // This catches the moment after login when the page redirects
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe(() => {
      this.hydrateFromLocalStorage();
      if (this.isLoggedIn && !this.username) {
        this.loadProfile();
      }
    });
  }

  private loadProfile(): void {
    this.apiService.getUserProfile().subscribe({
      next: (profile) => {
        this.username = profile.username;
        this.activeLocation = profile.location;

        // Lock location for owners (always locked)
        if (this.userRole === 'OWNER') {
          this.isLocationLocked = true;
        }
        // Lock location for customers who have an assigned vehicle
        else if (this.userRole === 'CUSTOMER') {
          this.isLocationLocked = profile.isAssigned === true;
        }
      }
    });
  }

  // Called when user selects a new city
  changeLocation(location: string): void {
    if (this.isLocationLocked) return;  // Do nothing if locked
    this.activeLocation = location;
    this.showLocationMenu = false;
    localStorage.setItem('location', location);
    this.apiService.updateLocation(location).subscribe();
    // Updates both localStorage and the backend
  }

  logout(): void {
    // Clear everything from localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('location');
    localStorage.removeItem('username');
    // Reset component state
    this.activeLocation = '';
    this.username = '';
    this.isLocationLocked = false;
    // Navigate to login page
    this.router.navigate(['/login']);
  }
}
```

#### `src/app/app.component.html` — The Navbar Template

This is the **template** (HTML) that Angular renders. Key Angular syntax:

| Syntax | Meaning |
|--------|---------|
| `*ngIf="condition"` | Only show this element if condition is true |
| `*ngFor="let item of items"` | Repeat this element for each item in the array |
| `{{ expression }}` | Display a value (interpolation) |
| `(click)="method()"` | Call a method when clicked (event binding) |
| `[ngClass]="{ 'class': condition }"` | Add CSS class conditionally |
| `[(ngModel)]="property"` | Two-way binding: input ↔ property stay in sync |

The navbar contains:
- Logo image + title
- Location selector dropdown (📍)
- Username badge (👤)
- Role badge (OWNER/CUSTOMER)
- Theme toggle switch (🌙/☀️)
- Logout button
- `<router-outlet>` — where the current page's content is rendered

#### `src/app/theme.service.ts` — Dark/Light Theme Toggle

```typescript
@Injectable({ providedIn: 'root' })  // Singleton: one instance shared across the entire app
export class ThemeService {
  isDark = false;

  constructor() {
    // On startup, check if user previously chose dark mode
    const saved = localStorage.getItem('theme');
    if (saved === 'dark') {
      this.isDark = true;
      document.documentElement.setAttribute('data-theme', 'dark');
      // document.documentElement = the <html> element
    }
  }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    if (this.isDark) {
      document.documentElement.setAttribute('data-theme', 'dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme');
      localStorage.setItem('theme', 'light');
    }
  }
}
```

---

### 5.4 Core Services

#### `src/app/core/api.service.ts` — HTTP Client

This is the central service that makes ALL API calls to the backend:

```typescript
@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = 'http://localhost:9090/api';

  constructor(private http: HttpClient) {}
  // HttpClient is Angular's built-in HTTP library

  // === AUTH ===
  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, { username, password });
    // POST http://localhost:9090/api/auth/login with JSON body
    // Returns an Observable — you must .subscribe() to execute the request
  }

  // === OWNER ===
  getOwnerAnalytics(): Observable<FleetAnalytics> {
    return this.http.get<FleetAnalytics>(`${this.baseUrl}/owner/analytics`);
    // The AuthInterceptor automatically adds "Authorization: Bearer <token>"
  }

  ownerAddVehicle(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/owner/vehicles`, data);
  }

  ownerAssignVehicle(vehicleId: number, username: string, swap: boolean): Observable<any> {
    return this.http.post(`${this.baseUrl}/owner/vehicles/${vehicleId}/assign`,
      { customerUsername: username, swap });
  }

  // === CUSTOMER ===
  getCustomerAvailableVehicles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/customer/available-vehicles`);
  }

  requestVehicle(vehicleId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/customer/request-vehicle/${vehicleId}`, {});
  }

  // ... 30+ more methods covering every API endpoint
}
```

**Key TypeScript interfaces defined in this file:**

```typescript
export interface Vehicle {
  id: number;
  vin: string;
  make: string;
  model: string;
  year: number;
  status: string;
  location: string;
  assignedCustomer?: { user: { username: string } };
  // The ? means this field is optional (null for unassigned vehicles)
}

export interface Alert {
  id: number;
  message: string;
  type: string;              // 'CRITICAL' | 'WARNING' | 'INFO'
  triggeredAt: string;
  isRead: boolean;
  licensePlate?: string;
  isAssignmentRequest?: boolean;  // true for assignment request alerts
}
```

#### `src/app/core/auth.interceptor.ts` — JWT Token Injector

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');

    if (token) {
      // Clone the request and add the Authorization header
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
      // Every HTTP request now has: "Authorization: Bearer eyJhbGciOiJI..."
    }

    return next.handle(req).pipe(
      catchError(err => {
        if (err.status === 401) {
          // 401 = Unauthorized (token expired or invalid)
          localStorage.removeItem('token');
          this.router.navigate(['/login']);
          // Auto-redirect to login page
        }
        return throwError(() => err);
      })
    );
  }
}
```

#### `src/app/core/guards.ts` — Route Protection

```typescript
// AuthGuard: Is the user logged in?
export class AuthGuard implements CanActivate {
  canActivate(): boolean {
    if (localStorage.getItem('token')) {
      return true;   // Token exists → allow access
    }
    this.router.navigate(['/login']);
    return false;    // No token → redirect to login
  }
}

// RoleGuard: Does the user have the right role?
export class RoleGuard implements CanActivate {
  canActivate(route: ActivatedRouteSnapshot): boolean {
    const expectedRole = route.data['role'];
    // route.data['role'] comes from the route definition: data: { role: 'OWNER' }

    const userRole = localStorage.getItem('role');

    if (userRole === expectedRole) {
      return true;   // Role matches → allow access
    }
    this.router.navigate(['/login']);
    return false;    // Wrong role → redirect to login
  }
}
```

---

### 5.5 Login & Registration

#### `src/app/login/login.component.ts`

This is an **inline template** component (template is inside the .ts file, not a separate .html file):

```typescript
@Component({
  selector: 'app-login',
  template: `
    <!-- The login form -->
    <div class="min-h-screen flex items-center justify-center bg-[var(--bg)]">
      <div class="w-full max-w-md bg-[var(--card)] rounded-2xl shadow-xl p-8">

        <!-- Title -->
        <h2 class="text-2xl font-bold text-center">Vehicle Telemetry System</h2>

        <!-- Error message -->
        <div *ngIf="error" class="bg-red-50 text-red-600 p-3 rounded">{{ error }}</div>

        <!-- Username input -->
        <input [(ngModel)]="username" placeholder="Username" />
        <!-- [(ngModel)] = two-way binding: typing updates this.username, and vice versa -->

        <!-- Password input -->
        <input [(ngModel)]="password" type="password" placeholder="Password" />

        <!-- Login button -->
        <button (click)="login()">Sign In</button>

        <!-- Demo credential buttons -->
        <button (click)="username='owner'; password='password'">Use Owner Demo</button>
        <button (click)="username='customer'; password='password'">Use Customer Demo</button>
      </div>
    </div>
  `
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';

  login(): void {
    this.apiService.login(this.username, this.password).subscribe({
      next: (res: AuthResponse) => {
        // Store everything in localStorage for later use
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        localStorage.setItem('location', res.location || '');
        localStorage.setItem('username', res.username || '');

        // Redirect to the appropriate dashboard based on role
        this.router.navigate([`/${res.role.toLowerCase()}`]);
        // OWNER → /owner, CUSTOMER → /customer
      },
      error: () => {
        this.error = 'Invalid credentials';
      }
    });
  }
}
```

---

### 5.6 Owner Dashboard

#### `src/app/owner/owner.module.ts` — Lazy-Loaded Module

```typescript
@NgModule({
  declarations: [OwnerComponent],
  imports: [
    CommonModule,           // Provides *ngIf, *ngFor, pipes
    OwnerRoutingModule,     // Routes for this module
    NgChartsModule,         // Chart.js components
    SharedModule,           // GaugeComponent, AlertListComponent
    FormsModule             // [(ngModel)] support
  ]
})
export class OwnerModule { }
```

#### `src/app/owner/owner.component.ts` — Fleet Owner Logic

```typescript
export class OwnerComponent implements OnInit, OnDestroy {

  // === DATA PROPERTIES ===
  analytics: FleetAnalytics | null = null;  // Fleet summary numbers
  vehicles: Vehicle[] = [];                  // All vehicles in the fleet
  peakSpeeds: VehiclePeakSpeed[] = [];       // Top 5 fastest vehicles today
  fleetAlerts: Alert[] = [];                 // All fleet alerts/activities
  assignmentRequests: any[] = [];            // Pending customer requests

  // === MODAL STATE ===
  showDetailModal = false;              // Is the vehicle detail popup open?
  selectedVehicle: Vehicle | null = null;  // Which vehicle is selected?
  showAddModal = false;                 // Is the "Add Vehicle" modal open?
  showAssignModal = false;              // Is the "Assign Vehicle" modal open?

  // === CHART DATA ===
  speedChartData: any = { ... };        // Data for the speed trend chart
  tempChartData: any = { ... };         // Data for the temperature trend chart

  pollInterval: any;  // Reference to the 5-second polling timer

  ngOnInit(): void {
    this.username = localStorage.getItem('username') || '';
    this.loadData();               // Fetch all data immediately
    this.pollInterval = setInterval(() => this.loadData(), 5000);
    // Poll every 5 seconds for real-time updates
  }

  ngOnDestroy(): void {
    clearInterval(this.pollInterval);  // IMPORTANT: stop polling when leaving the page
    // Without this, the timer would keep running and waste resources
  }

  loadData(): void {
    // Makes 5 parallel API calls:
    this.apiService.getOwnerAnalytics().subscribe(data => this.analytics = data);
    this.apiService.getOwnerVehicles().subscribe(data => this.vehicles = data);
    this.apiService.getOwnerPeakSpeeds().subscribe(data => this.peakSpeeds = data);
    this.apiService.getOwnerTrends().subscribe(readings => this.updateCharts(readings));
    this.apiService.getOwnerAllAlerts().subscribe(data => this.fleetAlerts = data);
    this.apiService.getOwnerAssignmentRequests().subscribe(data => this.assignmentRequests = data);
  }

  // Combined alerts: merge fleet alerts + assignment requests into one list
  get combinedAlerts(): Alert[] {
    const requestAlerts: Alert[] = (this.assignmentRequests || []).map(req => ({
      id: req.id,
      message: `${req.customerUsername} is requesting: ${req.vehicleMake} ${req.vehicleModel} (${req.vehicleVin})`,
      type: 'INFO',
      triggeredAt: req.createdAt,
      isRead: false,
      licensePlate: req.vehicleVin,
      isAssignmentRequest: true  // Flag to show Approve/Reject buttons
    }));
    return [...requestAlerts, ...(this.fleetAlerts || [])]
      .sort((a, b) => new Date(b.triggeredAt).getTime() - new Date(a.triggeredAt).getTime());
    // Newest first
  }

  // Open the vehicle detail popup
  selectVehicle(v: Vehicle): void {
    this.selectedVehicle = v;
    this.showDetailModal = true;
    this.loadVehicleDetails(v);
    // Start polling this vehicle's data every 3 seconds
    this.detailPollInterval = setInterval(() => this.loadVehicleDetails(v), 3000);
  }

  // Add a new vehicle
  submitAddVehicle(): void {
    this.apiService.ownerAddVehicle(this.addForm).subscribe({
      next: () => {
        this.closeAddModal();
        this.loadData();  // Refresh the vehicle list
      },
      error: (err) => {
        this.addError = err.error?.message || 'Failed to add vehicle';
      }
    });
  }

  // Delete a vehicle (with browser confirmation dialog)
  deleteVehicle(v: Vehicle, event: Event): void {
    event.stopPropagation();  // Don't trigger the row click
    if (!confirm('Delete ' + v.vin + '? This removes all telemetry data.')) return;
    this.apiService.ownerDeleteVehicle(v.id).subscribe(() => this.loadData());
  }

  // Get the assigned customer's username from a vehicle object
  getAssignedUsername(v: Vehicle): string {
    return v.assignedCustomer?.user?.username || '';
    // ?. is the optional chaining operator
    // If v.assignedCustomer is null, it returns '' instead of crashing
  }
}
```

#### `src/app/owner/owner.component.html` — Owner Dashboard Template

The template is ~700 lines. Here are the major sections:

1. **Header:** Shows "🏢 Fleet Owner Dashboard" + "Welcome, {username}"
2. **Add Vehicle Modal:** Form with VIN, Make, Model, Year inputs
3. **Assign Vehicle Modal:** Dropdown of eligible customers + swap checkbox
4. **Assignment Requests:** Cards showing pending customer requests with Approve/Reject
5. **Vehicle Detail Popup:** Full-screen modal with live gauges, charts, GPS, stats
6. **Fleet Summary Cards:** 4 KPI cards (Total Vehicles, Active Rentals, Avg Speed, Avg Temp)
7. **Peak Speed Cards:** Top 5 fastest vehicles today as clickable cards
8. **Vehicles Table + Fleet Alerts:** Side-by-side layout with vehicle roster and alert list
9. **Trend Charts:** Speed and Temperature line charts

---

### 5.7 Customer Dashboard

#### `src/app/customer/customer.component.ts`

```typescript
export class CustomerComponent implements OnInit, OnDestroy {

  // === STATE ===
  hasVehicle = false;                    // Does the customer have an assigned vehicle?
  assignedVehicles: any[] = [];          // List of assigned vehicles (usually 0 or 1)
  availableVehicles: any[] = [];         // Unassigned vehicles in their city
  pendingRequestVehicleIds = new Set<number>();  // Vehicles already requested
  controlMode: 'auto' | 'manual' = 'auto';

  // === TELEMETRY ===
  speed = 0;
  temperature = 0;
  alertLevel = 'NONE';
  latitude = 0;
  longitude = 0;

  ngOnInit(): void {
    this.username = localStorage.getItem('username') || '';

    // Load assigned vehicles
    this.apiService.getCustomerAssignedVehicles().subscribe({
      next: (vehicles) => {
        this.assignedVehicles = vehicles;
        this.hasVehicle = vehicles.length > 0;
        if (this.hasVehicle) {
          this.vehicleId = vehicles[0].id;
          this.startPolling();  // Start live telemetry updates
        } else {
          this.loadAvailableVehicles();  // Show available vehicles instead
        }
      }
    });

    // Keyboard controls for driving
    this.keydownHandler = (event: KeyboardEvent) => {
      if (!this.hasVehicle) return;
      if (event.key === 'ArrowUp' || event.key === 'w') {
        this.gas();     // Accelerate
      } else if (event.key === 'ArrowDown' || event.key === 's') {
        this.brake();   // Brake
      }
    };
    window.addEventListener('keydown', this.keydownHandler);
  }

  gas(): void {
    this.controlMode = 'manual';
    this.apiService.controlVehicle(this.vehicleId, 'ACCELERATE', this.throttleValue)
      .subscribe();
    // Sends a POST to the backend to accelerate the vehicle
  }

  brake(): void {
    this.controlMode = 'manual';
    this.apiService.controlVehicle(this.vehicleId, 'BRAKE', this.throttleValue)
      .subscribe();
  }

  // Request assignment to a vehicle
  requestVehicle(vehicleId: number): void {
    this.apiService.requestVehicle(vehicleId).subscribe({
      next: () => {
        this.pendingRequestVehicleIds.add(vehicleId);
        // Mark this vehicle as "requested" in the UI
      }
    });
  }

  // Release the assigned vehicle (two-step confirmation)
  releaseStep = 0;  // 0 = not started, 1 = confirming, 2 = releasing
  initiateRelease(): void { this.releaseStep = 1; }
  cancelRelease(): void { this.releaseStep = 0; }
  confirmRelease(): void {
    this.releaseStep = 2;
    this.apiService.releaseVehicle().subscribe({
      next: () => {
        this.hasVehicle = false;
        this.assignedVehicles = [];
        this.loadAvailableVehicles();
      }
    });
  }

  // Poll for location changes (for unassigned customers)
  private _lastCheckedLocation = '';
  // Every second, check if the user changed their location in the navbar
  // If so, reload available vehicles for the new city
}
```

---

### 5.8 Driver Dashboard

#### `src/app/driver/driver.component.ts`

The driver dashboard is a simpler version of the customer cockpit:

```typescript
export class DriverComponent implements OnInit, OnDestroy {
  vehicleId = 1;  // Hardcoded to vehicle 1 (legacy, now mostly unused)
  speed = 0;
  temperature = 0;
  alertLevel = 'NONE';
  controlMode: 'auto' | 'manual' = 'auto';

  // Same gas/brake/polling logic as CustomerComponent
  // SVG speedometer with animated needle
  // Temperature bar gauge
  // Telemetry history chart (last 20 readings)
}
```

---

### 5.9 Shared Components

#### `src/app/shared/gauge.component.ts` — Circular Gauge

```typescript
@Component({
  selector: 'app-gauge',
  template: `
    <div class="relative w-40 h-40 flex items-center justify-center">
      <!-- Circular background using conic-gradient CSS -->
      <div class="absolute inset-0 rounded-full"
           [style.background]="'conic-gradient(' + color + ' ' + percentage + '%, #e5e7eb ' + percentage + '%)'">
      </div>
      <!-- Center hole (white circle) -->
      <div class="relative bg-[var(--card)] w-28 h-28 rounded-full flex flex-col items-center justify-center">
        <span class="text-2xl font-bold">{{ value }}</span>
        <span class="text-xs text-[var(--muted)]">{{ unit }}</span>
      </div>
    </div>
  `
})
export class GaugeComponent {
  @Input() value = 0;     // Current value (e.g., 85)
  @Input() max = 100;     // Maximum value (e.g., 200 for speed)
  @Input() unit = '';      // Unit label ("km/h", "°C")
  @Input() label = '';     // Gauge title ("Speed", "Engine Temp")
  @Input() type: 'speed' | 'temp' = 'speed';

  get percentage(): number {
    return Math.min(100, (this.value / this.max) * 100);
    // Convert value to a 0-100% scale
  }

  get color(): string {
    if (this.type === 'speed') {
      return this.percentage > 70 ? '#ef4444' : this.percentage > 50 ? '#f59e0b' : '#22c55e';
      // Red if high, yellow if medium, green if low
    }
    return this.percentage > 75 ? '#ef4444' : this.percentage > 50 ? '#f59e0b' : '#3b82f6';
  }
}
```

#### `src/app/shared/alert-list.component.ts` — Reusable Alert List

```typescript
@Component({
  selector: 'app-alert-list',
  template: `
    <!-- Empty state -->
    <div *ngIf="!alerts || alerts.length === 0">No alerts found</div>

    <!-- Mark All as Read button -->
    <button *ngIf="showMarkAllRead && hasUnread" (click)="markAllRead.emit()">
      ✓ Mark All as Read
    </button>

    <!-- Alert items -->
    <div *ngFor="let alert of alerts"
         [ngClass]="{
           'border-l-red-500': alert.type === 'CRITICAL',     // Red left border
           'border-l-yellow-500': alert.type === 'WARNING',   // Yellow left border
           'border-l-blue-500': alert.type === 'INFO',        // Blue left border
           'opacity-50': alert.isRead                          // Faded if already read
         }">
      <!-- Icon: 📋 for requests, 🔴/🟡/🔵 for alerts -->
      {{ alert.isAssignmentRequest ? '📋' : alert.type === 'CRITICAL' ? '🔴' : '🟡' }}

      <!-- Message -->
      {{ alert.message }}

      <!-- For assignment requests: Approve/Reject buttons -->
      <div *ngIf="alert.isAssignmentRequest">
        <button (click)="approve.emit(alert.id)">✓ Approve</button>
        <button (click)="reject.emit(alert.id)">✕ Reject</button>
      </div>

      <!-- For normal alerts: Mark as Read button -->
      <button *ngIf="!alert.isAssignmentRequest && !alert.isRead"
              (click)="markRead.emit(alert.id)">
        ✓ Read
      </button>
    </div>
  `
})
export class AlertListComponent {
  @Input() alerts: Alert[] = [];           // List of alerts to display
  @Input() showPlate: boolean = false;     // Show VIN/license plate?
  @Input() showMarkAllRead: boolean = false; // Show "Mark All as Read" button?
  @Output() markRead = new EventEmitter<number>();    // Emits alert ID when "Read" clicked
  @Output() markAllRead = new EventEmitter<void>();   // Emits when "Mark All" clicked
  @Output() approve = new EventEmitter<number>();     // Emits request ID when approved
  @Output() reject = new EventEmitter<number>();      // Emits request ID when rejected
}
```

**Angular concept — `@Input()` and `@Output()`:**
- `@Input()` = data flowing IN to the component (parent → child)
- `@Output()` = events flowing OUT of the component (child → parent)
- The parent template uses `[alerts]="myAlerts"` to pass data in, and `(markRead)="handleRead($event)"` to listen for events out.

---

## 6. How Everything Connects

### Login Flow
```
1. User types username + password → clicks "Sign In"
2. LoginComponent calls apiService.login(username, password)
3. AuthInterceptor doesn't add token (none exists yet)
4. Backend AuthController.login() validates credentials
5. Backend generates JWT token → returns { token, role, location, username }
6. LoginComponent stores everything in localStorage
7. Angular Router navigates to /owner or /customer
8. Route guard checks: token exists? ✓  Role matches? ✓  → Allow
9. Owner/Customer module loads (lazy loaded)
10. Dashboard component calls loadData() → makes API calls with token
```

### Real-Time Telemetry Flow
```
1. VehicleJourneySimulator runs in a background thread (every 3 seconds)
2. For each rented vehicle:
   - If manual control: uses VehicleControlService speed calculations
   - If auto: generates random speed/temp changes
3. Saves VehicleReading to database
4. If alert level changes: saves FleetActivity to database
5. Meanwhile, on the frontend:
   - Owner dashboard polls GET /api/owner/analytics every 5 seconds
   - Customer dashboard polls GET /api/customer/assigned-vehicle/latest every 2 seconds
6. Frontend receives new data → updates gauges, charts, alerts in real-time
```

### Vehicle Assignment Flow
```
1. Customer (unassigned) sees available vehicles in their city
2. Customer clicks "Request Assignment" on a vehicle
3. Backend creates AssignmentRequest with status = PENDING
4. Owner sees the request in "Pending Assignment Requests" section
5. Owner clicks "Approve" → backend assigns vehicle to customer
6. Vehicle status changes to RENTED
7. Customer's dashboard now shows the assigned vehicle
8. Simulator starts generating telemetry for this vehicle
9. Customer can now control the vehicle with gas/brake buttons
```

---

## 7. API Reference

### Public Endpoints (No Login Required)
| Method | URL | Purpose |
|--------|-----|---------|
| POST | `/api/auth/login` | Login → returns JWT token |
| POST | `/api/auth/register` | Register a new account |
| GET | `/api/auth/locations` | List of supported cities |

### User Endpoints (Any Logged-In User)
| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/api/user/profile` | Get username, role, location, isAssigned |
| PUT | `/api/user/location` | Change city (blocked for owners and assigned customers) |

### Owner Endpoints (OWNER Role Only)
| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/api/owner/analytics` | Fleet summary (avg speed, temp, counts) |
| GET | `/api/owner/vehicles` | List all vehicles |
| POST | `/api/owner/vehicles` | Add a new vehicle |
| DELETE | `/api/owner/vehicles/{id}` | Delete a vehicle + all data |
| GET | `/api/owner/vehicles/{id}/latest` | Latest telemetry reading |
| GET | `/api/owner/vehicles/{id}/hourly` | Today's hourly averages |
| GET | `/api/owner/vehicles/{id}/alerts` | Alerts for one vehicle |
| GET | `/api/owner/vehicles/{id}/assignment-options` | Eligible customers for assignment |
| POST | `/api/owner/vehicles/{id}/assign` | Assign vehicle to customer |
| POST | `/api/owner/vehicles/{id}/unassign` | Remove customer assignment |
| GET | `/api/owner/trends` | Last 50 readings for charts |
| GET | `/api/owner/peak-speeds` | Top 5 fastest vehicles today |
| GET | `/api/owner/alerts` | All fleet alerts |
| PUT | `/api/owner/alerts/{id}/read` | Mark alert as read |
| PUT | `/api/owner/alerts/mark-all-read` | Mark all alerts as read |
| GET | `/api/owner/assignment-requests` | Pending customer requests |
| POST | `/api/owner/assignment-requests/{id}/approve` | Approve a request |
| POST | `/api/owner/assignment-requests/{id}/reject` | Reject a request |

### Customer Endpoints (CUSTOMER Role Only)
| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/api/customer/rentals` | Legacy rental records |
| GET | `/api/customer/assigned-vehicles` | Vehicles assigned to customer |
| GET | `/api/customer/assigned-vehicle/latest` | Latest telemetry for assigned vehicle |
| POST | `/api/customer/release-vehicle` | Release assigned vehicle |
| POST | `/api/customer/request-vehicle/{id}` | Request assignment to a vehicle |
| GET | `/api/customer/available-vehicles` | Unassigned vehicles in customer's city |
| POST | `/api/customer/switch-to-auto` | Switch from manual to auto driving |
| GET | `/api/customer/alerts` | Alerts for customer's vehicle |
| PUT | `/api/customer/alerts/{id}/read` | Mark alert as read |
| GET | `/api/customer/pending-requests` | Customer's pending assignment requests |

### Driver Endpoints (CUSTOMER Role)
| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/api/driver/vehicle/{id}/readings` | Last 20 readings |
| GET | `/api/driver/vehicle/{id}/readings/latest` | Latest reading |
| POST | `/api/driver/vehicle/{id}/control` | Send gas/brake command |

---

## Demo Credentials

| Username | Password | Role | Location |
|----------|----------|------|----------|
| owner | password | OWNER | Chennai |
| customer | password | CUSTOMER | Chennai |
| customer1 | password | CUSTOMER | Chennai |
| customer2 | password | CUSTOMER | Chennai |
| customer3 | password | CUSTOMER | Chennai |
| anynomo | password | CUSTOMER | Chennai |

---

*End of documentation. Every concept, file, and line has been explained to make the codebase understandable to someone new to Angular, TypeScript, Java, and Spring Boot.*

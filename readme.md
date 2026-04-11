Plan: Connected Vehicle Telemetry Monitoring System — Full-Stack Architecture & Documentation

A comprehensive system spanning a Spring Boot 3.x (Java 17) backend with JWT-secured REST APIs, MySQL persistence via Spring Data JPA + Lombok, a two-level telemetry alerting engine, and a simulated vehicle journey runner — paired with an Angular 16+ frontend using ng2-charts and a MoviePulse-inspired Tailwind CSS light/dark theme, delivering role-guarded dashboards for Owner, Driver, and Customer personas.

________________________________________

Steps

1.	Scaffold the Spring Boot backend project — Initialize a Spring Boot 3.x (Java 17) Maven/Gradle project with dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, mysql-connector-j, lombok, jjwt (io.jsonwebtoken), and spring-boot-starter-validation. Define application.yml with MySQL datasource, JPA ddl-auto, and JWT secret/expiration config.

2.	Implement the MySQL-backed JPA entity layer — Create six @Entity classes under a model/ package, each annotated with Lombok (@Data, @NoArgsConstructor, @AllArgsConstructor, @Builder):

o	User — id, username, password, role (enum: OWNER, DRIVER, CUSTOMER), with @OneToOne mappings to OwnerDetails / CustomerDetails.

o	OwnerDetails — id, companyName, fleetSize, FK → User.

o	CustomerDetails — id, licenseNumber, address, FK → User.

o	Vehicle — id, vin, make, model, year, status (enum), FK → OwnerDetails; @OneToMany → VehicleReading, @OneToMany → Rental.

o	Rental — id, startDate, endDate, status, FKs → Vehicle, CustomerDetails, OwnerDetails.

o	VehicleReading — id, timestamp, speed (double, 2 dp), temperature (double, 2 dp), latitude, longitude, alertLevel (enum: NONE, WARNING, CRITICAL), FK → Vehicle.

Create matching repository/ interfaces extending JpaRepository for each entity.

3.	Build JWT authentication & role-based access control — Implement a SecurityConfig with SecurityFilterChain, a custom JwtAuthenticationFilter (extends OncePerRequestFilter), JwtService (token generation/validation), and AuthController (/api/auth/register, /api/auth/login). Enforce role guards: OWNER → /api/owner/**, DRIVER → /api/driver/**, CUSTOMER → /api/customer/**, using @PreAuthorize or requestMatchers(...).hasRole(...).

4.	Develop VehicleService with fleet analytics & two-level alerting — In service/VehicleService, implement:

o	Fleet analytics methods: getAverageSpeed(ownerId), getAverageTemperature(ownerId), getVehicleCount(ownerId), getActiveRentals(ownerId) — all returning values formatted to 2 decimal places.

o	Threshold alerting in a dedicated AlertService: on each VehicleReading save, evaluate — WARNING if speed ≥ 80 km/h OR temp ≥ 90°C; CRITICAL if speed ≥ 110 km/h OR temp ≥ 110°C — and persist the alertLevel on the reading.

o	Expose via OwnerController, DriverController, and CustomerController REST endpoints.

5.	Create CommandLineRunner journey simulator — Implement VehicleJourneySimulator implements CommandLineRunner that on startup: selects (or seeds) demo vehicles, runs a @Scheduled or looped thread every 2–5 seconds, generates randomized but realistic incremental speed (0–130 km/h), temperature (60–120°C), and latitude/longitude drift, saves each VehicleReading to the DB via the repository, evaluates alert thresholds, and prints a formatted console line per tick (e.g., [VIN: ABC123] Speed: 87.45 km/h | Temp: 91.20°C | Alert: ⚠ WARNING).

6.	Scaffold the Angular 16+ frontend — Initialize via ng new vehicle-telemetry --routing --style=scss, install ng2-charts + chart.js, tailwindcss (v3 for Angular 16 compatibility), @angular/cdk. Configure tailwind.config.js with a darkMode: 'attribute' strategy and define CSS custom properties mirroring MoviePulse's theme tokens (--bg: #f6f6f6, --text: #0f172a, --muted, --border, --card for light; inverted for [data-theme="dark"]). Add a ThemeService toggling data-theme on <html> and persisting to localStorage.

7.	Build role-guarded Angular dashboards with ng2-charts — Create three lazy-loaded feature modules behind an AuthGuard + RoleGuard:

o	Owner Dashboard (/owner) — Fleet summary cards (vehicle count, avg speed, avg temp), a <canvas baseChart> line chart (ng2-charts) for speed trends, a second line chart for temperature trends, and a vehicle management table. Use surface-card styling, responsive grid (grid-cols-1 md:grid-cols-2 lg:grid-cols-3).

o	Driver Dashboard (/driver) — Live telemetry panel showing current speed/temp (formatted XX.XX km/h / XX.XX °C), alert banners (yellow for WARNING, red for CRITICAL with pulse animation), and a mini speed/temp chart of last 20 readings.

o	Customer Dashboard (/customer) — Active rental card (vehicle info, rental dates, status badge), and a read-only map placeholder or vehicle location display.

All data fetched via an ApiService with HttpInterceptor injecting the JWT bearer token.

Further Considerations

1.	Real-time push vs. polling — The spec says "live telemetry." Should the Angular frontend poll /api/driver/readings?latest every few seconds, or should we add Spring WebSocket/SSE support for true push? Polling is simpler; SSE is more aligned with "live."

2.	Tailwind version alignment — MoviePulse uses Tailwind v4 (CSS-only config, bg-(--var) syntax) which is React/Vite-native. Angular 16 pairs better with Tailwind v3 (darkMode: 'attribute', tailwind.config.js). The plan assumes v3 with the same CSS-variable theme pattern adapted to v3 syntax (bg-[var(--card)]).

3.	Database seeding — Should the CommandLineRunner also seed demo Users, Owners, Customers, Vehicles, and Rentals for immediate demo, or should seeding be a separate data.sql / Flyway migration?
 
Dataset example 

peak speed today of top 5 vehicles        avg.Engine Temp       Vehicle gps location                                  timestamp
 
100kmh                                                                     100c                                    41.403 , 2.174  (long and lat)            10AM

i have this requirements help me build this please keep it simple and brief 
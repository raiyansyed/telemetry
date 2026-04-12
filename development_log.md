# Development & Architecture Log

This document serves as a comprehensive record of the implementation details and standard best practices employed across both the Spring Boot backend and the Angular frontend for the **Connected Vehicle Telemetry Monitoring System**.

---

## 1. Spring Boot Backend

### Project Structure
```
backend/src/main/java/com/vehicle/telemetry/
├── TelemetryApplication.java          — @SpringBootApplication + @EnableScheduling
├── entity/                            — JPA @Entity classes
│   ├── User.java                      — id, username, password, role, location
│   ├── OwnerDetails.java              — id, companyName, fleetSize, FK → User
│   ├── CustomerDetails.java           — id, licenseNumber, address, FK → User
│   ├── Vehicle.java                   — id, vin, make, model, year, status, imageUrl, location, FK → Owner, FK → AssignedCustomer
│   ├── Rental.java                    — id, startDate, endDate, status, FKs
│   ├── VehicleReading.java            — id, timestamp, speed, temperature, lat/lon, alertLevel, FK → Vehicle
│   ├── FleetActivity.java             — Owner fleet activity / alert log (message, type, read, optional FK → Vehicle)
│   └── AssignmentRequest.java         — id, customer, vehicle, owner, status (PENDING/APPROVED/REJECTED), createdAt, resolvedAt
├── enums/                             — Enum types (separate from entities for clarity)
│   ├── Role.java                      — OWNER, CUSTOMER
│   ├── AlertLevel.java                — NONE, WARNING, CRITICAL
│   ├── VehicleStatus.java             — ACTIVE, INACTIVE, MAINTENANCE, RENTED
│   └── RentalStatus.java              — ACTIVE, COMPLETED, CANCELLED
├── repository/                        — Spring Data JPA interfaces
│   ├── UserRepository.java            — + findByRoleAndLocationIgnoreCase (assignment dropdown)
│   ├── OwnerDetailsRepository.java
│   ├── CustomerDetailsRepository.java
│   ├── VehicleRepository.java         — findByAssignedCustomerId, findByOwnerIdAndId, findAllJoinFetchOwner
│   ├── RentalRepository.java
│   ├── VehicleReadingRepository.java  — Custom @Query for AVG speed/temp, peak speeds, hourly aggregation, deleteByVehicleId
│   ├── FleetActivityRepository.java   — Top activities by owner; deleteByVehicleId
│   └── AssignmentRequestRepository.java — Queries by owner/status, by customer/vehicle/status, deleteByVehicleId
├── dto/                               — Data Transfer Objects
│   ├── AuthRequest.java               — Login payload
│   ├── AuthResponse.java              — Token + role + location + username response
│   ├── RegisterRequest.java           — Registration payload (+ location field)
│   ├── FleetAnalytics.java            — avgSpeed, avgTemp, vehicleCount, activeRentals
│   ├── VehiclePeakSpeed.java          — + vehicleId; peak speed, GPS, timestamp, assignedDriverUsername
│   ├── FleetAlertResponse.java       — API shape for fleet alerts / activities (Jackson isRead)
│   ├── AssignmentCustomerOption.java — Username + hasOtherVehicle + otherVehicle* (swap UX)
│   ├── VehicleRequest.java            — [NEW] DTO for creating vehicles (vin, make, model, year)
│   └── VehicleAssignRequest.java      — customerUsername + optional swap (boolean)
├── security/                          — JWT + Spring Security
│   ├── JwtService.java                — Token generation/validation using JJWT
│   ├── JwtAuthenticationFilter.java   — OncePerRequestFilter extracting JWT from Authorization header
│   ├── SecurityConfig.java            — SecurityFilterChain, role-based requestMatchers, CORS (wildcard localhost)
│   └── CustomUserDetailsService.java  — UserDetailsService loading from DB
├── service/                           — Business logic
│   ├── VehicleService.java            — Fleet analytics, trend data, peak speeds (top 5), hourly aggregation, findAllVehiclesForSimulation (JOIN FETCH owner)
│   ├── FleetActivityService.java      — Persist & list fleet activities; logByIds for simulator thread
│   ├── VehicleControlService.java     — Manual vehicle control state (throttle/brake) + clearManualControl()
│   └── AlertService.java              — Two-level threshold evaluation (WARNING/CRITICAL)
├── controller/                        — REST endpoints
│   ├── AuthController.java            — login, register, GET /locations (supported cities; default Chennai)
│   ├── UserController.java            — [NEW] /api/user/profile (+ isAssigned) + /api/user/location (locked for owners; locked for assigned customers)
│   ├── OwnerController.java           — Full vehicle CRUD, assignment, detail views, alerts, mark-all-read, assignment request management
│   ├── DriverController.java          — /api/driver/vehicle/{id}/readings + /latest + /control
│   └── CustomerController.java        — /api/customer/rentals + /assigned-vehicles + /assigned-vehicle/latest + /release-vehicle + /request-vehicle + /available-vehicles + /switch-to-auto
├── config/
│   └── AppLocations.java              — DEFAULT_CITY (Chennai) + SUPPORTED_CITIES
└── component/                         — Startup + simulation
    ├── DatabaseSeedUtility.java       — CommandLineRunner @Order(1): seeds when DB has no users
    └── VehicleJourneySimulator.java   — CommandLineRunner @Order(2): background telemetry; picks up new vehicles each tick
```

### Actions Completed
1. **Scaffolded Application:** Spring Boot 3.2.4, Java 17, Maven.
2. **Domain Architecture:** Six @Entity classes separated into `entity/` package. Four enums isolated in `enums/` package.
3. **Configuration:** `application.properties` (not YAML) with MySQL datasource, JPA ddl-auto, and JWT secret/expiration.
4. **JWT Security:** `JwtService`, `JwtAuthenticationFilter`, role-based endpoint protection (OWNER/CUSTOMER).
5. **Auth Endpoints:** Both `/api/auth/login` AND `/api/auth/register` (register was missing — now added).
6. **Fleet Analytics:** `VehicleService` provides avg speed, avg temp, vehicle count, active rentals — all formatted to 2 decimal places.
7. **Two-Level Alerting:** `AlertService` evaluates WARNING (speed ≥ 80 OR temp ≥ 90) and CRITICAL (speed ≥ 110 OR temp ≥ 110).
8. **Peak Speed of Top 5:** New `/api/owner/peak-speeds` endpoint matching the dataset example in readme (peak speed, avg engine temp, GPS location, timestamp).
9. **Trend Data:** New `/api/owner/trends` endpoint returning the latest 50 fleet readings for real chart data.
10. **Journey Simulator:** `VehicleJourneySimulator` starts a daemon thread that writes telemetry every 3 seconds and **re-queries vehicles each cycle** so new vehicles are simulated immediately (GPS drift around Chennai).
11. **Data Seeding:** `DatabaseSeedUtility` (`@Order(1)`) runs only when `userRepository.count() == 0`, seeding Chennai-based demo users (including `customer2`, `customer3`), five vehicles with `location = Chennai`, one rental, and one assignment.

### Session 2 — New Backend Features (2026-04-11)

12. **Location Registration (User Entity):** Added `location` field to `User.java`. Users now register with a city/area name (e.g., "Mumbai", "Delhi"). The location is saved during registration and returned in the `AuthResponse` on both login and register.

13. **Location Switching (UserController):** Created new `UserController.java` with:
    - `GET /api/user/profile` — Returns logged-in user's username, role, and location.
    - `PUT /api/user/location` — Accepts `{ "location": "CityName" }` to update the user's active location. Accessible by all authenticated roles.

14. **Security Config Updated:** Added `/api/user/**` as `.authenticated()` in `SecurityConfig.java` so any logged-in user (OWNER or CUSTOMER) can access profile/location endpoints. Also updated CORS from fixed `http://localhost:4200` to `addAllowedOriginPattern("http://localhost:*")` to support any local dev port.

15. **Vehicle CRUD Endpoints (OwnerController):**
    - `POST /api/owner/vehicles` — Creates a new vehicle for the logged-in owner. Requires `VehicleRequest` DTO (vin, make, model, year). Automatically updates owner's `fleetSize`.
    - `DELETE /api/owner/vehicles/{id}` — Deletes a vehicle and all its telemetry readings. Updates fleet size.
    - `GET /api/owner/vehicles` — Lists all vehicles belonging to the owner.
    - `GET /api/owner/vehicles/{id}/latest` — Returns the latest telemetry reading for a specific vehicle.
    - `GET /api/owner/vehicles/{id}/hourly` — Returns hourly aggregated speed/temperature data for today (grouped by hour with averages).
    - `GET /api/owner/vehicles/{id}/alerts` — Returns alerts for a specific vehicle.
    - `GET /api/owner/alerts` — Returns all fleet-wide alerts.
    - `PUT /api/owner/alerts/{id}/read` — Marks an alert as read.

16. **Vehicle Assignment Feature (OwnerController):**
    - `POST /api/owner/vehicles/{id}/assign` — Assigns a vehicle to a customer by their username. Accepts `VehicleAssignRequest` DTO. Validates that the user exists and has CUSTOMER role. Sets vehicle status to RENTED.
    - `POST /api/owner/vehicles/{id}/unassign` — Removes the customer assignment from a vehicle. Resets status to ACTIVE.

17. **Vehicle Entity Updated:** Added `@ManyToOne assignedCustomer` (FK → CustomerDetails) and `imageUrl` field to `Vehicle.java`.

18. **Customer Assigned Vehicle Endpoints (CustomerController):**
    - `GET /api/customer/assigned-vehicles` — Returns all vehicles currently assigned to the logged-in customer.
    - `GET /api/customer/assigned-vehicle/latest` — Returns the latest telemetry reading for the customer's first assigned vehicle.

19. **VehicleService Enhanced:**
    - `getTopVehiclePeakSpeeds()` now includes `assignedDriverUsername` in each `VehiclePeakSpeed` result.
    - `getHourlyAggregation(vehicleId)` — New method that groups today's readings by hour, returning `{ hours: [], avgSpeeds: [], avgTemps: [] }` for chart rendering.
    - `getLatestReading(vehicleId)` — Single latest reading accessor.

20. **VehicleReadingRepository Enhanced:**
    - Added `findByVehicleIdSince(vehicleId, since)` for hourly aggregation queries.
    - Added `@Modifying @Transactional deleteByVehicleId(vehicleId)` for cascade-deleting readings when a vehicle is removed.

21. **New DTOs:**
    - `VehicleRequest.java` — vin, make, model, year (for creating vehicles).
    - `VehicleAssignRequest.java` — customerUsername (for assigning vehicles).

22. **VehiclePeakSpeed DTO Updated:** Added `assignedDriverUsername` field so the owner's Top 5 table can display who is driving each vehicle.

23. **AuthResponse DTO Updated:** Added `location` field so the frontend stores the user's location on login/register.

24. **RegisterRequest DTO Updated:** Added `location` field to accept location during registration.

25. **Demo seed data (superseded in Session 3 by `DatabaseSeedUtility`):** Earlier iterations used Mumbai; Session 3 standardizes Chennai and adds extra demo customers (see Session 3 changelog).

26. **VehicleRepository Updated:** Added `findByAssignedCustomerId(Long)` and `findByOwnerIdAndId(Long, Long)` query methods.

### Session 3 — Chennai default, dynamic UI data, fleet activities, assignment rules (2026-04-11)

27. **DatabaseSeedUtility (replaces DataSeeder):** `DataSeeder.java` removed. `DatabaseSeedUtility` seeds only when the user table is empty; default geography is **Chennai**; additional demo customers `customer2` and `customer3` (same password) for assignment testing.

28. **AppLocations:** `DEFAULT_CITY = Chennai`, `SUPPORTED_CITIES` list; `GET /api/auth/locations` returns the list (public, under `/api/auth/**`). Registration defaults blank location to Chennai.

29. **Vehicle.location:** Each vehicle stores an operating city; new vehicles inherit the logged-in owner’s `User.location` (fallback Chennai). Assignment requires the customer’s `User.location` to match the vehicle’s location (case-insensitive).

30. **One customer, one vehicle:** `POST /api/owner/vehicles/{id}/assign` rejects customers who already have another vehicle unless `swap: true`, in which case the prior vehicle is unassigned first and activities are logged.

31. **GET /api/owner/vehicles/{id}/assignment-options:** Returns `AssignmentCustomerOption` rows (username, `hasOtherVehicle`, other vehicle id/vin) for customers in the vehicle’s location — powers the owner assign dropdown.

32. **FleetActivity + FleetActivityService:** Persists operational and telemetry-threshold events (types `INFO`, `WARNING`, `CRITICAL`). `GET /api/owner/alerts` and vehicle-scoped alerts return `FleetAlertResponse`; `PUT /api/owner/alerts/{id}/read` marks rows read. `logByIds` supports the simulator thread without lazy-loading issues.

33. **VehicleJourneySimulator:** Each 3s cycle calls `VehicleService.findAllVehiclesForSimulation()` (`JOIN FETCH` owner), merges new vehicle ids into in-memory state, drops removed ids, uses `getReferenceById` when saving readings. Logs a fleet activity when an alert **level changes** to WARNING or CRITICAL.

34. **VehiclePeakSpeed:** DTO includes `vehicleId` so the frontend can open the correct detail modal from Top 5.

35. **OwnerController:** Vehicle create/delete/assign/unassign all append `INFO` activities; delete removes prior activity rows tied to that vehicle id, then readings, then the vehicle, then logs removal.

### Session 4 — Owner action reliability + alerts fix (2026-04-11)

36. **Root-cause fixed (MySQL reserved keyword):** `FleetActivity.read` generated DDL with column name `read`, which failed on MySQL (`create table fleet_activities ... read bit not null ...`). As a result, `fleet_activities` was never created and owner alert logging failed at runtime.

37. **Schema mapping fix:** Updated `FleetActivity.java` to map read status as `@Column(name = "is_read", nullable = false)`. After restart, Hibernate successfully created `fleet_activities` and foreign keys.

38. **Owner action atomicity:** Added `@Transactional` to owner write endpoints in `OwnerController` (`POST /vehicles`, `DELETE /vehicles/{id}`, `POST /vehicles/{id}/assign`, `POST /vehicles/{id}/unassign`) so business updates and activity logging commit/rollback together.

39. **User-facing impact resolved:**
    - Add vehicle now returns successful responses instead of failing after partially saving.
    - Assignment/reassignment no longer shows false failure while still changing data.
    - `GET /api/owner/alerts` now returns persisted fleet activity rows (INFO/WARNING/CRITICAL) once actions/simulator produce events.

40. **Code health cleanup:** Suppressed an IDE null-analysis false-positive in `OwnerController.addVehicle()` after the transaction fixes, so local compile/problem reporting is clean.

### Best Practices Applied
- **Layered Architecture:** Controller → Service → Repository separation.
- **Constructor Injection:** Via Lombok `@RequiredArgsConstructor` with `final` fields (immutable, testable).
- **DTO Pattern:** `AuthRequest/AuthResponse/FleetAnalytics/VehiclePeakSpeed/RegisterRequest/VehicleRequest/VehicleAssignRequest` prevent entity leakage to the API layer.
- **Stateless Sessions:** JWT-only authentication, `SessionCreationPolicy.STATELESS`.
- **Proper Package Organization:** `entity/`, `enums/`, `dto/`, `repository/`, `service/`, `controller/`, `security/`, `component/`.
- **CORS Configuration:** Wildcard localhost pattern `http://localhost:*` for flexible dev server ports.
- **Transactional Deletes:** `@Modifying @Transactional` on delete queries for data integrity.
- **Owner Verification:** All owner endpoints resolve the owner from the JWT principal and verify vehicle ownership before operations.

---

## 2. Angular Frontend

### Project Structure
```
frontend/src/app/
├── app.module.ts                    — Root module: declares AppComponent + LoginComponent
├── app-routing.module.ts            — Routes with AuthGuard + RoleGuard on all dashboards
├── app.component.ts/html            — Shell layout: navbar + router-outlet + location selector dropdown
├── theme.service.ts                 — data-theme toggling + localStorage persistence
├── core/
│   ├── api.service.ts               — HttpClient service with TypeScript interfaces (Vehicle, VehiclePeakSpeed, etc.)
│   ├── auth.interceptor.ts          — Attaches JWT Bearer token to all requests
│   └── guards.ts                    — AuthGuard (checks token) + RoleGuard (checks role)
├── login/
│   ├── login.component.ts           — Login page with demo credentials + stores location on login
│   └── register.component.ts        — Registration form with location dropdown
├── owner/
│   ├── owner.module.ts              — Lazy-loaded module with NgChartsModule
│   ├── owner.component.ts           — Fleet analytics, vehicle CRUD, assignment, detail popup
│   └── owner.component.html         — Peak speed cards, vehicle roster, modals, Owner Fleet Alerts
└── customer/
    ├── customer.module.ts           — Lazy-loaded module
    ├── customer.component.ts        — Fetches assigned vehicles + rentals, live telemetry, vehicle controls
    └── customer.component.html      — Assigned vehicle banner, speedometer, controls, charts, alerts
```

### Actions Completed
1. **App Name Fixed:** Changed from "MoviePulse Telemetry" → "Vehicle Telemetry System" everywhere (navbar, login page, HTML title).
2. **Proper Login Page:** Created dedicated `LoginComponent` as its own route (`/login`) instead of inline in AppComponent.
3. **AuthGuard + RoleGuard:** Route guards now protect all dashboard routes — prevents unauthorized access.
4. **Role Persistence:** User's role is saved to localStorage on login, enabling proper redirect on page refresh.
5. **Owner Dashboard — Real Data:** Charts now fetch from `/api/owner/trends` instead of hardcoded dummy arrays.
6. **Peak Speed Table:** New section matching the readme dataset example — "peak speed today of top 5 vehicles" with avg engine temp, GPS coordinates, and timestamp.
7. **TypeScript Interfaces:** `ApiService` now uses proper typed interfaces (`AuthResponse`, `FleetAnalytics`, `VehiclePeakSpeed`, etc.) instead of `any`.
8. **Driver GPS Panel:** Added current GPS position display showing lat/lon from the latest reading.
9. **Polling:** Owner dashboard polls every 5s, Driver dashboard polls every 3s for live feel.
10. **Theme System:** CSS custom properties with `[data-theme="dark"]` strategy, Inter font from Google Fonts.

### Session 2 — New Frontend Features (2026-04-11)

11. **Location Registration (Register Component):** Added a location dropdown to the registration form with 10 cities (Mumbai, Delhi, Bangalore, Hyderabad, Chennai, Kolkata, Pune, Ahmedabad, New York, London). Location is sent in the registration payload and stored in localStorage on success.

12. **Location Storage on Login:** `LoginComponent` now stores the `location` field from `AuthResponse` into localStorage on successful login.

13. **Location Selector in Navbar (AppComponent):** Added a `📍 Location` dropdown button in the navbar (visible when logged in). Users can click to open a dropdown menu listing all available cities, and select a new active location. The selection:
    - Updates localStorage immediately.
    - Calls `PUT /api/user/location` to persist the change on the backend.
    - Shows the currently active location in the button.
    - Includes a click-outside overlay to close the dropdown.

14. **ApiService — New Methods & Interfaces:**
    - `Vehicle` interface updated: includes `assignedCustomer` object (with nested `user`) and `imageUrl`.
    - `AuthResponse` interface updated: includes `location` field.
    - `VehiclePeakSpeed` interface updated: includes `assignedDriverUsername` field.
    - New `HourlyData` interface: `{ hours: number[], avgSpeeds: number[], avgTemps: number[] }`.
    - New methods: `getUserProfile()`, `updateLocation()`, `ownerAddVehicle()`, `ownerDeleteVehicle()`, `ownerAssignVehicle()`, `ownerUnassignVehicle()`, `getOwnerVehicleLatest()`, `getOwnerVehicleHourly()`, `getOwnerVehicleAlerts()`, `getOwnerAllAlerts()`, `ownerMarkAlertRead()`, `getCustomerAssignedVehicles()`, `getCustomerAssignedVehicleLatest()`.

15. **Owner Dashboard — Vehicle Display Fixed:** The vehicle roster table now correctly displays all vehicles with columns: VIN, Make/Model, Status, Assigned Driver, and Actions. Each row is clickable to open the detail popup.

16. **Owner Dashboard — Add Vehicle Button Wired:** The `➕ Add Vehicle` button now opens a modal form. The form collects VIN, Make, Model, and Year, calls `POST /api/owner/vehicles`, and refreshes the vehicle list on success. Error handling displays API error messages.

17. **Owner Dashboard — Vehicle Detail Popup Modal:** Clicking a vehicle row or the "View" button opens a full-screen modal overlay showing:
    - Vehicle emoji icon + Make/Model/Year + VIN
    - 🔴 LIVE indicator
    - Four stat cards: GPS Location, Driving Status (Overspeeding/Normal), Assigned Driver, Vehicle Status
    - Live speed and temperature gauges (`app-gauge` component)
    - Hourly telemetry line chart (today's speed + temperature by hour)
    - Detail data polls every 3 seconds for live updates
    - Close button to dismiss the modal

18. **Owner Dashboard — Assigned Driver Column in Top 5 Table:** The "Peak Speed Today — Top 5 Vehicles" table now includes an "Assigned Driver" column showing a blue badge with the customer's username (e.g., `👤 customer`) or a `—` dash if unassigned.

19. **Owner Dashboard — Vehicle Assignment Modal:** Each vehicle in the roster has an "Assign" button (green) that opens a modal. The owner types a customer username and submits. On success, the vehicle roster and top 5 table refresh to show the assignment. Assigned vehicles show an "Unassign" button (orange) instead.

20. **Owner Dashboard — Vehicle Delete:** Each vehicle has a red 🗑️ delete button. Clicking shows a confirmation dialog, then calls `DELETE /api/owner/vehicles/{id}`. The vehicle and all its readings are removed.

21. **Customer Dashboard — Assigned Vehicle Support:** The customer component now:
    - Fetches assigned vehicles via `GET /api/customer/assigned-vehicles` on init.
    - Displays a prominent gradient banner showing the assigned vehicle's Make, Model, Year, VIN, and Status.
    - Shows "No Assigned Vehicles" placeholder if no vehicles are assigned.
    - Prioritizes assigned vehicles for telemetry display over rentals.
    - Includes full vehicle controls (Gas/Brake pedals with keyboard support), speedometer, temperature gauge, alert status, and telemetry history chart.

22. **Logout Updated:** `AppComponent.logout()` now also clears `location` from localStorage.

### Session 3 — Owner UX, charts, locations from API (2026-04-11)

23. **Locations from backend:** `AppComponent` and `RegisterComponent` load cities via `GET /api/auth/locations` (`ApiService.getSupportedLocations()`). No hardcoded city lists in the shell or register form.

24. **Top 5 as clickable cards:** Peak-speed section uses responsive cards; `openPeakVehicleDetail()` resolves `vehicleId` and opens the existing live detail modal (gauges, GPS, rental status, hourly chart).

25. **Trend charts:** Separate Chart.js options for speed vs temperature (axis titles, grids, tooltips, point styling). Detail modal hourly chart uses **dual y-axes** (`y` / `y1`) for clearer speed vs temperature scaling.

26. **Assignment UI:** Modal loads `assignment-options`, `<select>` lists customers eligible for the vehicle’s location; hidden customers with another vehicle unless **Swap** is checked; `ownerAssignVehicle(..., swap)` sends the flag.

27. **Detail modal:** Added operating location stat card; section title **Owner Fleet Alerts**.

28. **Alert list:** `app-alert-list` styles `INFO` (blue accent) in addition to WARNING/CRITICAL.

### Best Practices Applied
- **Lazy Loading:** Each dashboard module is lazy-loaded via `loadChildren` — reduces initial bundle size.
- **Route Guards:** `AuthGuard` checks token existence, `RoleGuard` checks role matches route data.
- **Typed Services:** `ApiService` uses TypeScript interfaces for all API responses — compile-time safety.
- **Singleton Services:** `@Injectable({ providedIn: 'root' })` for all shared services.
- **Immutable Chart Updates:** Chart data is replaced with spread operator `{ ...data }` to trigger Angular change detection.
- **OnDestroy Cleanup:** `clearInterval` called in `ngOnDestroy` to prevent memory leaks.
- **HTTP Interceptor:** `AuthInterceptor` automatically attaches JWT to every outgoing request.
- **Design System:** All colors reference CSS variables (`--bg`, `--card`, `--text`, `--muted`, `--border`) — theme changes are instant and global.
- **Modal Pattern:** All modals (Add Vehicle, Assign Vehicle, Vehicle Detail) use `fixed inset-0` overlays with `backdrop-blur-sm`, click-outside-to-close, and `$event.stopPropagation()` on content.
- **Event Propagation Control:** Action buttons use `$event.stopPropagation()` to prevent row click events from firing when clicking inline buttons.

---

## 3. API Endpoint Reference

### Auth Endpoints
| Method | Endpoint             | Description                              | Auth     |
|--------|----------------------|------------------------------------------|----------|
| POST   | /api/auth/login      | Login with username/password             | Public   |
| POST   | /api/auth/register   | Register with username/password/role/location | Public   |
| GET    | /api/auth/locations  | Supported city names (single source of truth) | Public   |

### User Endpoints (All Authenticated)
| Method | Endpoint             | Description                              | Auth     |
|--------|----------------------|------------------------------------------|----------|
| GET    | /api/user/profile    | Get logged-in user's profile (+ isAssigned) | Any role |
| PUT    | /api/user/location   | Update location (blocked for owners; blocked for assigned customers) | Any role |

### Owner Endpoints (OWNER role only)
| Method | Endpoint                           | Description                              |
|--------|------------------------------------|------------------------------------------|
| GET    | /api/owner/analytics               | Fleet analytics (avg speed/temp, counts) |
| GET    | /api/owner/vehicles                | List all owner's vehicles                |
| POST   | /api/owner/vehicles                | Create a new vehicle                     |
| DELETE | /api/owner/vehicles/{id}           | Delete a vehicle + all its readings      |
| GET    | /api/owner/vehicles/{id}/latest    | Latest telemetry for a vehicle           |
| GET    | /api/owner/vehicles/{id}/hourly    | Hourly aggregated telemetry for today    |
| GET    | /api/owner/vehicles/{id}/alerts    | Fleet activities filtered to that vehicle |
| GET    | /api/owner/vehicles/{id}/assignment-options | Customers in vehicle location + swap hints |
| POST   | /api/owner/vehicles/{id}/assign    | Assign (`customerUsername`, optional `swap`) |
| POST   | /api/owner/vehicles/{id}/unassign  | Remove customer assignment               |
| GET    | /api/owner/trends                  | Latest 50 fleet readings for charts      |
| GET    | /api/owner/peak-speeds             | Top 5 peak speeds today                  |
| GET    | /api/owner/alerts                  | Owner fleet activities / alerts (recent) |
| PUT    | /api/owner/alerts/{id}/read        | Mark an alert as read                    |
| PUT    | /api/owner/alerts/mark-all-read    | Mark all owner alerts as read            |
| GET    | /api/owner/assignment-requests     | List pending assignment requests         |
| POST   | /api/owner/assignment-requests/{id}/approve | Approve an assignment request   |
| POST   | /api/owner/assignment-requests/{id}/reject  | Reject an assignment request    |

### Driver Endpoints (CUSTOMER role)
| Method | Endpoint                                    | Description                    |
|--------|---------------------------------------------|--------------------------------|
| GET    | /api/driver/vehicle/{id}/readings           | Last 20 readings               |
| GET    | /api/driver/vehicle/{id}/readings/latest    | Latest reading                 |
| POST   | /api/driver/vehicle/{id}/control            | Send throttle/brake command    |

### Customer Endpoints (CUSTOMER role)
| Method | Endpoint                             | Description                              |
|--------|--------------------------------------|------------------------------------------|
| GET    | /api/customer/rentals                | Customer's rentals                       |
| GET    | /api/customer/assigned-vehicles      | Vehicles assigned to the customer        |
| GET    | /api/customer/assigned-vehicle/latest| Latest telemetry for assigned vehicle    |
| POST   | /api/customer/release-vehicle        | Release assigned vehicle                 |
| POST   | /api/customer/request-vehicle/{vehicleId} | Submit assignment request for a vehicle |
| GET    | /api/customer/available-vehicles     | List available vehicles in customer area |
| POST   | /api/customer/switch-to-auto         | Clear manual control, resume auto mode   |
| GET    | /api/customer/alerts                 | Alerts for the customer's assigned vehicle |
| PUT    | /api/customer/alerts/{id}/read       | Mark a customer vehicle alert as read    |
| GET    | /api/customer/pending-requests       | List customer's pending assignment requests |

---

## 4. How to Run

### Backend
```bash
# Ensure MySQL is running on localhost:3306
# Update credentials in backend/src/main/resources/application.properties
cd backend
mvn spring-boot:run
```
`DatabaseSeedUtility` creates demo accounts when the database has no users, and `VehicleJourneySimulator` starts generating telemetry immediately (and picks up new vehicles on the next tick).

### Frontend
```bash
cd frontend
npm start
# Open http://localhost:4200 (or the port shown in terminal)
```

### Demo Credentials
| Username   | Password | Role     | Location |
|------------|----------|----------|----------|
| owner      | password | OWNER    | Chennai  |
| customer   | password | CUSTOMER | Chennai  |
| customer2  | password | CUSTOMER | Chennai  |
| customer3  | password | CUSTOMER | Chennai  |

### Demo Data
- **5 Vehicles:** Toyota Camry (2023), Honda Civic (2024), Tesla Model 3 (2025), BMW X5 (2023), Mercedes C-Class (2024), all with `location = Chennai`
- **1 Active Rental:** Toyota Camry assigned to `customer`
- **1 Vehicle Assignment:** Toyota Camry → `customer` (additional Chennai customers seeded unassigned for assignment demos)

---

## 5. Change Log

### Session 1 (2026-04-09)
- Initial project scaffolding (Spring Boot + Angular)
- JWT authentication with login/register
- Owner, Driver, Customer dashboards
- Fleet analytics, peak speeds, trend charts
- Vehicle journey simulation
- Theme system (light/dark)

### Session 2 (2026-04-11)
- **Location Feature:** Added location field to User entity, registration form, login response, and navbar location switcher
- **Owner Dashboard Fixes:** Fixed vehicle display, wired Add Vehicle button to new POST endpoint, added vehicle delete
- **Vehicle Detail Popup:** Clickable vehicle cards now open a full modal with live gauges, hourly charts, and stats
- **Vehicle Assignment:** Owners can assign/unassign vehicles to customers via modal dialog; assigned driver shown in Top 5 table
- **Customer Dashboard:** Shows assigned vehicles with gradient banner, live telemetry, vehicle controls
- **CORS Fix:** Updated from fixed port to wildcard localhost pattern
- **DataSeeder:** Updated with real car makes/models, locations, and pre-assigned vehicle
- **New Endpoints:** 12 new API endpoints added (vehicle CRUD, assignment, hourly, user profile/location)
- **New DTOs:** VehicleRequest, VehicleAssignRequest
- **Updated DTOs:** AuthResponse (+location), RegisterRequest (+location), VehiclePeakSpeed (+assignedDriverUsername)

### Session 3 (2026-04-11)
- **Chennai as default:** `AppLocations`, auth register default, seed data, and new vehicles use Chennai unless the owner’s profile location differs.
- **DatabaseSeedUtility:** Replaces `DataSeeder`; seeds three Chennai customers when DB is empty.
- **Frontend city lists:** Loaded from `GET /api/auth/locations` (AppComponent + RegisterComponent).
- **Simulator:** Refreshes vehicle list every tick; new vehicles get readings immediately; telemetry threshold changes write to `FleetActivity`.
- **Fleet activities / Owner Fleet Alerts:** `FleetActivity` entity, persisted events, `/api/owner/alerts` + per-vehicle feed, mark-read support.
- **Assignment:** Location-matched customer dropdown, one-vehicle-per-customer rule, optional **swap** to reassign.
- **Vehicle.location + API:** Peak speeds include `vehicleId`; assignment-options endpoint; `VehicleAssignRequest.swap`.
- **Owner UI:** Top 5 as clickable cards; improved fleet trend charts; dual-axis hourly detail chart; assign modal select + swap checkbox; INFO alert styling.

### Session 4 (2026-04-11)
- **Owner action reliability fix:** Resolved false-failure owner operations caused by fleet activity table DDL failure.
- **DB schema fix:** `FleetActivity.read` now maps to `is_read` to avoid MySQL reserved keyword conflict and allow `fleet_activities` table creation.
- **Transactional owner mutations:** Add/delete/assign/unassign endpoints are now transactional to prevent partial updates when activity logging fails.
- **Owner alerts restored:** `/api/owner/alerts` now returns data correctly after table creation and successful activity writes.
- **Final cleanup:** Suppressed a local Java null-analysis false-positive in owner add-vehicle flow so the Problems view remains clean.

### Session 5 (2026-04-12)

#### Backend Changes

41. **Active Rentals counter fixed:** `VehicleService.getFleetAnalytics()` now counts vehicles with a non-null `assignedCustomer` instead of querying the legacy `Rental` table. The `RentalRepository` dependency was removed from the service.

42. **Telemetry simulation: rented vehicles only.** `VehicleJourneySimulator.simulateTick()` now checks `vehicle.getAssignedCustomer() != null`. Rented vehicles get full live speed/temp/GPS drift. Unassigned vehicles record static GPS readings with speed = 0 and temperature = 70°C; no console output or alert logging for them.

43. **Mark All Alerts as Read:** New `PUT /api/owner/alerts/mark-all-read` endpoint. `FleetActivityService.markAllRead(ownerId)` sets `is_read = true` on all unread activities for the owner.

44. **Owner location locked:** `UserController.updateLocation()` now rejects `PUT /api/user/location` for users with role `OWNER`, returning `400 "Owner location is locked after registration"`.

45. **Customer location rules:** `UserController.updateLocation()` also rejects location changes for customers who have an assigned vehicle, returning `400 "Cannot change location while assigned to a vehicle"`. Unassigned customers may freely change location.

46. **User profile includes `isAssigned`:** `GET /api/user/profile` now returns `{ username, role, location, isAssigned }` where `isAssigned` is true when the customer has at least one assigned vehicle.

47. **Username in AuthResponse:** Added `username` field to `AuthResponse` DTO. Both `/api/auth/login` and `/api/auth/register` now return the username so the frontend stores it in localStorage.

48. **AssignmentRequest entity:** New JPA entity `AssignmentRequest` with fields `id`, `customer` (FK → CustomerDetails), `vehicle` (FK → Vehicle), `owner` (FK → OwnerDetails), `status` (PENDING/APPROVED/REJECTED), `createdAt`, `resolvedAt`. Table: `assignment_requests`.

49. **AssignmentRequestRepository:** Spring Data JPA interface with queries for listing by owner/status, finding duplicates, and cascade-deleting by vehicle id.

50. **Customer assignment request endpoint:** `POST /api/customer/request-vehicle/{vehicleId}` — validates customer is unassigned, vehicle is in the customer's location, vehicle is available (status ACTIVE, no current assignment), and no duplicate pending request exists. Creates an `AssignmentRequest` with status PENDING.

51. **Customer available vehicles endpoint:** `GET /api/customer/available-vehicles` — returns all vehicles with status ACTIVE, no assigned customer, and matching the customer's location.

52. **Owner assignment request management:**
    - `GET /api/owner/assignment-requests` — returns pending requests for the owner's vehicles (customerUsername, vehicleVin, vehicleMake, vehicleModel, vehicleId, status, createdAt).
    - `POST /api/owner/assignment-requests/{id}/approve` — approves the request: unassigns any existing vehicle from the customer, assigns the requested vehicle, sets status to APPROVED, logs a fleet activity.
    - `POST /api/owner/assignment-requests/{id}/reject` — rejects the request, sets status to REJECTED, logs a fleet activity.

53. **Customer release vehicle endpoint:** `POST /api/customer/release-vehicle` — releases the customer's assigned vehicle (sets assignedCustomer to null, status to ACTIVE), clears manual control state, logs fleet activity.

54. **Customer switch-to-auto endpoint:** `POST /api/customer/switch-to-auto` — clears the manual control state for the customer's assigned vehicle, returning it to automated telemetry mode.

55. **VehicleControlService.clearManualControl():** New method that removes manual control and throttle entries from the in-memory maps for a given vehicle id.

56. **Vehicle delete cascade:** `OwnerController.deleteVehicle()` now also deletes `AssignmentRequest` rows for the vehicle before deleting readings and the vehicle itself.

#### Frontend Changes

57. **Username displayed globally:** `AuthResponse` interface updated with `username`. Login and register components store username in localStorage. `AppComponent` shows `👤 {username}` badge in the navbar when logged in.

58. **Owner dashboard greeting:** Owner HTML header shows "Welcome, {username}" on the right side.

59. **Customer dashboard greeting:** Customer HTML header shows "Welcome, {username}" alongside the LIVE badge.

60. **Location selector locked for owners:** `AppComponent` fetches `GET /api/user/profile` on init. If role is OWNER, `isLocationLocked = true` — the location dropdown button shows a 🔒 icon, click does nothing, and the dropdown never opens. If role is CUSTOMER and `isAssigned` is true, location is also locked.

61. **Mark All as Read button:** `AlertListComponent` gains `@Input() showMarkAllRead` and `@Output() markAllRead`. When enabled and there are unread alerts, a blue "✓ Mark All as Read" button appears above the alert list. Owner fleet alerts section passes `[showMarkAllRead]="true"` and `(markAllRead)="markAllAlertsRead()"`.

62. **Owner assignment requests section:** New "📋 Pending Assignment Requests" section in owner HTML, visible when `assignmentRequests.length > 0`. Each request shows customer username, vehicle info, and Approve/Reject buttons. Data loaded via `getOwnerAssignmentRequests()` on each poll cycle.

63. **Customer available vehicles section:** When unassigned, customer dashboard shows a "🚙 Available Vehicles in Your Area" list with vehicle details and a "📩 Request Assignment" button per vehicle. `availableVehicles` loaded from `GET /api/customer/available-vehicles`.

64. **Customer release vehicle (double-confirm):** Assigned vehicle banner gains a "↩ Release Vehicle" button. First click shows "Are you sure?" with "Yes, Release" and "Cancel". Second confirmation calls `POST /api/customer/release-vehicle`, clears local state, stops polling, and reloads available vehicles.

65. **Switch to Auto button:** When `controlMode === 'manual'`, a "↻ Switch to Auto" button appears next to the MANUAL/AUTO badge. Clicking calls `POST /api/customer/switch-to-auto`, clears manual state on backend, and resets `controlMode` to `'auto'` on frontend without requiring a page refresh.

#### API Endpoint Updates

| Method | Endpoint                                     | Description                              | Auth         |
|--------|----------------------------------------------|------------------------------------------|--------------|
| PUT    | /api/owner/alerts/mark-all-read              | Mark all owner alerts as read            | OWNER        |
| GET    | /api/owner/assignment-requests               | List pending assignment requests         | OWNER        |
| POST   | /api/owner/assignment-requests/{id}/approve  | Approve an assignment request            | OWNER        |
| POST   | /api/owner/assignment-requests/{id}/reject   | Reject an assignment request             | OWNER        |
| POST   | /api/customer/release-vehicle                | Release assigned vehicle                 | CUSTOMER     |
| POST   | /api/customer/request-vehicle/{vehicleId}    | Submit assignment request for a vehicle  | CUSTOMER     |
| GET    | /api/customer/available-vehicles             | List available vehicles in customer area | CUSTOMER     |
| POST   | /api/customer/switch-to-auto                 | Clear manual control, resume auto mode   | CUSTOMER     |

### Session 6 (2026-04-12)

#### Backend Changes

66. **Speed alert threshold lowered:** `AlertService.evaluateAndGetAlertLevel()` WARNING threshold changed from `speed >= 80` to `speed >= 70` km/h. CRITICAL thresholds remain at speed ≥ 110 and temperature ≥ 110.

67. **Customer alerts endpoint:** New `GET /api/customer/alerts` endpoint in `CustomerController`. Returns the 50 most recent fleet activities for the customer's assigned vehicle, enabling persistent alert history in the customer dashboard's Vehicle Alerts panel.

68. **Customer alert mark-read endpoint:** New `PUT /api/customer/alerts/{id}/read` endpoint in `CustomerController`. Marks a specific alert as read, with vehicle-ownership validation to prevent cross-customer access.

69. **Customer pending requests endpoint:** New `GET /api/customer/pending-requests` endpoint in `CustomerController`. Returns the customer's pending assignment requests (vehicleId, vehicleVin, status) so the frontend can track which vehicles already have outstanding requests.

70. **FleetActivityRepository query:** Added `findTop50ByVehicle_IdOrderByCreatedAtDesc(Long vehicleId)` — Spring Data derived query that returns the 50 most recent activities for a specific vehicle.

71. **FleetActivityService new methods:**
    - `listForVehicleDirect(Long vehicleId)` — retrieves alerts scoped to a single vehicle (used by customer alerts endpoint).
    - `markReadByVehicle(Long activityId, Long vehicleId)` — marks an alert as read with vehicle-ownership guard.

#### Frontend Changes

72. **Login state hydration fix:** `AppComponent` rewritten to subscribe to `Router.events` filtered to `NavigationEnd`. On every navigation (including post-login redirect), `hydrateFromLocalStorage()` re-reads `username`, `location`, and role from localStorage and updates the navbar immediately — no page refresh required.

73. **Customer request button disabled state:** `CustomerComponent` now tracks `pendingRequestVehicleIds: Set<number>`. On load, `getCustomerPendingRequests()` populates the set. After a successful request, the vehicle id is added to the set immediately. Template shows a disabled amber "⏳ Requested" badge for vehicles in the set, replacing the active "📩 Request Assignment" button.

74. **Customer location-switch vehicle filtering:** `CustomerComponent.ngOnInit()` starts a 1-second polling interval that checks `localStorage.getItem('location')` against `_lastCheckedLocation`. When the location changes (unassigned customer uses the navbar selector), `loadAvailableVehicles()` is called automatically to refresh the list with vehicles matching the new area.

75. **ApiService: getCustomerPendingRequests():** New method calling `GET /api/customer/pending-requests` — returns the customer's pending assignment request list for tracking request button state.

#### API Endpoint Updates

| Method | Endpoint                              | Description                                          | Auth     |
|--------|---------------------------------------|------------------------------------------------------|----------|
| GET    | /api/customer/alerts                  | Alerts for the customer's assigned vehicle           | CUSTOMER |
| PUT    | /api/customer/alerts/{id}/read        | Mark a customer vehicle alert as read                | CUSTOMER |
| GET    | /api/customer/pending-requests        | List customer's pending assignment requests          | CUSTOMER |

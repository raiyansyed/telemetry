/**
 * api.service.ts - The central HTTP service that communicates with the Spring Boot backend.
 *
 * WHAT IS A SERVICE IN ANGULAR?
 * - A service is a class that holds business logic and data access code.
 * - Unlike components (which have templates/UI), services are pure logic.
 * - @Injectable({ providedIn: 'root' }) makes it a singleton - one instance for the whole app.
 * - Components inject this service via their constructor to make API calls.
 *
 * WHAT THIS FILE CONTAINS:
 * 1. TypeScript INTERFACES (at the top):
 *    - Define the "shape" of data objects (like Java DTOs).
 *    - TypeScript checks that the data you use matches these shapes at compile time.
 *    - Examples: AuthResponse, Vehicle, VehicleReading, Alert, etc.
 *
 * 2. ApiService CLASS (at the bottom):
 *    - Methods that make HTTP GET/POST/PUT/DELETE requests to the backend.
 *    - Each method returns an Observable (RxJS) - Angular's way of handling async data.
 *    - The AuthInterceptor automatically adds the JWT token to every request.
 *
 * API BASE URL: http://localhost:9090/api
 *
 * OBSERVABLE PATTERN:
 * - All methods return Observable<T> instead of Promise<T>.
 * - Components call .subscribe() to receive the data when it arrives.
 * - Example: this.apiService.getOwnerVehicles().subscribe({ next: (data) => this.vehicles = data });
 */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/** AuthResponse - Data returned after successful login/registration */
export interface AuthResponse {
  token: string;
  role: string;
  location: string;
  username: string;
}

/** FleetAnalytics - Summary stats for the owner dashboard header cards */
export interface FleetAnalytics {
  averageSpeed: number;
  averageTemperature: number;
  vehicleCount: number;
  activeRentals: number;
}

/** VehiclePeakSpeed - Highest speed today for a vehicle (peak speed leaderboard) */
export interface VehiclePeakSpeed {
  vehicleId: number;
  vin: string;
  make: string;
  model: string;
  peakSpeed: number;
  avgTemperature: number;
  latitude: number;
  longitude: number;
  timestamp: string;
  assignedDriverUsername: string | null;
}

/** VehicleReading - A single telemetry snapshot (speed, temp, GPS, alert level) */
export interface VehicleReading {
  id: number;
  timestamp: string;
  speed: number;
  temperature: number;
  latitude: number;
  longitude: number;
  alertLevel: string;
}

/** Vehicle - A vehicle in the fleet with its assignment status */
export interface Vehicle {
  id: number;
  vin: string;
  make: string;
  model: string;
  year: number;
  status: string;
  location?: string | null;
  imageUrl: string | null;
  assignedCustomer: {
    id: number;
    licenseNumber: string;
    address: string;
    user: {
      id: number;
      username: string;
      role: string;
      location: string;
    };
  } | null;
}

export interface Rental {
  id: number;
  startDate: string;
  endDate: string;
  status: string;
  vehicle: Vehicle;
}

/** Alert - A notification/alert displayed in the alerts panel */
export interface Alert {
  id: number;
  message: string;
  type: string;        // 'CRITICAL' | 'WARNING' | 'INFO' - determines the color of the alert
  triggeredAt: string; // ISO datetime string for display
  isRead: boolean;     // Whether the user has marked this alert as read
  licensePlate?: string;         // Vehicle VIN for display (optional)
  isAssignmentRequest?: boolean; // If true, shows Approve/Reject buttons instead of Read button
}

/** HourlyData - Hourly averages for the vehicle detail popup chart */
export interface HourlyData {
  hours: number[];
  avgSpeeds: number[];
  avgTemps: number[];
}

/** AssignmentCustomerOption - A customer option in the vehicle assignment dropdown */
export interface AssignmentCustomerOption {
  username: string;
  hasOtherVehicle: boolean;
  otherVehicleId: number | null;
  otherVehicleVin: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly baseUrl = 'http://localhost:9090/api';

  constructor(private readonly http: HttpClient) {}

  // --- Auth ---
  login(credentials: { username: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, credentials);
  }

  register(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/register`, data);
  }

  getSupportedLocations(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/auth/locations`);
  }

  // --- User Profile & Location ---
  getUserProfile(): Observable<{ username: string; role: string; location: string }> {
    return this.http.get<{ username: string; role: string; location: string }>(`${this.baseUrl}/user/profile`);
  }

  updateLocation(location: string): Observable<{ location: string }> {
    return this.http.put<{ location: string }>(`${this.baseUrl}/user/location`, { location });
  }

  // --- Owner ---
  getOwnerAnalytics(): Observable<FleetAnalytics> {
    return this.http.get<FleetAnalytics>(`${this.baseUrl}/owner/analytics`);
  }

  getOwnerVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(`${this.baseUrl}/owner/vehicles`);
  }

  getOwnerTrends(): Observable<VehicleReading[]> {
    return this.http.get<VehicleReading[]>(`${this.baseUrl}/owner/trends`);
  }

  getOwnerPeakSpeeds(): Observable<VehiclePeakSpeed[]> {
    return this.http.get<VehiclePeakSpeed[]>(`${this.baseUrl}/owner/peak-speeds`);
  }

  getOwnerVehicleLatest(vehicleId: number): Observable<VehicleReading> {
    return this.http.get<VehicleReading>(`${this.baseUrl}/owner/vehicles/${vehicleId}/latest`);
  }

  getOwnerVehicleHourly(vehicleId: number): Observable<HourlyData> {
    return this.http.get<HourlyData>(`${this.baseUrl}/owner/vehicles/${vehicleId}/hourly`);
  }

  getOwnerVehicleAlerts(vehicleId: number): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${this.baseUrl}/owner/vehicles/${vehicleId}/alerts`);
  }

  getOwnerAllAlerts(): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${this.baseUrl}/owner/alerts`);
  }

  ownerMarkAlertRead(alertId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/owner/alerts/${alertId}/read`, {});
  }

  ownerAddVehicle(data: { vin: string; make: string; model: string; year?: number }): Observable<Vehicle> {
    return this.http.post<Vehicle>(`${this.baseUrl}/owner/vehicles`, data);
  }

  ownerDeleteVehicle(vehicleId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/owner/vehicles/${vehicleId}`);
  }

  ownerAssignVehicle(vehicleId: number, customerUsername: string, swap?: boolean): Observable<any> {
    return this.http.post(`${this.baseUrl}/owner/vehicles/${vehicleId}/assign`, {
      customerUsername,
      swap: swap === true
    });
  }

  getOwnerAssignmentOptions(vehicleId: number): Observable<AssignmentCustomerOption[]> {
    return this.http.get<AssignmentCustomerOption[]>(`${this.baseUrl}/owner/vehicles/${vehicleId}/assignment-options`);
  }

  ownerUnassignVehicle(vehicleId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/owner/vehicles/${vehicleId}/unassign`, {});
  }

  // --- Driver ---
  getDriverLatestReading(vehicleId: number): Observable<VehicleReading> {
    return this.http.get<VehicleReading>(`${this.baseUrl}/driver/vehicle/${vehicleId}/readings/latest`);
  }

  getDriverReadings(vehicleId: number): Observable<VehicleReading[]> {
    return this.http.get<VehicleReading[]>(`${this.baseUrl}/driver/vehicle/${vehicleId}/readings`);
  }

  controlVehicle(vehicleId: number, action: string, throttle: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/driver/vehicle/${vehicleId}/control`, { action, throttle });
  }

  // --- Customer ---
  getCustomerRentals(): Observable<Rental[]> {
    return this.http.get<Rental[]>(`${this.baseUrl}/customer/rentals`);
  }

  getCustomerAssignedVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(`${this.baseUrl}/customer/assigned-vehicles`);
  }

  getCustomerAssignedVehicleLatest(): Observable<VehicleReading> {
    return this.http.get<VehicleReading>(`${this.baseUrl}/customer/assigned-vehicle/latest`);
  }

  getCustomerTelemetry(): Observable<VehicleReading> {
    return this.http.get<VehicleReading>(`${this.baseUrl}/customer/telemetry`);
  }

  getCustomerAlerts(): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${this.baseUrl}/customer/alerts`);
  }

  customerMarkAlertRead(alertId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/customer/alerts/${alertId}/read`, {});
  }

  // --- Owner: Mark All Alerts Read ---
  ownerMarkAllAlertsRead(): Observable<any> {
    return this.http.put(`${this.baseUrl}/owner/alerts/mark-all-read`, {});
  }

  // --- Owner: Assignment Requests ---
  getOwnerAssignmentRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/owner/assignment-requests`);
  }

  ownerApproveRequest(requestId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/owner/assignment-requests/${requestId}/approve`, {});
  }

  ownerRejectRequest(requestId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/owner/assignment-requests/${requestId}/reject`, {});
  }

  // --- Customer: Release Vehicle ---
  customerReleaseVehicle(): Observable<any> {
    return this.http.post(`${this.baseUrl}/customer/release-vehicle`, {});
  }

  // --- Customer: Request Vehicle ---
  customerRequestVehicle(vehicleId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/customer/request-vehicle/${vehicleId}`, {});
  }

  // --- Customer: Available Vehicles ---
  getCustomerAvailableVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(`${this.baseUrl}/customer/available-vehicles`);
  }

  // --- Customer: Pending Requests ---
  getCustomerPendingRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/customer/pending-requests`);
  }

  // --- Customer: Switch to Auto ---
  customerSwitchToAuto(): Observable<any> {
    return this.http.post(`${this.baseUrl}/customer/switch-to-auto`, {});
  }

  getCustomerHourly(): Observable<HourlyData> {
    return this.http.get<HourlyData>(`${this.baseUrl}/customer/hourly`);
  }
}

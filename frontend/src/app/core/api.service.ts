import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthResponse {
  token: string;
  role: string;
  location: string;
  username: string;
}

export interface FleetAnalytics {
  averageSpeed: number;
  averageTemperature: number;
  vehicleCount: number;
  activeRentals: number;
}

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

export interface VehicleReading {
  id: number;
  timestamp: string;
  speed: number;
  temperature: number;
  latitude: number;
  longitude: number;
  alertLevel: string;
}

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

export interface Alert {
  id: number;
  message: string;
  type: string;        
  triggeredAt: string;
  isRead: boolean;
  licensePlate?: string;
  isAssignmentRequest?: boolean;
}

export interface HourlyData {
  hours: number[];
  avgSpeeds: number[];
  avgTemps: number[];
}

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

  //  User Profile & Location 
  getUserProfile(): Observable<{ username: string; role: string; location: string }> {
    return this.http.get<{ username: string; role: string; location: string }>(`${this.baseUrl}/user/profile`);
  }

  updateLocation(location: string): Observable<{ location: string }> {
    return this.http.put<{ location: string }>(`${this.baseUrl}/user/location`, { location });
  }

  //  Owner 
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

  //  Driver 
  getDriverLatestReading(vehicleId: number): Observable<VehicleReading> {
    return this.http.get<VehicleReading>(`${this.baseUrl}/driver/vehicle/${vehicleId}/readings/latest`);
  }

  getDriverReadings(vehicleId: number): Observable<VehicleReading[]> {
    return this.http.get<VehicleReading[]>(`${this.baseUrl}/driver/vehicle/${vehicleId}/readings`);
  }

  controlVehicle(vehicleId: number, action: string, throttle: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/driver/vehicle/${vehicleId}/control`, { action, throttle });
  }

  // Customer 
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

  //  Owner: Mark All Alerts Read 
  ownerMarkAllAlertsRead(): Observable<any> {
    return this.http.put(`${this.baseUrl}/owner/alerts/mark-all-read`, {});
  }

  // Owner: Assignment Requests 
  getOwnerAssignmentRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/owner/assignment-requests`);
  }

  ownerApproveRequest(requestId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/owner/assignment-requests/${requestId}/approve`, {});
  }

  ownerRejectRequest(requestId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/owner/assignment-requests/${requestId}/reject`, {});
  }

  //  Customer: Release Vehicle 
  customerReleaseVehicle(): Observable<any> {
    return this.http.post(`${this.baseUrl}/customer/release-vehicle`, {});
  }

  //  Customer: Request Vehicle 
  customerRequestVehicle(vehicleId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/customer/request-vehicle/${vehicleId}`, {});
  }

  //  Customer: Available Vehicles `
  getCustomerAvailableVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(`${this.baseUrl}/customer/available-vehicles`);
  }

  //  Customer: Pending Requests 
  getCustomerPendingRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/customer/pending-requests`);
  }

  //  Customer: Switch to Auto 
  customerSwitchToAuto(): Observable<any> {
    return this.http.post(`${this.baseUrl}/customer/switch-to-auto`, {});
  }

  getCustomerHourly(): Observable<HourlyData> {
    return this.http.get<HourlyData>(`${this.baseUrl}/customer/hourly`);
  }
}

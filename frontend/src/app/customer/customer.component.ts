/**
 * CustomerComponent - The main Customer/Driver Dashboard.
 *
 * This is a feature-rich component (~430 lines) that handles TWO states:
 *
 * STATE 1: NO VEHICLE ASSIGNED
 * - Shows "No Assigned Vehicles" message.
 * - Displays a list of available (unassigned) vehicles in the customer's city.
 * - Customer can click "Request Assignment" to ask the owner for a vehicle.
 * - Polls for location changes (if customer switches city in the navbar).
 *
 * STATE 2: VEHICLE ASSIGNED
 * - Shows an info banner with vehicle details (make, model, VIN, status).
 * - Live speedometer gauge (SVG) that updates every 2 seconds.
 * - Vehicle details card (make/model, rental info, GPS coordinates).
 * - GAS and BRAKE buttons for manual driving control (or keyboard: W/S/arrows).
 * - Temperature gauge (horizontal bar).
 * - Alert status indicator (NONE/WARNING/CRITICAL).
 * - Telemetry History chart (last 20 readings).
 * - Vehicle alerts panel (overspeeding warnings, etc.).
 * - "Release Vehicle" button with double-confirm (prevents accidental release).
 * - "Switch to Auto" button to stop manual control.
 *
 * KEYBOARD CONTROLS:
 * - @HostListener('window:keydown') / @HostListener('window:keyup')
 * - These Angular decorators listen for keyboard events on the entire window.
 * - W or ArrowUp = accelerate, S or ArrowDown or Space = brake.
 * - Holding a key sends continuous control commands to the backend.
 *
 * DATA POLLING:
 * - When a vehicle is assigned: polls every 2 seconds for latest telemetry data.
 * - When no vehicle: polls every 1 second for location changes (to refresh available vehicles).
 */
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { ApiService, Rental, Vehicle, VehicleReading, Alert, HourlyData } from '../core/api.service';

@Component({
  selector: 'app-customer',
  templateUrl: './customer.component.html',
  styleUrls: ['./customer.component.scss']
})
export class CustomerComponent implements OnInit, OnDestroy {

  rentals: Rental[] = [];
  activeRental: Rental | null = null;
  assignedVehicles: Vehicle[] = [];
  username = '';

  // Live telemetry
  telemetry: VehicleReading | null = null;
  alerts: Alert[] = [];
  hourlyData: HourlyData | null = null;
  pollInterval: any;

  // Driver controls (merged from driver component)
  latestReading: VehicleReading | null = null;
  readings: VehicleReading[] = [];
  isAccelerating = false;
  isBraking = false;
  throttleLevel = 0.5;
  controlMode: 'auto' | 'manual' = 'auto';

  // Release vehicle double-confirm
  releaseStep: 0 | 1 | 2 = 0; // 0=idle, 1=first confirm, 2=processing

  // Available vehicles for unassigned customers
  availableVehicles: Vehicle[] = [];
  requestLoading = false;
  pendingRequestVehicleIds: Set<number> = new Set();
  private locationCheckInterval: any;

  get vehicleId(): number {
    // Prefer assigned vehicle, then rental vehicle
    if (this.assignedVehicles.length > 0) {
      return this.assignedVehicles[0].id;
    }
    return this.activeRental?.vehicle?.id ?? 1;
  }

  get activeVehicle(): Vehicle | null {
    if (this.assignedVehicles.length > 0) return this.assignedVehicles[0];
    return this.activeRental?.vehicle ?? null;
  }

  // Speedometer gauge calculations
  get speedNeedleAngle(): number {
    const speed = this.latestReading?.speed || 0;
    return -135 + (speed / 200) * 270;
  }

  get speedPercent(): number {
    return Math.min(100, ((this.latestReading?.speed || 0) / 200) * 100);
  }

  get tempPercent(): number {
    return Math.min(100, (((this.latestReading?.temperature || 70) - 60) / 80) * 100);
  }

  // Chart data
  public liveChartData: any = {
    labels: [],
    datasets: [
      { data: [], label: 'Speed (km/h)', borderColor: '#3b82f6', backgroundColor: 'rgba(59,130,246,0.08)', fill: true, tension: 0.3, pointRadius: 0, borderWidth: 2 },
      { data: [], label: 'Temp (°C)', borderColor: '#ef4444', backgroundColor: 'rgba(239,68,68,0.08)', fill: true, tension: 0.3, pointRadius: 0, borderWidth: 2 }
    ]
  };

  public chartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 0 },
    plugins: { legend: { position: 'top' } },
    scales: {
      y: { beginAtZero: true, grid: { color: 'rgba(128,128,128,0.1)' } },
      x: { ticks: { maxTicksLimit: 8, font: { size: 9 } }, grid: { display: false } }
    }
  };

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.username = localStorage.getItem('username') || '';
    this.loadInitialData();
    // Poll for location changes (when customer switches city in navbar)
    this.locationCheckInterval = setInterval(() => {
      if (!this.hasVehicle) {
        const currentLoc = localStorage.getItem('location') || '';
        if (currentLoc !== this._lastCheckedLocation) {
          this._lastCheckedLocation = currentLoc;
          this.loadAvailableVehicles();
        }
      }
    }, 1000);
  }
  private _lastCheckedLocation = localStorage.getItem('location') || '';

  ngOnDestroy(): void {
    if (this.pollInterval) { clearInterval(this.pollInterval); }
    if (this.locationCheckInterval) { clearInterval(this.locationCheckInterval); }
    this.stopAccelerate();
    this.stopBrake();
  }

  private loadInitialData(): void {
    // Load assigned vehicles
    this.apiService.getCustomerAssignedVehicles().subscribe({
      next: (data) => {
        this.assignedVehicles = data;
        if (this.assignedVehicles.length > 0) {
          this.startLivePolling();
        } else {
          this.loadAvailableVehicles();
        }
      },
      error: () => {}
    });

    // Load rentals
    this.apiService.getCustomerRentals().subscribe({
      next: (data) => {
        this.rentals = data;
        this.activeRental = this.rentals.find(r => r.status === 'ACTIVE') || this.rentals[0] || null;
        if (this.activeRental && this.assignedVehicles.length === 0) {
          this.startLivePolling();
        }
      }
    });
  }

  private loadAvailableVehicles(): void {
    this.apiService.getCustomerAvailableVehicles().subscribe({
      next: (data) => this.availableVehicles = data,
      error: () => {}
    });
    // Also load pending requests to disable already-requested buttons
    this.apiService.getCustomerPendingRequests().subscribe({
      next: (data: any[]) => {
        this.pendingRequestVehicleIds = new Set(data.map(r => r.vehicleId));
      },
      error: () => {}
    });
  }

  isVehicleRequested(vehicleId: number): boolean {
    return this.pendingRequestVehicleIds.has(vehicleId);
  }

  private startLivePolling(): void {
    if (this.pollInterval) return; // Don't double-start
    this.pollData();
    this.pollInterval = setInterval(() => this.pollData(), 2000);
  }

  private pollData(): void {
    const vid = this.vehicleId;

    this.apiService.getDriverLatestReading(vid).subscribe({
      next: (data) => {
        this.latestReading = data;
        this.telemetry = data;
      },
      error: () => {}
    });

    this.apiService.getDriverReadings(vid).subscribe({
      next: (data) => {
        this.readings = [...data].reverse();
        this.updateChart();
      },
      error: () => {}
    });

    this.apiService.getCustomerAlerts().subscribe({
      next: (data) => this.alerts = data,
      error: () => {}
    });
  }

  private updateChart(): void {
    const labels = this.readings.map(r => new Date(r.timestamp).toLocaleTimeString());
    this.liveChartData = {
      labels,
      datasets: [
        { ...this.liveChartData.datasets[0], data: this.readings.map(r => r.speed) },
        { ...this.liveChartData.datasets[1], data: this.readings.map(r => r.temperature) }
      ]
    };
  }

  onMarkAlertRead(alertId: number): void {
    this.apiService.customerMarkAlertRead(alertId).subscribe({
      next: () => this.pollData()
    });
  }

  get isOverspeeding(): boolean {
    return (this.latestReading?.speed ?? 0) > 80;
  }

  get hasVehicle(): boolean {
    return this.assignedVehicles.length > 0 || this.activeRental != null;
  }

  // ---- Owner Info (from rental) ----
  get ownerCompany(): string {
    return (this.activeRental as any)?.owner?.companyName || 'Fleet Owner';
  }

  get ownerName(): string {
    return (this.activeRental as any)?.owner?.user?.username || 'Owner';
  }

  // ---- Vehicle Controls (from driver) ----
  @HostListener('window:keydown', ['$event'])
  onKeyDown(e: KeyboardEvent) {
    if (e.key === 'ArrowUp' || e.key === 'w' || e.key === 'W') {
      this.startAccelerate();
    }
    if (e.key === 'ArrowDown' || e.key === 's' || e.key === 'S' || e.key === ' ') {
      e.preventDefault();
      this.startBrake();
    }
  }

  @HostListener('window:keyup', ['$event'])
  onKeyUp(e: KeyboardEvent) {
    if (e.key === 'ArrowUp' || e.key === 'w' || e.key === 'W') {
      this.stopAccelerate();
    }
    if (e.key === 'ArrowDown' || e.key === 's' || e.key === 'S' || e.key === ' ') {
      this.stopBrake();
    }
  }

  startAccelerate(): void {
    if (this.isAccelerating) return;
    this.isAccelerating = true;
    this.isBraking = false;
    this.controlMode = 'manual';
    this.sendControl('ACCELERATE');
  }

  stopAccelerate(): void {
    if (!this.isAccelerating) return;
    this.isAccelerating = false;
    if (!this.isBraking) { this.sendControl('IDLE'); }
  }

  startBrake(): void {
    if (this.isBraking) return;
    this.isBraking = true;
    this.isAccelerating = false;
    this.controlMode = 'manual';
    this.sendControl('BRAKE');
  }

  stopBrake(): void {
    if (!this.isBraking) return;
    this.isBraking = false;
    if (!this.isAccelerating) { this.sendControl('IDLE'); }
  }

  onThrottleChange(): void {
    if (this.isAccelerating) { this.sendControl('ACCELERATE'); }
  }

  private sendControl(action: string): void {
    this.apiService.controlVehicle(this.vehicleId, action, this.throttleLevel).subscribe();
  }

  // ---- Release Vehicle (double confirm) ----
  initiateRelease(): void {
    this.releaseStep = 1;
  }

  cancelRelease(): void {
    this.releaseStep = 0;
  }

  confirmRelease(): void {
    this.releaseStep = 2;
    this.apiService.customerReleaseVehicle().subscribe({
      next: () => {
        this.releaseStep = 0;
        this.assignedVehicles = [];
        this.latestReading = null;
        this.telemetry = null;
        this.readings = [];
        this.controlMode = 'auto';
        if (this.pollInterval) {
          clearInterval(this.pollInterval);
          this.pollInterval = null;
        }
        this.loadAvailableVehicles();
      },
      error: (err) => {
        this.releaseStep = 0;
        alert(err.error?.message || 'Failed to release vehicle');
      }
    });
  }

  // ---- Request Vehicle ----
  requestVehicle(vehicleId: number): void {
    this.requestLoading = true;
    this.apiService.customerRequestVehicle(vehicleId).subscribe({
      next: () => {
        this.requestLoading = false;
        this.pendingRequestVehicleIds.add(vehicleId);
      },
      error: (err) => {
        this.requestLoading = false;
        alert(err.error?.message || 'Failed to submit request');
      }
    });
  }

  // ---- Switch to Auto ----
  switchToAuto(): void {
    this.apiService.customerSwitchToAuto().subscribe({
      next: () => {
        this.controlMode = 'auto';
        this.isAccelerating = false;
        this.isBraking = false;
      },
      error: () => {}
    });
  }
}

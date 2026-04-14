/**
 * DriverComponent - Standalone vehicle control & telemetry dashboard.
 *
 * This is a simplified driver dashboard that controls a HARDCODED vehicle (vehicleId = 1).
 * Unlike the CustomerComponent which dynamically gets an assigned vehicle, this component
 * is for direct demo/testing of vehicle controls.
 *
 * FEATURES:
 * - Live speedometer gauge (SVG arc, 0-200 km/h, updates every 2 seconds).
 * - Temperature gauge (horizontal bar, range 60-140°C).
 * - Telemetry history chart (speed + temperature over time via Chart.js).
 * - Manual driving controls: GAS / BRAKE buttons + keyboard support.
 * - Throttle intensity slider (0-100%, controls how hard the vehicle accelerates).
 * - Auto / Manual mode toggle.
 *
 * KEYBOARD CONTROLS:
 * - W or ArrowUp → Accelerate (hold to keep accelerating).
 * - S or ArrowDown or Space → Brake (hold to keep braking).
 * - Release key → sends IDLE command (vehicle coasts / decelerates naturally).
 *
 * DATA FLOW:
 * 1. Every 2 seconds, pollData() fetches:
 *    - GET /api/driver/vehicle/1/latest → latest VehicleReading (speed, temp, GPS, alerts).
 *    - GET /api/driver/vehicle/1/readings → last 20 readings for the chart.
 * 2. sendControl() sends POST /api/driver/vehicle/1/control with action (ACCELERATE/BRAKE/IDLE).
 * 3. The backend's VehicleJourneySimulator applies physics (acceleration/drag) and generates
 *    new readings every 3 seconds.
 *
 * COMPUTED PROPERTIES (getters):
 * - speedNeedleAngle: Maps speed (0-200) to rotation angle (-135° to +135°) for SVG gauge.
 * - speedPercent: Speed as a percentage of 200 km/h (used for color coding).
 * - tempPercent: Temperature mapped to 0-100% (range 60-140°C, used for bar width).
 */
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { ApiService, VehicleReading } from '../core/api.service';

@Component({
  selector: 'app-driver',
  templateUrl: './driver.component.html',
  styleUrls: ['./driver.component.scss']
})
export class DriverComponent implements OnInit, OnDestroy {
  latestReading: VehicleReading | null = null;
  readings: VehicleReading[] = [];
  pollInterval: any;
  vehicleId = 1;

  // Control state
  isAccelerating = false;
  isBraking = false;
  throttleLevel = 0.5; // 0-1 throttle intensity
  controlMode: 'auto' | 'manual' = 'auto';

  // Gauge
  get speedNeedleAngle(): number {
    const speed = this.latestReading?.speed || 0;
    // Map 0-200 km/h to -135° to +135° (270° sweep)
    return -135 + (speed / 200) * 270;
  }

  get speedPercent(): number {
    return Math.min(100, ((this.latestReading?.speed || 0) / 200) * 100);
  }

  get tempPercent(): number {
    return Math.min(100, (((this.latestReading?.temperature || 70) - 60) / 80) * 100);
  }

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
    this.pollData();
    this.pollInterval = setInterval(() => this.pollData(), 2000);
  }

  ngOnDestroy(): void {
    if (this.pollInterval) { clearInterval(this.pollInterval); }
    this.stopAccelerate();
    this.stopBrake();
  }

  // -- Keyboard controls --
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
    if (!this.isBraking) {
      this.sendControl('IDLE');
    }
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
    if (!this.isAccelerating) {
      this.sendControl('IDLE');
    }
  }

  onThrottleChange(): void {
    if (this.isAccelerating) {
      this.sendControl('ACCELERATE');
    }
  }

  private sendControl(action: string): void {
    this.apiService.controlVehicle(this.vehicleId, action, this.throttleLevel).subscribe();
  }

  private pollData(): void {
    this.apiService.getDriverLatestReading(this.vehicleId).subscribe({
      next: (data) => this.latestReading = data
    });

    this.apiService.getDriverReadings(this.vehicleId).subscribe({
      next: (data) => {
        this.readings = [...data].reverse();
        this.updateChart();
      }
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
}

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ApiService, FleetAnalytics, Vehicle, VehiclePeakSpeed, VehicleReading, Alert, HourlyData, AssignmentCustomerOption } from '../core/api.service';

@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.scss']
})
export class OwnerComponent implements OnInit, OnDestroy {

  analytics: FleetAnalytics | null = null;
  vehicles: Vehicle[] = [];
  peakSpeeds: VehiclePeakSpeed[] = [];
  fleetAlerts: Alert[] = [];
  pollInterval: any;
  username = '';

  // Assignment Requests
  assignmentRequests: any[] = [];

  // Vehicle detail popup
  showDetailModal = false;
  selectedVehicle: Vehicle | null = null;
  selectedLatest: VehicleReading | null = null;
  selectedHourly: HourlyData | null = null;
  selectedAlerts: Alert[] = [];
  detailPollInterval: any;

  // Add Vehicle modal
  showAddModal = false;
  addForm = { vin: '', make: '', model: '', year: '' };
  addError = '';
  addLoading = false;

  // Assign Vehicle modal
  showAssignModal = false;
  assignVehicle: Vehicle | null = null;
  assignmentOptions: AssignmentCustomerOption[] = [];
  assignSelectedUsername = '';
  assignSwap = false;
  assignError = '';
  assignLoading = false;

  // Fleet trend charts
  public speedChartData: any = {
    labels: [],
    datasets: [
      { data: [], label: 'Fleet Speed (km/h)', fill: true, tension: 0.4, borderColor: '#3b82f6', backgroundColor: 'rgba(59, 130, 246, 0.1)', pointRadius: 1 }
    ]
  };

  public tempChartData: any = {
    labels: [],
    datasets: [
      { data: [], label: 'Engine Temp (°C)', fill: true, tension: 0.4, borderColor: '#ef4444', backgroundColor: 'rgba(239, 68, 68, 0.1)', pointRadius: 1 }
    ]
  };

  public speedChartOptions: any = {
    responsive: true,
    maintainAspectRatio: true,
    interaction: { mode: 'index' as const, intersect: false },
    plugins: {
      legend: { display: true, labels: { boxWidth: 12, font: { size: 11 } } },
      tooltip: { mode: 'index' as const, intersect: false }
    },
    scales: {
      x: {
        display: true,
        grid: { display: false },
        ticks: { maxTicksLimit: 10, font: { size: 10 }, maxRotation: 0 }
      },
      y: {
        beginAtZero: true,
        suggestedMax: 140,
        title: { display: true, text: 'Speed (km/h)', font: { size: 11 } },
        grid: { color: 'rgba(128,128,128,0.12)' },
        ticks: { font: { size: 10 } }
      }
    }
  };

  public tempChartOptions: any = {
    responsive: true,
    maintainAspectRatio: true,
    interaction: { mode: 'index' as const, intersect: false },
    plugins: {
      legend: { display: true, labels: { boxWidth: 12, font: { size: 11 } } },
      tooltip: { mode: 'index' as const, intersect: false }
    },
    scales: {
      x: {
        display: true,
        grid: { display: false },
        ticks: { maxTicksLimit: 10, font: { size: 10 }, maxRotation: 0 }
      },
      y: {
        beginAtZero: true,
        suggestedMax: 130,
        title: { display: true, text: 'Temperature (°C)', font: { size: 11 } },
        grid: { color: 'rgba(128,128,128,0.12)' },
        ticks: { font: { size: 10 } }
      }
    }
  };

  // Vehicle detail hourly chart
  public detailChartData: any = { labels: [], datasets: [] };
  public detailChartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: 'index' as const, intersect: false },
    plugins: {
      legend: { position: 'top' as const, labels: { font: { size: 12 }, boxWidth: 14 } },
      tooltip: { mode: 'index' as const, intersect: false }
    },
    scales: {
      x: {
        ticks: { maxTicksLimit: 12, font: { size: 10 }, maxRotation: 0 },
        grid: { display: false },
        title: { display: true, text: 'Hour (today)', font: { size: 11 } }
      },
      y: {
        type: 'linear' as const,
        display: true,
        position: 'left' as const,
        beginAtZero: true,
        title: { display: true, text: 'Avg speed (km/h)', font: { size: 11 } },
        grid: { color: 'rgba(59, 130, 246, 0.12)' },
        ticks: { font: { size: 10 } }
      },
      y1: {
        type: 'linear' as const,
        display: true,
        position: 'right' as const,
        beginAtZero: true,
        title: { display: true, text: 'Avg temp (°C)', font: { size: 11 } },
        grid: { drawOnChartArea: false },
        ticks: { font: { size: 10 } }
      }
    }
  };

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.username = localStorage.getItem('username') || '';
    this.loadData();
    this.pollInterval = setInterval(() => this.loadData(), 5000);
  }

  ngOnDestroy(): void {
    if (this.pollInterval) { clearInterval(this.pollInterval); }
    if (this.detailPollInterval) { clearInterval(this.detailPollInterval); }
  }

  private loadData(): void {
    this.apiService.getOwnerAnalytics().subscribe({
      next: (data) => this.analytics = data
    });

    this.apiService.getOwnerVehicles().subscribe({
      next: (data) => this.vehicles = data
    });

    this.apiService.getOwnerPeakSpeeds().subscribe({
      next: (data) => this.peakSpeeds = data
    });

    this.apiService.getOwnerTrends().subscribe({
      next: (readings) => this.updateCharts(readings)
    });

    this.apiService.getOwnerAllAlerts().subscribe({
      next: (data) => this.fleetAlerts = data,
      error: () => {}
    });

    this.apiService.getOwnerAssignmentRequests().subscribe({
      next: (data) => this.assignmentRequests = data,
      error: () => {}
    });
  }

  private updateCharts(readings: VehicleReading[]): void {
    const labels = readings.map(r => new Date(r.timestamp).toLocaleTimeString());
    const speeds = readings.map(r => r.speed);
    const temps = readings.map(r => r.temperature);

    this.speedChartData = {
      labels,
      datasets: [
        {
          ...this.speedChartData.datasets[0],
          data: speeds,
          pointRadius: 0,
          pointHoverRadius: 4,
          borderWidth: 2
        }
      ]
    };

    this.tempChartData = {
      labels,
      datasets: [
        {
          ...this.tempChartData.datasets[0],
          data: temps,
          pointRadius: 0,
          pointHoverRadius: 4,
          borderWidth: 2
        }
      ]
    };
  }

  // ---- Vehicle Detail Popup ----
  selectVehicle(vehicle: Vehicle): void {
    this.selectedVehicle = vehicle;
    this.showDetailModal = true;
    if (this.detailPollInterval) { clearInterval(this.detailPollInterval); }
    this.loadVehicleDetails(vehicle);
    this.detailPollInterval = setInterval(() => this.loadVehicleDetails(vehicle), 3000);
  }

  closeDetail(): void {
    this.showDetailModal = false;
    this.selectedVehicle = null;
    this.selectedLatest = null;
    this.selectedHourly = null;
    this.selectedAlerts = [];
    if (this.detailPollInterval) { clearInterval(this.detailPollInterval); }
  }

  private loadVehicleDetails(vehicle: Vehicle): void {
    this.apiService.getOwnerVehicleLatest(vehicle.id).subscribe({
      next: (data) => this.selectedLatest = data,
      error: () => {}
    });

    this.apiService.getOwnerVehicleHourly(vehicle.id).subscribe({
      next: (data) => {
        this.selectedHourly = data;
        this.updateDetailChart();
      },
      error: () => {}
    });

    this.apiService.getOwnerVehicleAlerts(vehicle.id).subscribe({
      next: (data) => this.selectedAlerts = data,
      error: () => {}
    });
  }

  private updateDetailChart(): void {
    if (!this.selectedHourly?.hours) return;
    this.detailChartData = {
      labels: this.selectedHourly.hours.map(h => `${h}:00`),
      datasets: [
        {
          label: 'Avg Speed (km/h)',
          data: this.selectedHourly.avgSpeeds,
          yAxisID: 'y',
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.08)',
          fill: true,
          tension: 0.35,
          pointRadius: 3,
          borderWidth: 2
        },
        {
          label: 'Avg Engine Temp (°C)',
          data: this.selectedHourly.avgTemps,
          yAxisID: 'y1',
          borderColor: '#f97316',
          backgroundColor: 'rgba(249, 115, 22, 0.08)',
          fill: true,
          tension: 0.35,
          pointRadius: 3,
          borderWidth: 2
        }
      ]
    };
  }

  openPeakVehicleDetail(peak: VehiclePeakSpeed): void {
    const match = this.vehicles.find(v => v.id === peak.vehicleId);
    if (match) {
      this.selectVehicle(match);
    }
  }

  onFleetAlertRead(alertId: number): void {
    this.apiService.ownerMarkAlertRead(alertId).subscribe({
      next: () => {
        this.loadData();
        if (this.selectedVehicle) { this.loadVehicleDetails(this.selectedVehicle); }
      }
    });
  }

  onVehicleAlertRead(alertId: number): void {
    this.onFleetAlertRead(alertId);
  }

  markAllAlertsRead(): void {
    this.apiService.ownerMarkAllAlertsRead().subscribe({
      next: () => this.loadData()
    });
  }

  get unreadAlertCount(): number {
    return this.fleetAlerts.filter(a => !a.isRead).length;
  }

  approveRequest(reqId: number): void {
    this.apiService.ownerApproveRequest(reqId).subscribe({
      next: () => this.loadData(),
      error: (err) => alert(err.error?.message || 'Failed to approve request')
    });
  }

  rejectRequest(reqId: number): void {
    this.apiService.ownerRejectRequest(reqId).subscribe({
      next: () => this.loadData(),
      error: (err) => alert(err.error?.message || 'Failed to reject request')
    });
  }

  get selectedIsOverspeeding(): boolean {
    return (this.selectedLatest?.speed ?? 0) > 80;
  }

  // ---- Add Vehicle Modal ----
  openAddModal(): void {
    this.addError = '';
    this.addForm = { vin: '', make: '', model: '', year: '' };
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  submitAddVehicle(): void {
    this.addError = '';
    this.addLoading = true;
    const data: any = {
      vin: this.addForm.vin,
      make: this.addForm.make,
      model: this.addForm.model
    };
    if (this.addForm.year) { data.year = parseInt(this.addForm.year, 10); }

    this.apiService.ownerAddVehicle(data).subscribe({
      next: () => {
        this.showAddModal = false;
        this.addLoading = false;
        this.loadData();
      },
      error: (err) => {
        this.addError = err.error?.message || err.error || 'Failed to add vehicle';
        this.addLoading = false;
      }
    });
  }

  // ---- Delete Vehicle ----
  deleteVehicle(vehicle: Vehicle, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Delete vehicle ${vehicle.vin}? This will remove all its readings.`)) return;
    this.apiService.ownerDeleteVehicle(vehicle.id).subscribe({
      next: () => {
        if (this.selectedVehicle?.id === vehicle.id) { this.closeDetail(); }
        this.loadData();
      },
      error: (err) => alert(err.error || 'Failed to delete vehicle')
    });
  }

  // ---- Assign Vehicle Modal ----
  openAssignModal(vehicle: Vehicle, event: Event): void {
    event.stopPropagation();
    this.assignVehicle = vehicle;
    this.assignSelectedUsername = '';
    this.assignSwap = false;
    this.assignError = '';
    this.assignmentOptions = [];
    this.showAssignModal = true;
    this.apiService.getOwnerAssignmentOptions(vehicle.id).subscribe({
      next: (opts) => { this.assignmentOptions = opts; },
      error: () => { this.assignError = 'Could not load customers for this location'; }
    });
  }

  closeAssignModal(): void {
    this.showAssignModal = false;
    this.assignVehicle = null;
    this.assignmentOptions = [];
  }

  get assignableCustomerOptions(): AssignmentCustomerOption[] {
    if (this.assignSwap) {
      return this.assignmentOptions;
    }
    return this.assignmentOptions.filter(o => !o.hasOtherVehicle && o.username !== 'anynomo');
  }

  get hasCustomersNeedingSwap(): boolean {
    return this.assignmentOptions.some(o => o.hasOtherVehicle);
  }

  submitAssignVehicle(): void {
    if (!this.assignVehicle || !this.assignSelectedUsername.trim()) {
      this.assignError = 'Please select a customer';
      return;
    }
    this.assignLoading = true;
    this.assignError = '';

    this.apiService.ownerAssignVehicle(
      this.assignVehicle.id,
      this.assignSelectedUsername.trim(),
      this.assignSwap
    ).subscribe({
      next: () => {
        this.showAssignModal = false;
        this.assignLoading = false;
        this.loadData();
      },
      error: (err) => {
        this.assignError = err.error?.message || 'Failed to assign vehicle';
        this.assignLoading = false;
      }
    });
  }

  unassignVehicle(vehicle: Vehicle, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Unassign vehicle ${vehicle.vin} from ${vehicle.assignedCustomer?.user?.username}?`)) return;
    this.apiService.ownerUnassignVehicle(vehicle.id).subscribe({
      next: () => this.loadData(),
      error: (err) => alert(err.error?.message || 'Failed to unassign')
    });
  }

  getAssignedUsername(vehicle: Vehicle): string {
    return vehicle.assignedCustomer?.user?.username || '';
  }

  getVehicleImage(vehicle: Vehicle): string {
    return this.emojiForMake(vehicle.make);
  }

  getVehicleImageFromPeak(peak: VehiclePeakSpeed): string {
    return this.emojiForMake(peak.make);
  }

  private emojiForMake(make: string): string {
    const makes: Record<string, string> = {
      Toyota: '🚙',
      Honda: '🚗',
      Tesla: '⚡',
      BMW: '🏎️',
      Mercedes: '🚘',
    };
    return makes[make] || '🚗';
  }
}

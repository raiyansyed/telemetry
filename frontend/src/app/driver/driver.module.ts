/**
 * DriverModule - Angular feature module for the stand-alone Driver view.
 *
 * This module is LAZY-LOADED when a user navigates to /driver.
 * It is a simplified version of the customer dashboard, focused on
 * controlling a single hardcoded vehicle (vehicleId = 1).
 *
 * WHY THIS EXISTS:
 * - The customer dashboard works with dynamically assigned vehicles.
 * - This driver module is for direct testing/demo of vehicle controls
 *   without needing to go through the assignment workflow.
 *
 * IMPORTS:
 * - CommonModule: Angular built-ins like *ngIf, *ngFor, pipes, etc.
 * - FormsModule: Two-way binding [(ngModel)] for the throttle slider.
 * - DriverRoutingModule: Sets up the '' route to DriverComponent.
 * - NgChartsModule: Chart.js integration for the telemetry history chart.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { DriverRoutingModule } from './driver-routing.module';
import { DriverComponent } from './driver.component';
import { NgChartsModule } from 'ng2-charts';

@NgModule({
  declarations: [
    DriverComponent   // The only component in this module
  ],
  imports: [
    CommonModule,       // Provides *ngIf, *ngFor, pipes, etc.
    FormsModule,        // Provides [(ngModel)] for throttle slider
    DriverRoutingModule,// Registers the '' route -> DriverComponent
    NgChartsModule      // Provides <canvas baseChart> for Chart.js
  ]
})
export class DriverModule { }

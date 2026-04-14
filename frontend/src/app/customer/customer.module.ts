/**
 * CustomerModule - Feature module for the Customer/Driver Dashboard.
 *
 * LAZY-LOADED: Only downloaded when the user navigates to /customer.
 *
 * IMPORTS:
 * - CommonModule: *ngIf, *ngFor, [ngClass], pipes, etc.
 * - FormsModule: [(ngModel)] for throttle slider and other inputs.
 * - CustomerRoutingModule: route configuration ('' -> CustomerComponent).
 * - SharedModule: <app-alert-list> for vehicle alerts display.
 * - NgChartsModule: Chart.js charts for telemetry history.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CustomerRoutingModule } from './customer-routing.module';
import { CustomerComponent } from './customer.component';
import { SharedModule } from '../shared/shared.module';
import { NgChartsModule } from 'ng2-charts';

@NgModule({
  declarations: [
    CustomerComponent  // The main customer/driver dashboard component
  ],
  imports: [
    CommonModule,           // *ngIf, *ngFor, [ngClass], etc.
    FormsModule,            // [(ngModel)] for throttle slider
    CustomerRoutingModule,  // Route: '' -> CustomerComponent
    SharedModule,           // <app-alert-list> component
    NgChartsModule          // Chart.js telemetry history chart
  ]
})
export class CustomerModule { }
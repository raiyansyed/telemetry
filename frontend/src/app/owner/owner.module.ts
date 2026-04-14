/**
 * OwnerModule - Feature module for the Fleet Owner Dashboard.
 *
 * WHAT IS A FEATURE MODULE?
 * - A feature module groups all the components, imports, and configuration
 *   for a specific feature of the application.
 * - This module is LAZY-LOADED: it's only downloaded when the user navigates to /owner.
 *   This keeps the initial app bundle smaller and faster to load.
 *
 * DECLARATIONS:
 * - OwnerComponent: the main dashboard component with all the owner UI.
 *
 * IMPORTS:
 * - CommonModule: provides *ngIf, *ngFor, [ngClass], and other common directives.
 *   (BrowserModule is only imported once in AppModule; feature modules use CommonModule.)
 * - FormsModule: enables [(ngModel)] two-way binding for the add/assign vehicle forms.
 * - OwnerRoutingModule: defines the route configuration for this module.
 * - NgChartsModule: provides the <canvas baseChart> directive for Chart.js charts.
 * - SharedModule: provides <app-gauge> and <app-alert-list> reusable components.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { OwnerRoutingModule } from './owner-routing.module';
import { OwnerComponent } from './owner.component';
import { NgChartsModule } from 'ng2-charts';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    OwnerComponent  // The main fleet owner dashboard component
  ],
  imports: [
    CommonModule,        // *ngIf, *ngFor, [ngClass], pipes, etc.
    FormsModule,         // [(ngModel)] for form inputs
    OwnerRoutingModule,  // Route: '' -> OwnerComponent
    NgChartsModule,      // Chart.js charts (speed trend, temperature trend, hourly chart)
    SharedModule         // <app-gauge> and <app-alert-list> components
  ]
})
export class OwnerModule { }
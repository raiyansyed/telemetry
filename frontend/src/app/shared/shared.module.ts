/**
 * SharedModule - Contains reusable UI components shared across multiple feature modules.
 *
 * WHAT IS A SHARED MODULE?
 * - A shared module groups components that are used in MORE THAN ONE feature module.
 * - Instead of declaring GaugeComponent and AlertListComponent in both OwnerModule and CustomerModule,
 *   we declare them once here and import SharedModule in both feature modules.
 *
 * COMPONENTS IN THIS MODULE:
 * - GaugeComponent: A circular gauge that displays speed or temperature with a conic gradient.
 * - AlertListComponent: A scrollable list of alerts with color-coded borders and action buttons.
 *
 * USAGE:
 * - OwnerModule and CustomerModule both have "imports: [SharedModule]".
 * - This allows them to use <app-gauge> and <app-alert-list> in their HTML templates.
 *
 * "exports" makes these components available to any module that imports SharedModule.
 * Without "exports", the components would be private to SharedModule only.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GaugeComponent } from './gauge.component';
import { AlertListComponent } from './alert-list.component';

@NgModule({
  declarations: [
    GaugeComponent,     // Circular gauge for speed/temperature display
    AlertListComponent  // Scrollable alert/notification list
  ],
  imports: [
    CommonModule  // Provides *ngIf, *ngFor, [ngClass], etc.
  ],
  exports: [
    GaugeComponent,     // Make available to importing modules
    AlertListComponent  // Make available to importing modules
  ]
})
export class SharedModule { }
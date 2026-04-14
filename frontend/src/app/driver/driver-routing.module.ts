/**
 * DriverRoutingModule - Routing configuration for the Driver feature.
 *
 * This module defines a single route:
 *   '' (empty path) -> DriverComponent
 *
 * HOW LAZY LOADING WORKS:
 * 1. In app-routing.module.ts, there is: { path: 'driver', loadChildren: ... }
 * 2. When the user navigates to /driver, Angular loads this module.
 * 3. This routing module maps '' (relative to /driver) to DriverComponent.
 * 4. So the final URL is just /driver.
 *
 * RouterModule.forChild() is used (not forRoot) because this is a feature module,
 * not the main app router. forRoot() is only called once in app-routing.module.ts.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DriverComponent } from './driver.component';

/** Single route: the empty path maps to the DriverComponent */
const routes: Routes = [{ path: '', component: DriverComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],  // Register child routes
  exports: [RouterModule]                     // Export so parent module can use <router-outlet>
})
export class DriverRoutingModule { }

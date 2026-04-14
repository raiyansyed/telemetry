/**
 * CustomerRoutingModule - Route configuration for the customer feature module.
 *
 * Single route: '' -> CustomerComponent (displayed when user navigates to /customer).
 * Uses forChild() because this is a feature module (not the root).
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CustomerComponent } from './customer.component';

const routes: Routes = [{ path: '', component: CustomerComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CustomerRoutingModule { }
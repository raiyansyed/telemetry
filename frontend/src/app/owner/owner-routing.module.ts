/**
 * OwnerRoutingModule - Route configuration for the owner feature module.
 *
 * This module defines a single route: when the user navigates to /owner,
 * OwnerComponent is displayed.
 *
 * Note: RouterModule.forChild() is used (not forRoot()) because this is a
 * child/feature module, not the root module. forRoot() is only used once in AppRoutingModule.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { OwnerComponent } from './owner.component';

const routes: Routes = [{ path: '', component: OwnerComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],  // forChild = feature module routing
  exports: [RouterModule]
})
export class OwnerRoutingModule { }
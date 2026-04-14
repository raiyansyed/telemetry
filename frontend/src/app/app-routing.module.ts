/**
 * AppRoutingModule - Defines the URL routes for the entire application.
 *
 * WHAT IS ROUTING?
 * - Routing maps URLs to components. When you navigate to /login, Angular shows LoginComponent.
 * - When you navigate to /owner, it loads OwnerModule (and its OwnerComponent).
 *
 * ROUTE CONFIGURATION:
 *
 * | URL Path   | Component/Module   | Guards        | Notes                         |
 * |------------|-------------------|---------------|-------------------------------|
 * | /login     | LoginComponent    | (none)        | Public - anyone can access    |
 * | /register  | RegisterComponent | (none)        | Public - anyone can access    |
 * | /owner     | OwnerModule       | Auth + Role   | Must be logged in as OWNER    |
 * | /customer  | CustomerModule    | Auth + Role   | Must be logged in as CUSTOMER |
 * | /          | redirects to /login|              | Default redirect              |
 * | /**        | redirects to /login|              | Catch-all for unknown URLs    |
 *
 * LAZY LOADING:
 * - OwnerModule and CustomerModule use "loadChildren" which means they are loaded ON DEMAND.
 * - When you navigate to /owner for the first time, Angular downloads the OwnerModule bundle.
 * - This improves initial page load time because you don't download code you don't need.
 * - The syntax: loadChildren: () => import('./owner/owner.module').then(m => m.OwnerModule)
 *
 * GUARDS:
 * - AuthGuard: checks if the user has a JWT token (is logged in). Redirects to /login if not.
 * - RoleGuard: checks if the user's role matches the route's expected role. Prevents
 *   a CUSTOMER from accessing /owner and vice versa.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './login/register.component';
import { AuthGuard, RoleGuard } from './core/guards';

const routes: Routes = [
  { path: 'login', component: LoginComponent },        // Public login page
  { path: 'register', component: RegisterComponent },  // Public registration page
  {
    path: 'owner',
    loadChildren: () => import('./owner/owner.module').then(m => m.OwnerModule),  // Lazy-loaded
    canActivate: [AuthGuard, RoleGuard],  // Must be logged in AND have OWNER role
    data: { role: 'OWNER' }              // Expected role for RoleGuard
  },
  {
    path: 'customer',
    loadChildren: () => import('./customer/customer.module').then(m => m.CustomerModule),  // Lazy-loaded
    canActivate: [AuthGuard, RoleGuard],  // Must be logged in AND have CUSTOMER role
    data: { role: 'CUSTOMER' }           // Expected role for RoleGuard
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },  // Default: go to login
  { path: '**', redirectTo: '/login' }                    // Catch-all: unknown URLs go to login
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],  // forRoot() = register routes at the app level
  exports: [RouterModule]                   // Export so AppModule can use <router-outlet>
})
export class AppRoutingModule { }
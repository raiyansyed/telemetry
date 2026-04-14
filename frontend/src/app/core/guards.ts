/**
 * guards.ts - Route guards that protect pages from unauthorized access.
 *
 * WHAT IS A ROUTE GUARD?
 * - A guard is a class that Angular checks BEFORE navigating to a route.
 * - If canActivate() returns true, navigation proceeds. If false, it's blocked.
 * - Guards are specified in the route configuration (app-routing.module.ts).
 *
 * TWO GUARDS IN THIS FILE:
 *
 * 1. AuthGuard - Checks if the user is logged in (has a JWT token).
 *    - If token exists in localStorage -> allow navigation.
 *    - If no token -> redirect to /login.
 *
 * 2. RoleGuard - Checks if the user has the correct role for the page.
 *    - Each protected route has a "data: { role: 'OWNER' }" configuration.
 *    - RoleGuard compares the user's role (from localStorage) against the expected role.
 *    - If roles match -> allow navigation.
 *    - If roles don't match -> redirect to /login (e.g., prevents a CUSTOMER from visiting /owner).
 *
 * @Injectable({ providedIn: 'root' }) means Angular creates ONE instance of each guard
 * and makes it available everywhere (singleton pattern).
 */
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';

/**
 * AuthGuard - Ensures the user is logged in before accessing protected routes.
 * Checks for a JWT token in localStorage.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router) {}

  /**
   * Called by Angular before navigating to a guarded route.
   * @returns true if user has a token (logged in), false otherwise.
   */
  canActivate(): boolean {
    const token = localStorage.getItem('token');
    if (token) {
      return true;  // User is logged in, allow access
    }
    this.router.navigate(['/login']);  // Not logged in, redirect to login
    return false;
  }
}

/**
 * RoleGuard - Ensures the user has the correct role for the route.
 * Compares localStorage role against the route's expected role.
 */
@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private router: Router) {}

  /**
   * Called by Angular before navigating to a role-guarded route.
   * @param route The route being navigated to. Contains data.role (e.g., 'OWNER').
   * @returns true if user's role matches the expected role, false otherwise.
   */
  canActivate(route: ActivatedRouteSnapshot): boolean {
    const expectedRole = route.data['role'];       // Expected role from route config
    const userRole = localStorage.getItem('role'); // Actual user role from localStorage

    if (userRole && userRole === expectedRole) {
      return true;  // Roles match, allow access
    }
    this.router.navigate(['/login']);  // Wrong role, redirect to login
    return false;
  }
}
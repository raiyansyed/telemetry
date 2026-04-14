/**
 * AppComponent - The root component that wraps the entire application.
 *
 * WHAT IS A COMPONENT?
 * - A component is the basic building block of an Angular app.
 * - It has 3 parts:
 *   1. TypeScript class (this file) - contains the logic and data.
 *   2. HTML template (app.component.html) - defines what the user sees.
 *   3. SCSS stylesheet (app.component.scss) - defines the styles.
 * - The @Component decorator links these 3 parts together.
 *
 * WHAT THIS COMPONENT DOES:
 * - Renders the top navigation bar (navbar) with:
 *   - App logo and title
 *   - Location selector dropdown (for switching cities)
 *   - Username display
 *   - User role badge (OWNER/CUSTOMER)
 *   - Dark/Light theme toggle switch
 *   - Logout button
 * - Below the navbar: <router-outlet> which displays the current page
 *   (login, register, owner dashboard, or customer dashboard).
 *
 * SELECTOR: 'app-root' - matches the <app-root></app-root> tag in index.html.
 * This is how Angular knows where to render this component.
 */
import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { ThemeService } from './theme.service';
import { ApiService } from './core/api.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  /** The currently selected city (e.g., "Chennai"). Displayed in navbar location selector. */
  activeLocation = '';
  /** Whether the location dropdown menu is open. */
  showLocationMenu = false;
  /** List of supported cities fetched from the backend. Populates the dropdown. */
  locations: string[] = [];
  /** The logged-in user's display name. Shown in navbar as "Welcome, <username>". */
  username = '';
  /** Whether the user's location is locked (owners always, customers when assigned to a vehicle). */
  isLocationLocked = false;

  constructor(
    /** ThemeService is public so the HTML template can access themeService.isDark */
    public themeService: ThemeService,
    /** Router for navigation (redirect to dashboard, logout redirect to login) */
    private router: Router,
    /** ApiService for HTTP calls to the backend */
    private apiService: ApiService
  ) {}

  /**
   * Check if user is logged in by looking for a JWT token in localStorage.
   * Used in the HTML template with *ngIf="isLoggedIn" to show/hide navbar elements.
   */
  get isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  /**
   * Get the current user's role from localStorage ("OWNER" or "CUSTOMER").
   * Used to display the role badge and for conditional UI logic.
   */
  get userRole(): string {
    return localStorage.getItem('role') || '';
  }

  ngOnInit(): void {
    this.apiService.getSupportedLocations().subscribe({
      next: (list) => { this.locations = list; },
      error: () => { this.locations = ['Chennai']; }
    });

    // Hydrate on init
    this.hydrateFromLocalStorage();
    if (this.isLoggedIn) {
      this.loadProfile();
    }

    // Re-hydrate on every navigation (catches post-login redirect)
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe(() => {
      this.hydrateFromLocalStorage();
      if (this.isLoggedIn && !this.username) {
        this.loadProfile();
      }
    });

    // If user is logged in but on the root path, redirect to their dashboard
    if (this.isLoggedIn && this.userRole) {
      const currentUrl = this.router.url;
      if (currentUrl === '/' || currentUrl === '/login') {
        this.router.navigate([`/${this.userRole.toLowerCase()}`]);
      }
    }
  }

  private hydrateFromLocalStorage(): void {
    this.activeLocation = localStorage.getItem('location') || '';
    this.username = localStorage.getItem('username') || '';
    // Immediately lock for owners based on role from localStorage
    if (this.userRole === 'OWNER') {
      this.isLocationLocked = true;
    }
  }

  private loadProfile(): void {
    this.apiService.getUserProfile().subscribe({
      next: (profile: any) => {
        this.username = profile.username;
        this.activeLocation = profile.location || this.activeLocation;
        localStorage.setItem('username', profile.username);
        if (profile.location) {
          localStorage.setItem('location', profile.location);
        }
        // Owner location is always locked; assigned customers are also locked
        if (this.userRole === 'OWNER') {
          this.isLocationLocked = true;
        } else if (this.userRole === 'CUSTOMER') {
          this.isLocationLocked = profile.isAssigned === true;
        }
      },
      error: () => {}
    });
  }

  changeLocation(location: string): void {
    if (this.isLocationLocked) return;
    this.activeLocation = location;
    this.showLocationMenu = false;
    localStorage.setItem('location', location);
    this.apiService.updateLocation(location).subscribe({
      error: () => {} // Silently handle errors
    });
  }

  toggleLocationMenu(): void {
    if (this.isLocationLocked) return;
    this.showLocationMenu = !this.showLocationMenu;
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('location');
    localStorage.removeItem('username');
    this.activeLocation = '';
    this.username = '';
    this.isLocationLocked = false;
    this.router.navigate(['/login']);
  }
}

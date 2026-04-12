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

  activeLocation = '';
  showLocationMenu = false;
  locations: string[] = [];
  username = '';
  isLocationLocked = false;

  constructor(
    public themeService: ThemeService,
    private router: Router,
    private apiService: ApiService
  ) {}

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

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

import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { ThemeService } from './theme.service';
import { ApiService } from './core/api.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
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

    this.hydrateFromLocalStorage();
    if (this.isLoggedIn) {
      this.loadProfile();
    }

    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe(() => {
      this.hydrateFromLocalStorage();
      if (this.isLoggedIn && !this.username) {
        this.loadProfile();
      }
    });

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
      error: () => {} 
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

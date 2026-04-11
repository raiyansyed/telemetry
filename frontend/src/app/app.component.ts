import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ThemeService } from './theme.service';
import { ApiService } from './core/api.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  activeLocation = '';
  showLocationMenu = false;
  locations: string[] = [];

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
    this.activeLocation = localStorage.getItem('location') || '';
    this.apiService.getSupportedLocations().subscribe({
      next: (list) => { this.locations = list; },
      error: () => { this.locations = ['Chennai']; }
    });

    // If user is logged in but on the root path, redirect to their dashboard
    if (this.isLoggedIn && this.userRole) {
      const currentUrl = this.router.url;
      if (currentUrl === '/' || currentUrl === '/login') {
        this.router.navigate([`/${this.userRole.toLowerCase()}`]);
      }
    }
  }

  changeLocation(location: string): void {
    this.activeLocation = location;
    this.showLocationMenu = false;
    localStorage.setItem('location', location);
    this.apiService.updateLocation(location).subscribe({
      error: () => {} // Silently handle errors
    });
  }

  toggleLocationMenu(): void {
    this.showLocationMenu = !this.showLocationMenu;
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('location');
    this.activeLocation = '';
    this.router.navigate(['/login']);
  }
}

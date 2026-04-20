import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { ThemeService } from '../theme.service';

@Component({
  selector: 'app-login',
  template: `
    <div class="min-h-[80vh] flex items-center justify-center">
      <div class="w-full max-w-md bg-[var(--card)] border border-[var(--border)] rounded-2xl shadow-xl p-8">
        <div class="text-center mb-8">
          <img src="assets/logo.png" alt="Logo" class="h-16 w-16 mx-auto mb-2 object-contain">
          <h2 class="text-2xl font-extrabold text-[var(--text)]">Vehicle Telemetry System</h2>
          <p class="text-sm text-[var(--muted)] mt-1">Sign in to access your dashboard</p>
        </div>

        <div class="space-y-5">
          <div>
            <label class="block text-xs font-semibold text-[var(--muted)] uppercase tracking-wider mb-1.5">Username</label>
            <input
              [(ngModel)]="username"
              type="text"
              placeholder="e.g. owner"
              class="w-full px-4 py-2.5 border border-[var(--border)] rounded-lg bg-[var(--bg)] text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
            >
          </div>
          <div>
            <label class="block text-xs font-semibold text-[var(--muted)] uppercase tracking-wider mb-1.5">Password</label>
            <input
              [(ngModel)]="password"
              type="password"
              placeholder="••••••••"
              (keyup.enter)="login()"
              class="w-full px-4 py-2.5 border border-[var(--border)] rounded-lg bg-[var(--bg)] text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
            >
          </div>

          <div *ngIf="error" class="text-red-500 text-sm text-center font-medium">{{ error }}</div>

          <button
            (click)="login()"
            [disabled]="loading"
            class="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-semibold py-2.5 px-4 rounded-lg transition duration-150 shadow-md hover:shadow-lg"
          >
            <span *ngIf="!loading">Sign In</span>
            <span *ngIf="loading">Signing in...</span>
          </button>
        </div>

        <div class="mt-6 border-t border-[var(--border)] pt-4">
          <p class="text-xs text-[var(--muted)] text-center mb-2">Demo Credentials</p>
          <div class="grid grid-cols-2 gap-2 text-center">
            <button (click)="fillDemo('Kapoor')" class="text-xs px-2 py-1.5 border border-[var(--border)] rounded-md hover:bg-blue-50 hover:border-blue-300 transition text-[var(--text)]">
              👔 Owner
            </button>
            <button (click)="fillDemo('Raiyan')" class="text-xs px-2 py-1.5 border border-[var(--border)] rounded-md hover:bg-purple-50 hover:border-purple-300 transition text-[var(--text)]">
              👤 Customer
            </button>
          </div>
        </div>

        <div class="mt-4 text-center text-sm text-[var(--muted)]">
          Don't have an account?
          <a routerLink="/register" class="text-blue-500 hover:text-blue-600 font-medium">Register</a>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {}

  fillDemo(role: string) {
    this.username = role;
    this.password = 'password';
  }

  login() {
    if (!this.username || !this.password) {
      this.error = 'Please enter username and password';
      return;
    }

    this.loading = true;
    this.error = '';

    this.apiService.login({ username: this.username, password: this.password }).subscribe({
      next: (res) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        localStorage.setItem('location', res.location || '');
        localStorage.setItem('username', res.username || this.username);
        this.loading = false;
        this.router.navigate([`/${res.role.toLowerCase()}`]);
      },
      error: () => {
        this.error = 'Invalid credentials. Please try again.';
        this.loading = false;
      }
    });
  }
}

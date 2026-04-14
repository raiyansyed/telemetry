/**
 * RegisterComponent - The account creation page.
 *
 * This component uses an INLINE TEMPLATE (HTML written directly in this file).
 * It allows new users to create either an OWNER or CUSTOMER account.
 *
 * FEATURES:
 * - Username & password fields (required for all users).
 * - Role dropdown: CUSTOMER (default) or OWNER.
 * - Location dropdown: populated from GET /api/auth/locations (backend returns
 *   the list of supported cities like Chennai, Mumbai, Delhi, etc.).
 * - CONDITIONAL FIELDS based on role:
 *   - If OWNER → shows "Company Name" input.
 *   - If CUSTOMER → shows "License Number" input.
 * - Form validation (username & password required).
 * - Loading state while API call is in progress.
 * - Error handling (shows backend error message, e.g., "Username already exists").
 *
 * REGISTRATION FLOW:
 * 1. User fills in the form and clicks "Create Account".
 * 2. register() method sends POST /api/auth/register with the form data.
 * 3. Backend creates User + OwnerDetails or CustomerDetails records.
 * 4. Backend returns { token, role, location, username } (auto-login).
 * 5. Token + user info stored in localStorage.
 * 6. Router navigates to /{role} dashboard.
 *
 * ON INIT:
 * - Fetches supported locations from the backend to populate the dropdown.
 * - Falls back to ['Chennai'] if the API call fails.
 */
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';

@Component({
  selector: 'app-register',
  template: `
    <div class="min-h-[80vh] flex items-center justify-center">
      <div class="w-full max-w-md bg-[var(--card)] border border-[var(--border)] rounded-2xl shadow-xl p-8">
        <div class="text-center mb-8">
          <img src="assets/logo.png" alt="Logo" class="h-16 w-16 mx-auto mb-2 object-contain">
          <h2 class="text-2xl font-extrabold text-[var(--text)]">Create Account</h2>
          <p class="text-sm text-[var(--muted)] mt-1">Join Vehicle Telemetry System</p>
        </div>

        <div *ngIf="error" class="bg-red-50 border border-red-200 text-red-600 text-sm text-center font-medium p-3 rounded-lg mb-4">{{ error }}</div>

        <form (ngSubmit)="register()">
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-semibold text-[var(--muted)] uppercase tracking-wider mb-1.5">Username</label>
              <input
                [(ngModel)]="form.username"
                name="username"
                type="text"
                placeholder="Choose a username"
                required
                class="w-full px-4 py-2.5 border border-[var(--border)] rounded-lg bg-[var(--bg)] text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              >
            </div>

            <div>
              <label class="block text-xs font-semibold text-[var(--muted)] uppercase tracking-wider mb-1.5">Password</label>
              <input
                [(ngModel)]="form.password"
                name="password"
                type="password"
                placeholder="••••••••"
                required
                class="w-full px-4 py-2.5 border border-[var(--border)] rounded-lg bg-[var(--bg)] text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              >
            </div>

            <div>
              <label class="block text-xs font-semibold text-[var(--muted)] uppercase tracking-wider mb-1.5">Role</label>
              <select
                [(ngModel)]="form.role"
                name="role"
                class="w-full px-4 py-2.5 border border-[var(--border)] rounded-lg bg-[var(--bg)] text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              >
                <option value="CUSTOMER">Customer</option>
                <option value="OWNER">Fleet Owner</option>
              </select>
            </div>

            <!-- Location -->
            <div>
              <label class="block text-xs font-semibold text-[var(--muted)] uppercase tracking-wider mb-1.5">📍 Location</label>
              <select
                [(ngModel)]="form.location"
                name="location"
                class="w-full px-4 py-2.5 border border-[var(--border)] rounded-lg bg-[var(--bg)] text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              >
                <option *ngFor="let loc of locations" [value]="loc">{{ loc }}</option>
              </select>
            </div>

            <!-- Owner-specific field -->
            <div *ngIf="form.role === 'OWNER'">
              <label class="block text-xs font-semibold text-[var(--muted)] uppercase tracking-wider mb-1.5">Company Name</label>
              <input
                [(ngModel)]="form.companyName"
                name="companyName"
                type="text"
                placeholder="e.g. Fast Fleet Inc."
                class="w-full px-4 py-2.5 border border-[var(--border)] rounded-lg bg-[var(--bg)] text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              >
            </div>

            <!-- Customer-specific fields -->
            <div *ngIf="form.role === 'CUSTOMER'">
              <label class="block text-xs font-semibold text-[var(--muted)] uppercase tracking-wider mb-1.5">License Number</label>
              <input
                [(ngModel)]="form.licenseNumber"
                name="licenseNumber"
                type="text"
                placeholder="e.g. DL-1234567"
                class="w-full px-4 py-2.5 border border-[var(--border)] rounded-lg bg-[var(--bg)] text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              >
            </div>

            <button
              type="submit"
              [disabled]="loading"
              class="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-semibold py-2.5 px-4 rounded-lg transition duration-150 shadow-md hover:shadow-lg"
            >
              <span *ngIf="!loading">Create Account</span>
              <span *ngIf="loading">Creating…</span>
            </button>
          </div>
        </form>

        <div class="mt-6 text-center text-sm text-[var(--muted)]">
          Already have an account?
          <a routerLink="/login" class="text-blue-500 hover:text-blue-600 font-medium">Sign In</a>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class RegisterComponent implements OnInit {
  form = {
    username: '',
    password: '',
    role: 'CUSTOMER',
    companyName: '',
    licenseNumber: '',
    address: '',
    location: ''
  };
  error = '';
  loading = false;

  locations: string[] = [];

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.apiService.getSupportedLocations().subscribe({
      next: (list) => {
        this.locations = list;
        if (!this.form.location && list.length > 0) {
          this.form.location = list[0];
        }
      },
      error: () => {
        this.locations = ['Chennai'];
        this.form.location = 'Chennai';
      }
    });
  }

  register() {
    if (!this.form.username || !this.form.password) {
      this.error = 'Please fill in all required fields';
      return;
    }

    this.loading = true;
    this.error = '';

    this.apiService.register(this.form).subscribe({
      next: (res) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        localStorage.setItem('location', res.location || this.form.location);
        localStorage.setItem('username', res.username || this.form.username);
        this.loading = false;
        this.router.navigate([`/${res.role.toLowerCase()}`]);
      },
      error: (err) => {
        this.error = err.error?.message || err.error || 'Registration failed. Username may already exist.';
        this.loading = false;
      }
    });
  }
}

/**
 * AuthInterceptor - Automatically attaches the JWT token to every HTTP request.
 *
 * WHAT IS AN INTERCEPTOR?
 * - An interceptor is middleware that runs for EVERY HTTP request made by HttpClient.
 * - It can modify the request before it's sent (add headers, transform data, etc.)
 * - It can also handle errors from the response.
 *
 * WHAT THIS INTERCEPTOR DOES:
 * 1. BEFORE each request: checks if a JWT token exists in localStorage.
 *    If so, adds the "Authorization: Bearer <token>" header to the request.
 *    This is how the Spring Boot backend knows who is making the request.
 *
 * 2. AFTER each response: checks if the response is a 401 (Unauthorized) error.
 *    If so, clears the token from localStorage and redirects to the login page.
 *    This handles token expiration — after 24 hours, the token becomes invalid
 *    and the user must log in again.
 *
 * WITHOUT THIS INTERCEPTOR:
 * - Every API call would need to manually add the Authorization header.
 * - Example: this.http.get(url, { headers: { Authorization: 'Bearer ...' } })
 * - The interceptor does this automatically for ALL requests.
 *
 * REGISTERED IN: app.module.ts via { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor }
 */
import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  /**
   * Intercept every outgoing HTTP request.
   *
   * @param request The original HTTP request.
   * @param next    The next handler in the chain (eventually sends the request to the server).
   * @returns An Observable of the HTTP response.
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Step 1: Get the JWT token from localStorage
    const token = localStorage.getItem('token');

    // Step 2: If a token exists, clone the request and add the Authorization header
    // (HTTP requests are immutable in Angular, so we must clone to modify)
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`  // Format: "Bearer eyJhbGci..."
        }
      });
    }

    // Step 3: Send the request and handle any errors in the response
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Step 4: If server returns 401 (Unauthorized), the token is invalid/expired
        if (error.status === 401) {
          localStorage.removeItem('token');  // Clear invalid token
          localStorage.removeItem('role');   // Clear role
          this.router.navigate(['/login']);   // Redirect to login page
        }
        return throwError(() => error);  // Re-throw the error for the component to handle
      })
    );
  }
}
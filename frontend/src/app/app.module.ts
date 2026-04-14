/**
 * AppModule - The ROOT module of the Angular application.
 *
 * WHAT IS AN ANGULAR MODULE?
 * - Angular organizes code into "modules" - groups of related components, services, and other code.
 * - Every Angular app has exactly one root module (AppModule) that bootstraps the application.
 * - Other modules (OwnerModule, CustomerModule) are "feature modules" loaded on demand.
 *
 * @NgModule PROPERTIES:
 * - declarations: Components that BELONG to this module. These are created and managed here.
 *   Only LoginComponent, RegisterComponent, and AppComponent are declared here.
 *   Owner/Customer/Driver components are in their own lazy-loaded modules.
 *
 * - imports: Other modules whose features this module needs.
 *   BrowserModule: required for any Angular app running in a browser.
 *   AppRoutingModule: defines the URL routes (which URL shows which component).
 *   HttpClientModule: enables making HTTP requests to the backend API.
 *   FormsModule: enables [(ngModel)] two-way data binding in forms.
 *
 * - providers: Services and configuration available app-wide.
 *   The HTTP_INTERCEPTORS entry registers AuthInterceptor to automatically attach
 *   the JWT token to every outgoing HTTP request. "multi: true" means multiple
 *   interceptors can be registered (we only have one, but it's required syntax).
 *
 * - bootstrap: The component that Angular creates first when the app starts.
 *   AppComponent renders the navbar and <router-outlet> where pages are displayed.
 */
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './login/register.component';
import { AuthInterceptor } from './core/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,       // The root component (navbar + router-outlet)
    LoginComponent,     // The login page
    RegisterComponent   // The registration page
  ],
  imports: [
    BrowserModule,      // Required for browser-based Angular apps
    AppRoutingModule,   // URL routing configuration
    HttpClientModule,   // Enables HTTP requests to the Spring Boot backend
    FormsModule         // Enables [(ngModel)] two-way binding in login/register forms
  ],
  providers: [
    // Register AuthInterceptor to automatically add JWT token to all HTTP requests
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]  // AppComponent is the first component loaded
})
export class AppModule { }
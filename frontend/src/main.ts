/**
 * main.ts - The entry point for the Angular application.
 *
 * WHAT THIS FILE DOES:
 * - This is the FIRST file that runs when you open the app in a browser.
 * - platformBrowserDynamic() creates a platform for running Angular in a web browser.
 * - bootstrapModule(AppModule) loads the root module (AppModule) which then loads
 *   all components, services, and routes.
 *
 * ANGULAR STARTUP SEQUENCE:
 *   1. Browser loads index.html
 *   2. index.html has <app-root></app-root> tag
 *   3. This file runs and bootstraps AppModule
 *   4. AppModule declares AppComponent (which uses the 'app-root' selector)
 *   5. Angular replaces <app-root> with AppComponent's template (the navbar + router-outlet)
 *   6. The router loads the appropriate page (login, owner dashboard, customer dashboard)
 */
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';


platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
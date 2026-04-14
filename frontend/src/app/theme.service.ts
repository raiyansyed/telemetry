/**
 * ThemeService - Manages dark/light theme switching for the entire application.
 *
 * HOW THEMING WORKS:
 * 1. The theme is stored as a "data-theme" attribute on the <html> element.
 *    - data-theme="dark" -> dark mode
 *    - data-theme="light" (or absent) -> light mode
 *
 * 2. CSS variables in styles.scss change based on [data-theme="dark"]:
 *    - --bg changes from #f6f6f6 (light gray) to #0f172a (dark navy)
 *    - --text changes from #0f172a (dark) to #f8fafc (light)
 *    - --card, --muted, --border, etc. all change accordingly
 *
 * 3. All components use these CSS variables (e.g., bg-[var(--card)]) instead of
 *    hardcoded colors, so the entire UI theme changes instantly.
 *
 * 4. The theme preference is persisted in localStorage so it survives page refreshes.
 *
 * 5. The navbar has a toggle switch that calls toggleTheme() to switch between modes.
 *
 * @Injectable({ providedIn: 'root' }) = singleton service available everywhere.
 * "providedIn: 'root'" means Angular creates one instance for the entire app.
 */
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  /** localStorage key used to persist the theme preference */
  private readonly themeKey = 'theme';

  /** On service creation (app startup), restore the saved theme from localStorage */
  constructor() {
    this.initTheme();
  }

  /**
   * Initialize theme from localStorage.
   * If the user previously selected dark mode, it will be restored on page load.
   */
  initTheme() {
    const savedTheme = localStorage.getItem(this.themeKey);
    if (savedTheme) {
      document.documentElement.setAttribute('data-theme', savedTheme);
    }
  }

  /**
   * Toggle between light and dark themes.
   * Updates the HTML attribute AND saves to localStorage for persistence.
   */
  toggleTheme() {
    const nextTheme = this.isDark ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', nextTheme);
    localStorage.setItem(this.themeKey, nextTheme);
  }

  /**
   * Check if dark mode is currently active.
   * Used by the navbar template to position the theme toggle switch knob.
   */
  get isDark(): boolean {
    return document.documentElement.getAttribute('data-theme') === 'dark';
  }
}
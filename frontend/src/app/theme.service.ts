import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly themeKey = 'theme';

  constructor() {
    this.initTheme();
  }

  initTheme() {
    const savedTheme = localStorage.getItem(this.themeKey);
    if (savedTheme) {
      document.documentElement.setAttribute('data-theme', savedTheme);
    }
  }

  toggleTheme() {
    const nextTheme = this.isDark ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', nextTheme);
    localStorage.setItem(this.themeKey, nextTheme);
  }

  get isDark(): boolean {
    return document.documentElement.getAttribute('data-theme') === 'dark';
  }
}

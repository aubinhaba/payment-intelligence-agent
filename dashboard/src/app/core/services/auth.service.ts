import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly STORAGE_KEY = 'pia_auth';

  readonly isAuthenticated = signal(AuthService.hasSession());

  private static hasSession(): boolean {
    try {
      return localStorage.getItem(AuthService.STORAGE_KEY) === 'true';
    } catch {
      return false;
    }
  }

  private static readonly DEMO_EMAIL = 'aubin.haba@gmail.com';
  private static readonly DEMO_PASSWORD = 'pia123';

  login(email: string, password: string): boolean {
    if (email !== AuthService.DEMO_EMAIL || password !== AuthService.DEMO_PASSWORD) {
      return false;
    }
    localStorage.setItem(AuthService.STORAGE_KEY, 'true');
    this.isAuthenticated.set(true);
    return true;
  }

  logout(): void {
    localStorage.removeItem(AuthService.STORAGE_KEY);
    this.isAuthenticated.set(false);
  }
}

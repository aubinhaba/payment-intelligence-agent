import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  CardModule,
  FormModule,
  ButtonModule,
  AlertModule,
  SpinnerModule,
} from '@coreui/angular';
import { IconModule } from '@coreui/icons-angular';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CardModule, FormModule, ButtonModule, AlertModule, SpinnerModule, IconModule],
  template: `
    <div class="min-vh-100 d-flex align-items-center justify-content-center bg-body-tertiary px-3">
      <div style="width: 100%; max-width: 420px;">

        <!-- Branding -->
        <div class="text-center mb-4">
          <div class="d-inline-flex align-items-center gap-2 mb-2">
            <div class="rounded-2 bg-primary d-flex align-items-center justify-content-center"
                 style="width: 44px; height: 44px;">
              <svg cIcon name="cilShieldAlt" class="text-white" size="lg"></svg>
            </div>
            <span class="fs-3 fw-bold lh-1">PIA</span>
          </div>
          <p class="text-body-secondary small mb-0">Payment Intelligence Agent</p>
        </div>

        <!-- Login card -->
        <c-card>
          <c-card-body class="p-4">
            <h5 class="text-center mb-4">Sign in to your account</h5>

            @if (error()) {
              <c-alert color="danger" [dismissible]="true" (visibleChange)="error.set('')">
                {{ error() }}
              </c-alert>
            }

            <form (ngSubmit)="submit()" #loginForm="ngForm" novalidate>

              <!-- Email -->
              <div class="mb-3">
                <label cLabel for="email" class="mb-1">Email address</label>
                <input
                  cFormControl
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  [(ngModel)]="email"
                  name="email"
                  autocomplete="email"
                  required
                />
              </div>

              <!-- Password -->
              <div class="mb-4">
                <label cLabel for="password" class="mb-1">Password</label>
                <c-input-group>
                  <input
                    cFormControl
                    id="password"
                    [type]="showPassword ? 'text' : 'password'"
                    placeholder="••••••••"
                    [(ngModel)]="password"
                    name="password"
                    autocomplete="current-password"
                    required
                  />
                  <button
                    cButton
                    type="button"
                    color="secondary"
                    variant="outline"
                    (click)="showPassword = !showPassword"
                    [attr.aria-label]="showPassword ? 'Hide password' : 'Show password'"
                  >
                    <svg cIcon [name]="showPassword ? 'cilLockUnlocked' : 'cilLockLocked'" size="sm"></svg>
                  </button>
                </c-input-group>
              </div>

              <!-- Submit -->
              <button
                cButton
                color="primary"
                type="submit"
                class="w-100"
                [disabled]="loading()"
              >
                @if (loading()) {
                  <c-spinner size="sm" class="me-2" />
                }
                Sign in
              </button>

            </form>
          </c-card-body>
        </c-card>

        <!-- Demo notice -->
        <p class="text-center text-body-secondary small mt-3 mb-0">
          Demo mode — backend auth not yet implemented
        </p>

      </div>
    </div>
  `,
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  password = '';
  showPassword = false;

  readonly loading = signal(false);
  readonly error = signal('');

  submit(): void {
    this.error.set('');
    if (!this.email.trim() || !this.password.trim()) {
      this.error.set('Please enter your email and password.');
      return;
    }
    this.loading.set(true);
    // Simulate a short async delay for realism
    setTimeout(() => {
      const ok = this.auth.login(this.email, this.password);
      this.loading.set(false);
      if (ok) {
        this.router.navigate(['/']);
      } else {
        this.error.set('Invalid credentials. Please try again.');
      }
    }, 600);
  }
}

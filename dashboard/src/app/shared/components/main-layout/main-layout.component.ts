import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet, RouterLink, Router } from '@angular/router';
import {
  SidebarModule,
  HeaderModule,
  BreadcrumbModule,
  ContainerComponent,
  FooterComponent,
  ButtonModule,
  DropdownModule,
} from '@coreui/angular';
import { IconModule } from '@coreui/icons-angular';
import { navItems } from '../../data/nav-items';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    SidebarModule,
    HeaderModule,
    BreadcrumbModule,
    ContainerComponent,
    FooterComponent,
    ButtonModule,
    DropdownModule,
    IconModule,
  ],
  template: `
    <c-sidebar id="pia-sidebar" colorScheme="dark" position="fixed" [visible]="true">
      <c-sidebar-header>
        <c-sidebar-brand [routerLink]="'/'">
          <span class="fw-bold">PIA</span>
        </c-sidebar-brand>
      </c-sidebar-header>
      <c-sidebar-nav [navItems]="navItems" dropdownMode="close" />
    </c-sidebar>

    <div class="wrapper d-flex flex-column min-vh-100">
      <c-header class="mb-4 d-print-none" position="sticky">
        <c-container [fluid]="true" class="border-bottom px-4">
          <button cHeaderToggler [cSidebarToggle]="'pia-sidebar'" class="ps-1" aria-label="Toggle navigation">
            <svg cIcon name="cilMenu" size="lg"></svg>
          </button>
          <c-breadcrumb-router class="d-none d-md-flex ms-3" />

          <div class="ms-auto d-flex align-items-center gap-2">

            <!-- Notification bell -->
            <div cDropdown placement="bottom-end">
              <button cButton cDropdownToggle color="secondary" variant="ghost" size="sm"
                      class="position-relative" aria-label="Notifications">
                <svg cIcon name="cilBell" size="sm"></svg>
                @if (alertCount() > 0) {
                  <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                        style="font-size: 0.55rem; padding: 3px 5px;">
                    {{ alertCount() > 99 ? '99+' : alertCount() }}
                  </span>
                }
              </button>
              <div cDropdownMenu class="dropdown-menu-end" style="min-width: 280px;">
                <h6 cDropdownHeader class="d-flex justify-content-between align-items-center">
                  Alerts
                  @if (alertCount() > 0) {
                    <span class="badge bg-danger">{{ alertCount() }}</span>
                  }
                </h6>
                @if (alertCount() === 0) {
                  <div class="px-3 py-2 text-body-secondary small">No active alerts</div>
                } @else {
                  <div class="px-3 py-2 text-body-secondary small">
                    {{ alertCount() }} HIGH / CRITICAL anomaly{{ alertCount() > 1 ? 'ies' : '' }} require attention
                  </div>
                  <hr class="dropdown-divider my-1">
                  <a class="dropdown-item small" [routerLink]="['/anomalies']">View all anomalies →</a>
                }
              </div>
            </div>

            <!-- Avatar + user dropdown -->
            <div cDropdown placement="bottom-end">
              <button cDropdownToggle class="btn btn-ghost-secondary btn-sm p-0 border-0 rounded-circle"
                      aria-label="User menu" style="line-height: 0;">
                <img
                  src="assets/avatar.png"
                  alt="Aubin HABA"
                  class="rounded-circle"
                  style="width: 34px; height: 34px; object-fit: cover; border: 2px solid var(--cui-border-color);"
                />
              </button>
              <div cDropdownMenu class="dropdown-menu-end">
                <div class="px-3 py-2">
                  <div class="fw-semibold small">Aubin HABA</div>
                  <div class="text-body-secondary" style="font-size: 0.75rem;">aubin.haba&#64;gmail.com</div>
                </div>
                <hr class="dropdown-divider my-1">
                <button cDropdownItem (click)="logout()" class="d-flex align-items-center gap-2">
                  <svg cIcon name="cilAccountLogout" size="sm"></svg>
                  Sign out
                </button>
              </div>
            </div>

          </div>
        </c-container>
      </c-header>

      <div class="body flex-grow-1">
        <c-container [fluid]="true" class="px-4">
          <router-outlet />
        </c-container>
      </div>

      <c-footer class="px-4">
        <div>
          <span class="me-1">PIA</span>
          <span class="text-body-secondary">— Payment Intelligence Agent v1.0.0</span>
        </div>
        <div class="ms-auto">
          <span class="text-body-secondary me-1">Built by</span>
          <a href="https://github.com/aubinhaba" target="_blank" rel="noopener" class="text-decoration-none">Aubin HABA</a>
        </div>
      </c-footer>
    </div>
  `,
  styles: `
    .wrapper {
      margin-inline-start: var(--cui-sidebar-occupy-start, 0);
      transition: margin-inline-start 0.3s ease-in-out;
    }
  `,
})
export class MainLayoutComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);

  readonly navItems = navItems;
  readonly alertCount = signal(0);

  ngOnInit(): void {
    this.api.getAnomalies(0, 100).subscribe({
      next: (resp) => {
        const count = resp.content.filter(
          (a) => a.severity === 'HIGH' || a.severity === 'CRITICAL'
        ).length;
        this.alertCount.set(count);
      },
      error: () => {},
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}

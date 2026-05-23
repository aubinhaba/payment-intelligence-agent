import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  CardModule,
  GridModule,
  BadgeModule,
  ButtonModule,
  AlertModule,
  SpinnerModule,
} from '@coreui/angular';
import { IconModule } from '@coreui/icons-angular';
import { ApiService } from '../../core/services/api.service';

interface HealthDetail {
  name: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  details?: string;
}

type ThemeColor = 'success' | 'danger' | 'secondary';

@Component({
  selector: 'app-health',
  standalone: true,
  imports: [
    DatePipe,
    CardModule,
    GridModule,
    BadgeModule,
    ButtonModule,
    AlertModule,
    SpinnerModule,
    IconModule,
  ],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-start mb-4 gap-3">
      <div>
        <h1 class="mb-1">System Health</h1>
        <p class="text-body-secondary mb-0">
          Spring Boot Actuator — last checked at {{ checkedAt() | date:'HH:mm:ss' }}
        </p>
      </div>
      <button
        cButton
        color="secondary"
        variant="outline"
        size="sm"
        (click)="refresh()"
        [disabled]="loading()"
      >
        <svg cIcon name="cilReload" size="sm" class="me-1"></svg>
        Refresh
      </button>
    </div>

    @if (loading()) {
      <div class="d-flex justify-content-center py-5">
        <c-spinner color="primary" />
      </div>
    } @else {
      <c-alert [color]="overallUp() ? 'success' : 'danger'" class="d-flex align-items-center justify-content-between mb-4">
        <div>
          <strong>{{ overallUp() ? 'All systems operational' : 'Service degraded' }}</strong>
          <div class="small">
            {{ components().length }} component{{ components().length !== 1 ? 's' : '' }} monitored
          </div>
        </div>
        <div class="font-monospace small">
          <strong>{{ upCount() }}</strong> / {{ components().length }} UP
        </div>
      </c-alert>

      <c-row [xs]="1" [sm]="2" [lg]="3" [xl]="4" class="g-4">
        @for (c of components(); track c.name) {
          <c-col>
            <c-card class="h-100 border-start border-4" [class]="borderClass(c.status)">
              <c-card-body>
                <div class="d-flex justify-content-between align-items-center">
                  <span class="fw-semibold text-capitalize text-truncate">{{ c.name }}</span>
                  <c-badge [color]="statusColor(c.status)">{{ c.status }}</c-badge>
                </div>
                @if (c.details) {
                  <p class="small font-monospace text-body-secondary mt-2 mb-0" style="word-break: break-all;">
                    {{ c.details }}
                  </p>
                }
              </c-card-body>
            </c-card>
          </c-col>
        }

        @if (components().length === 0) {
          <c-col [xs]="12">
            <c-card>
              <c-card-body class="text-center text-body-secondary py-5">
                <p class="mb-1">No components detected</p>
                <small>Check actuator endpoint configuration</small>
              </c-card-body>
            </c-card>
          </c-col>
        }
      </c-row>
    }
  `,
})
export class HealthComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly overallUp = signal(false);
  readonly components = signal<HealthDetail[]>([]);
  readonly checkedAt = signal(new Date());

  readonly upCount = computed(() => this.components().filter((c) => c.status === 'UP').length);

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.checkedAt.set(new Date());
    this.api.getHealth().subscribe({
      next: (raw) => {
        const health = raw as Record<string, unknown>;
        this.overallUp.set((health['status'] as string) === 'UP');
        const comps =
          (health['components'] as Record<
            string,
            { status: string; details?: Record<string, unknown> }
          >) ?? {};
        this.components.set(
          Object.entries(comps).map(([name, v]) => ({
            name,
            status: (v.status ?? 'UNKNOWN') as 'UP' | 'DOWN' | 'UNKNOWN',
            details: v.details ? JSON.stringify(v.details).slice(0, 120) : undefined,
          }))
        );
        this.loading.set(false);
      },
      error: () => {
        this.overallUp.set(false);
        this.components.set([
          { name: 'API', status: 'DOWN', details: 'Unable to reach /actuator/health' },
        ]);
        this.loading.set(false);
      },
    });
  }

  statusColor(status: HealthDetail['status']): ThemeColor {
    if (status === 'UP') return 'success';
    if (status === 'DOWN') return 'danger';
    return 'secondary';
  }

  borderClass(status: HealthDetail['status']): string {
    if (status === 'UP') return 'border-success';
    if (status === 'DOWN') return 'border-danger';
    return 'border-secondary';
  }
}

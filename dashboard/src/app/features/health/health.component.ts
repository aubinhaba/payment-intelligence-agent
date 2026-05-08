import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

interface HealthDetail {
  name: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  details?: string;
}

@Component({
  selector: 'app-health',
  standalone: true,
  imports: [DatePipe],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>System Health</h1>
        <p>Spring Boot Actuator — checked at {{ checkedAt() | date: 'HH:mm:ss' }}</p>
      </div>

      @if (loading()) {
        <div class="loading-state"><span>Checking health…</span></div>
      } @else {
        <div class="overall-banner" [class.up]="overallUp()" [class.down]="!overallUp()">
          <div class="banner-dot"></div>
          <div class="banner-text">
            <span class="banner-title">{{ overallUp() ? 'All systems operational' : 'Service degraded' }}</span>
            <span class="banner-sub">{{ components().length }} components checked</span>
          </div>
          <button class="refresh-btn" (click)="refresh()">↻ Refresh</button>
        </div>

        <div class="health-grid">
          @for (c of components(); track c.name) {
            <div class="health-card" [class.up]="c.status === 'UP'" [class.down]="c.status !== 'UP'">
              <div class="hc-header">
                <span class="hc-name">{{ c.name }}</span>
                <span class="hc-status" [class]="c.status.toLowerCase()">
                  <span class="hc-dot"></span>
                  {{ c.status }}
                </span>
              </div>
              @if (c.details) {
                <p class="hc-details">{{ c.details }}</p>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .overall-banner {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem 1.25rem;
      border-radius: 12px;
      margin-bottom: 1.5rem;
      border: 1px solid;

      &.up   { background: rgba(16, 185, 129, 0.06);  border-color: rgba(16, 185, 129, 0.2); }
      &.down { background: rgba(244, 63, 94, 0.06);   border-color: rgba(244, 63, 94, 0.2); }
    }

    .banner-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      flex-shrink: 0;

      .up &   { background: #10b981; box-shadow: 0 0 10px #10b981; animation: pulse 2s ease-in-out infinite; }
      .down & { background: #f43f5e; box-shadow: 0 0 10px #f43f5e; }
    }

    .banner-text { flex: 1; display: flex; flex-direction: column; gap: 2px; }
    .banner-title { font-weight: 600; font-size: 0.9rem; }
    .banner-sub   { font-size: 0.7rem; color: var(--color-text-muted); }

    .refresh-btn {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 8px;
      color: var(--color-text-secondary);
      font-size: 0.775rem;
      padding: 0.375rem 0.875rem;
      cursor: pointer;
      font-family: var(--font-sans);
      transition: background var(--transition-fast), border-color var(--transition-fast), color var(--transition-fast);
      white-space: nowrap;

      &:hover {
        background: rgba(99, 102, 241, 0.08);
        border-color: rgba(99, 102, 241, 0.3);
        color: var(--color-text-primary);
      }
    }

    .health-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 1rem;
    }

    .health-card {
      background: rgba(9, 14, 28, 0.7);
      border: 1px solid rgba(99, 102, 241, 0.08);
      border-radius: 12px;
      padding: 1.25rem;
      backdrop-filter: blur(8px);
      transition: border-color var(--transition-fast);

      &.up   { border-left: 3px solid #10b981; }
      &.down { border-left: 3px solid #f43f5e; }

      &:hover { border-color: rgba(99, 102, 241, 0.25); }
    }

    .hc-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .hc-name { font-weight: 600; font-size: 0.875rem; text-transform: capitalize; }

    .hc-status {
      display: flex;
      align-items: center;
      gap: 0.35rem;
      font-size: 0.65rem;
      font-weight: 700;
      font-family: var(--font-mono);
      letter-spacing: 0.08em;

      &.up      { color: #10b981; }
      &.down    { color: #f43f5e; }
      &.unknown { color: var(--color-text-muted); }
    }

    .hc-dot {
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: currentColor;

      .up & { animation: pulse 2s ease-in-out infinite; }
    }

    .hc-details {
      margin-top: 0.625rem;
      font-size: 0.7rem;
      color: var(--color-text-muted);
      font-family: var(--font-mono);
      word-break: break-all;
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; box-shadow: 0 0 6px currentColor; }
      50%       { opacity: 0.6; box-shadow: none; }
    }
  `,
})
export class HealthComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly overallUp = signal(false);
  readonly components = signal<HealthDetail[]>([]);
  readonly checkedAt = signal(new Date());

  ngOnInit(): void { this.refresh(); }

  refresh(): void {
    this.loading.set(true);
    this.checkedAt.set(new Date());
    this.api.getHealth().subscribe({
      next: (raw) => {
        const health = raw as Record<string, unknown>;
        this.overallUp.set((health['status'] as string) === 'UP');
        const comps = (health['components'] as Record<string, { status: string; details?: Record<string, unknown> }>) ?? {};
        this.components.set(
          Object.entries(comps).map(([name, v]) => ({
            name,
            status: (v.status ?? 'UNKNOWN') as 'UP' | 'DOWN' | 'UNKNOWN',
            details: v.details ? JSON.stringify(v.details).slice(0, 100) : undefined,
          }))
        );
        this.loading.set(false);
      },
      error: () => {
        this.overallUp.set(false);
        this.components.set([{ name: 'API', status: 'DOWN', details: 'Unable to reach /actuator/health' }]);
        this.loading.set(false);
      },
    });
  }
}

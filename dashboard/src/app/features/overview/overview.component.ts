import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { MetricsSummary } from '../../core/models/metrics.model';

interface KpiCard {
  label: string;
  value: number;
  icon: string;
  route: string;
  gradient: string;
  glow: string;
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Overview</h1>
        <p>Real-time payment fraud analysis — last 1 000 events</p>
      </div>

      @if (loading()) {
        <div class="loading-state"><span>Fetching metrics…</span></div>
      } @else if (metrics()) {
        <div class="kpi-grid">
          @for (kpi of kpiCards(); track kpi.label) {
            <a [routerLink]="kpi.route" class="kpi-card" [style.--glow]="kpi.glow">
              <div class="kpi-card-bg" [style.background]="kpi.gradient"></div>
              <div class="kpi-icon">{{ kpi.icon }}</div>
              <div class="kpi-value">{{ kpi.value.toLocaleString() }}</div>
              <div class="kpi-label">{{ kpi.label }}</div>
              <span class="kpi-cta">View all →</span>
            </a>
          }
        </div>

        <div class="bottom-grid">
          <!-- Severity breakdown -->
          <div class="card severity-card">
            <div class="card-header">
              <h2>Anomaly Breakdown</h2>
              <span class="total-badge">{{ metrics()!.anomalyCount }} total</span>
            </div>

            <div class="severity-list">
              @for (entry of severityEntries(); track entry.key) {
                <div class="severity-item">
                  <div class="severity-info">
                    <span class="severity-dot" [style.background]="severityColor(entry.key)"></span>
                    <span class="severity-name" [style.color]="severityColor(entry.key)">{{ entry.key }}</span>
                    <span class="severity-pct">{{ entry.pct | number: '1.0-0' }}%</span>
                  </div>
                  <div class="bar-track">
                    <div
                      class="bar-fill"
                      [style.width.%]="entry.pct"
                      [style.background]="severityGradient(entry.key)"
                    ></div>
                  </div>
                  <span class="severity-count">{{ entry.value }}</span>
                </div>
              }
              @if (severityEntries().length === 0) {
                <p class="no-data">No anomalies recorded</p>
              }
            </div>
          </div>

          <!-- Quick nav -->
          <div class="card quick-nav-card">
            <div class="card-header">
              <h2>Quick Actions</h2>
            </div>

            <div class="quick-nav-list">
              @for (item of quickLinks; track item.route) {
                <a [routerLink]="item.route" class="quick-nav-item">
                  <div class="qn-left">
                    <span class="qn-icon">{{ item.icon }}</span>
                    <div class="qn-text">
                      <span class="qn-label">{{ item.label }}</span>
                      <span class="qn-desc">{{ item.desc }}</span>
                    </div>
                  </div>
                  <span class="qn-arrow">→</span>
                </a>
              }
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    /* KPI grid */
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1rem;
      margin-bottom: 1.25rem;
    }

    .kpi-card {
      position: relative;
      overflow: hidden;
      border-radius: 14px;
      border: 1px solid rgba(99, 102, 241, 0.12);
      padding: 1.5rem;
      text-decoration: none;
      display: flex;
      flex-direction: column;
      gap: 0.375rem;
      background: rgba(9, 14, 28, 0.8);
      transition: transform var(--transition-fast), border-color var(--transition-fast), box-shadow var(--transition-fast);
      backdrop-filter: blur(12px);

      &:hover {
        transform: translateY(-3px);
        border-color: rgba(99, 102, 241, 0.35);
        box-shadow: 0 12px 40px -8px var(--glow, rgba(99, 102, 241, 0.3));

        .kpi-cta { opacity: 1; transform: translateX(2px); }
        .kpi-card-bg { opacity: 0.06; }
      }
    }

    .kpi-card-bg {
      position: absolute;
      inset: 0;
      opacity: 0.04;
      transition: opacity var(--transition-base);
      pointer-events: none;
    }

    .kpi-icon {
      font-size: 1.125rem;
      color: var(--color-text-secondary);
      margin-bottom: 0.25rem;
    }

    .kpi-value {
      font-size: 2.75rem;
      font-weight: 800;
      font-family: var(--font-mono);
      line-height: 1;
      background: linear-gradient(135deg, #c7d2fe 0%, #a5f3fc 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: -0.03em;
    }

    .kpi-label {
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--color-text-secondary);
      letter-spacing: 0.04em;
      text-transform: uppercase;
    }

    .kpi-cta {
      font-size: 0.7rem;
      color: var(--color-accent);
      margin-top: 0.75rem;
      opacity: 0;
      display: inline-block;
      transition: opacity var(--transition-fast), transform var(--transition-fast);
    }

    /* Bottom grid */
    .bottom-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1.25rem;

      h2 {
        font-size: 0.8125rem;
        font-weight: 600;
        color: var(--color-text-secondary);
        letter-spacing: 0.04em;
      }
    }

    .total-badge {
      font-size: 0.65rem;
      font-family: var(--font-mono);
      background: rgba(99, 102, 241, 0.1);
      color: var(--color-accent);
      border: 1px solid rgba(99, 102, 241, 0.2);
      padding: 2px 8px;
      border-radius: 20px;
    }

    /* Severity */
    .severity-list { display: flex; flex-direction: column; gap: 0.875rem; }

    .severity-item {
      display: grid;
      grid-template-columns: 140px 1fr 36px;
      align-items: center;
      gap: 0.875rem;
    }

    .severity-info {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .severity-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      flex-shrink: 0;
      box-shadow: 0 0 6px currentColor;
    }

    .severity-name {
      font-size: 0.7rem;
      font-weight: 700;
      font-family: var(--font-mono);
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .severity-pct {
      font-size: 0.65rem;
      color: var(--color-text-muted);
      font-family: var(--font-mono);
      margin-left: auto;
    }

    .bar-track {
      height: 5px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 3px;
      overflow: hidden;
    }

    .bar-fill {
      height: 100%;
      border-radius: 3px;
      transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .severity-count {
      font-size: 0.7rem;
      color: var(--color-text-secondary);
      font-family: var(--font-mono);
      text-align: right;
    }

    .no-data { font-size: 0.8rem; color: var(--color-text-muted); text-align: center; padding: 1rem; }

    /* Quick nav */
    .quick-nav-list { display: flex; flex-direction: column; gap: 4px; }

    .quick-nav-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.75rem 0.875rem;
      border-radius: 10px;
      text-decoration: none;
      transition: background var(--transition-fast);
      border: 1px solid transparent;

      &:hover {
        background: rgba(99, 102, 241, 0.06);
        border-color: rgba(99, 102, 241, 0.12);

        .qn-arrow { color: var(--color-accent); transform: translateX(3px); }
      }
    }

    .qn-left { display: flex; align-items: center; gap: 0.75rem; }

    .qn-icon {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      background: rgba(99, 102, 241, 0.08);
      border: 1px solid rgba(99, 102, 241, 0.12);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.875rem;
      flex-shrink: 0;
    }

    .qn-text { display: flex; flex-direction: column; gap: 1px; }
    .qn-label { font-size: 0.8rem; font-weight: 600; color: var(--color-text-primary); }
    .qn-desc  { font-size: 0.7rem; color: var(--color-text-muted); }

    .qn-arrow {
      font-size: 0.875rem;
      color: var(--color-text-muted);
      transition: color var(--transition-fast), transform var(--transition-fast);
    }

    @media (max-width: 900px) {
      .kpi-grid    { grid-template-columns: 1fr 1fr; }
      .bottom-grid { grid-template-columns: 1fr; }
    }
  `,
})
export class OverviewComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly metrics = signal<MetricsSummary | null>(null);
  readonly kpiCards = signal<KpiCard[]>([]);
  readonly severityEntries = signal<{ key: string; value: number; pct: number }[]>([]);

  readonly quickLinks = [
    { label: 'Transactions', desc: 'Browse recent payment events', route: '/transactions', icon: '⇄' },
    { label: 'Anomalies',    desc: 'Review detected fraud signals', route: '/anomalies',    icon: '⚑' },
    { label: 'Reports',      desc: 'Read Claude AI analysis',      route: '/reports',       icon: '≡' },
    { label: 'System Health', desc: 'Check service status',        route: '/health',        icon: '◎' },
  ];

  ngOnInit(): void {
    this.api.getMetrics().subscribe({
      next: (m) => {
        this.metrics.set(m);
        this.kpiCards.set([
          {
            label: 'Transactions', value: m.transactionCount,
            icon: '⇄', route: '/transactions',
            gradient: 'linear-gradient(135deg, #6366f1, #06b6d4)',
            glow: 'rgba(99, 102, 241, 0.35)',
          },
          {
            label: 'Anomalies', value: m.anomalyCount,
            icon: '⚑', route: '/anomalies',
            gradient: 'linear-gradient(135deg, #f43f5e, #f59e0b)',
            glow: 'rgba(244, 63, 94, 0.3)',
          },
          {
            label: 'Reports', value: m.reportCount,
            icon: '≡', route: '/reports',
            gradient: 'linear-gradient(135deg, #10b981, #06b6d4)',
            glow: 'rgba(16, 185, 129, 0.3)',
          },
        ]);

        const total = Object.values(m.anomaliesBySeverity).reduce((a, b) => a + b, 0) || 1;
        const order = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
        this.severityEntries.set(
          order
            .filter((k) => m.anomaliesBySeverity[k] !== undefined)
            .map((k) => ({
              key: k,
              value: m.anomaliesBySeverity[k],
              pct: (m.anomaliesBySeverity[k] / total) * 100,
            }))
        );
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  severityColor(key: string): string {
    const m: Record<string, string> = {
      LOW: '#10b981', MEDIUM: '#f59e0b', HIGH: '#f43f5e', CRITICAL: '#a855f7',
    };
    return m[key] ?? '#3d4d6b';
  }

  severityGradient(key: string): string {
    const m: Record<string, string> = {
      LOW:      'linear-gradient(90deg, #10b981, #06b6d4)',
      MEDIUM:   'linear-gradient(90deg, #f59e0b, #f43f5e)',
      HIGH:     'linear-gradient(90deg, #f43f5e, #a855f7)',
      CRITICAL: 'linear-gradient(90deg, #a855f7, #6366f1)',
    };
    return m[key] ?? 'var(--color-accent)';
  }
}

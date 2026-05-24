import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { DecimalPipe, DatePipe, SlicePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import {
  CardModule,
  GridModule,
  BadgeModule,
  ProgressModule,
  ListGroupModule,
  WidgetModule,
  SpinnerModule,
  SharedModule,
  ButtonModule,
} from '@coreui/angular';
import { IconModule } from '@coreui/icons-angular';
import { NgChartsModule } from 'ng2-charts';
import type { ChartConfiguration, ChartData } from 'chart.js';
import { ApiService } from '../../core/services/api.service';
import { MetricsSummary } from '../../core/models/metrics.model';
import { Anomaly } from '../../core/models/anomaly.model';
import { Report } from '../../core/models/report.model';

type ThemeColor = 'primary' | 'info' | 'warning' | 'danger' | 'success' | 'secondary';

interface KpiCard {
  label: string;
  subtitle: string;
  value: string;
  icon: string;
  color: ThemeColor;
  route: string;
}

interface SeverityEntry {
  key: string;
  value: number;
  pct: number;
  color: ThemeColor;
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    RouterLink,
    DecimalPipe,
    DatePipe,
    SlicePipe,
    CardModule,
    GridModule,
    BadgeModule,
    ProgressModule,
    ListGroupModule,
    WidgetModule,
    SpinnerModule,
    SharedModule,
    ButtonModule,
    IconModule,
    NgChartsModule,
  ],
  template: `
    <!-- Header with refresh action -->
    <div class="d-flex flex-wrap justify-content-between align-items-start mb-4 gap-3">
      <div>
        <h1 class="mb-1">Overview</h1>
        <p class="text-body-secondary mb-0">
          Real-time payment fraud analysis — last 100 events
        </p>
      </div>
      <div class="d-flex align-items-center gap-2">
        <small class="text-body-secondary text-nowrap">
          Last updated {{ checkedAt() | date:'HH:mm:ss' }}
        </small>
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
    </div>

    @if (loading()) {
      <div class="d-flex justify-content-center py-5">
        <c-spinner color="primary" />
      </div>
    } @else if (metrics()) {
      <!-- KPI row: 1 widget-stat-a with sparkline + 4 widget-stat-f -->
      <c-row [xs]="1" [sm]="2" [xl]="5" class="g-4 mb-4">
        <!-- Transactions KPI with sparkline -->
        <c-col>
          <a routerLink="/transactions" class="text-decoration-none">
            <c-widget-stat-a color="primary" [value]="kpiCards()[0].value" [title]="kpiCards()[0].label">
              <ng-template cTemplateId="widgetChartTemplate">
                <div class="chart-wrapper position-relative" style="height:50px;margin-top:8px;">
                  <canvas
                    baseChart
                    type="line"
                    [data]="sparklineData()"
                    [options]="sparklineOptions"
                  ></canvas>
                </div>
              </ng-template>
            </c-widget-stat-a>
          </a>
        </c-col>

        <!-- 4 remaining KPIs as widget-stat-f -->
        @for (kpi of kpiCards().slice(1); track kpi.label) {
          <c-col>
            <a [routerLink]="kpi.route" class="text-decoration-none">
              <c-widget-stat-f
                [color]="kpi.color"
                [title]="kpi.label"
                [value]="kpi.value"
                [footer]="kpi.subtitle"
              >
                <ng-template cTemplateId="widgetIconTemplate">
                  <svg cIcon [name]="kpi.icon" size="xl"></svg>
                </ng-template>
              </c-widget-stat-f>
            </a>
          </c-col>
        }
      </c-row>

      <!-- Recent anomalies + Latest AI insights -->
      <c-row [xs]="1" [md]="2" class="g-4 mb-4">
        <c-col>
          <c-card class="h-100">
            <c-card-header class="d-flex justify-content-between align-items-center">
              <span class="fw-semibold">Recent high-risk anomalies</span>
              <a routerLink="/anomalies" class="small">View all</a>
            </c-card-header>
            <c-card-body class="p-0">
              @if (criticalAnomalies().length === 0) {
                <div class="text-center text-body-secondary py-4 small">
                  No high-risk anomalies detected
                </div>
              } @else {
                <ul cListGroup flush class="list-group-flush">
                  @for (a of criticalAnomalies(); track a.id) {
                    <li cListGroupItem class="d-flex justify-content-between align-items-center gap-3">
                      <div class="d-flex flex-column gap-1 min-width-0 flex-grow-1">
                        <div class="d-flex align-items-center gap-2">
                          <c-badge [color]="severityColor(a.severity)">{{ a.severity }}</c-badge>
                          <span class="small fw-semibold font-monospace text-body">{{ a.type }}</span>
                        </div>
                        <p class="small text-body-secondary mb-0 text-truncate" [title]="a.description">
                          {{ a.description }}
                        </p>
                      </div>
                      <small class="text-body-secondary font-monospace flex-shrink-0">
                        {{ a.detectedAt | date:'HH:mm' }}
                      </small>
                    </li>
                  }
                </ul>
              }
            </c-card-body>
          </c-card>
        </c-col>

        <c-col>
          <c-card class="h-100">
            <c-card-header class="d-flex justify-content-between align-items-center">
              <span class="fw-semibold">Latest AI insights</span>
              <a routerLink="/reports" class="small">View all</a>
            </c-card-header>
            <c-card-body class="p-0">
              @if (latestReports().length === 0) {
                <div class="text-center text-body-secondary py-4 small">
                  No reports yet
                </div>
              } @else {
                <div cListGroup flush class="list-group-flush">
                  @for (r of latestReports(); track r.id) {
                    <a
                      [routerLink]="['/reports', r.id]"
                      cListGroupItem
                      class="text-decoration-none"
                    >
                      <div class="d-flex justify-content-between align-items-center mb-1">
                        <div class="d-flex align-items-center gap-2">
                          <c-badge color="info">AI</c-badge>
                          <small class="font-monospace text-body-secondary">
                            {{ r.id | slice:0:14 }}…
                          </small>
                        </div>
                        <small class="text-body-secondary">
                          {{ r.generatedAt | date:'dd MMM, HH:mm' }}
                        </small>
                      </div>
                      <div class="small text-body text-truncate">{{ r.summary }}</div>
                    </a>
                  }
                </div>
              }
            </c-card-body>
          </c-card>
        </c-col>
      </c-row>

      <!-- Anomaly severity breakdown -->
      <c-card class="mb-4">
        <c-card-header class="d-flex justify-content-between align-items-center">
          <span class="fw-semibold">Anomaly breakdown</span>
          <div class="d-flex gap-2">
            <c-badge color="primary">{{ metrics()!.anomalyCount | number }} total</c-badge>
            @if (metrics()!.transactionCount > 0) {
              <c-badge color="success">
                {{ anomalyRate() | number:'1.1-1' }}% detection rate
              </c-badge>
            }
          </div>
        </c-card-header>
        <c-card-body>
          @if (severityEntries().length === 0) {
            <p class="text-center text-body-secondary small mb-0">
              No anomalies recorded yet
            </p>
          } @else {
            @for (entry of severityEntries(); track entry.key) {
              <div class="d-flex align-items-center mb-3">
                <div style="width: 110px;" class="d-flex align-items-center gap-2">
                  <c-badge [color]="entry.color">{{ entry.key }}</c-badge>
                </div>
                <div class="small text-body-secondary font-monospace me-3" style="width: 44px;">
                  {{ entry.pct | number:'1.0-0' }}%
                </div>
                <div class="flex-grow-1">
                  <c-progress [height]="8">
                    <c-progress-bar [color]="entry.color" [value]="entry.pct" />
                  </c-progress>
                </div>
                <div class="ms-3 small text-body-secondary font-monospace text-end" style="width: 40px;">
                  {{ entry.value | number }}
                </div>
              </div>
            }
          }
        </c-card-body>
      </c-card>
    }
  `,
  styles: `
    .min-width-0 { min-width: 0; }
  `,
})
export class OverviewComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly metrics = signal<MetricsSummary | null>(null);
  readonly criticalAnomalies = signal<Anomaly[]>([]);
  readonly latestReports = signal<Report[]>([]);
  readonly kpiCards = signal<KpiCard[]>([]);
  readonly severityEntries = signal<SeverityEntry[]>([]);
  readonly checkedAt = signal(new Date());
  readonly sparklineData = signal<ChartData<'line'>>({ labels: [], datasets: [] });

  readonly sparklineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: { enabled: false },
    },
    scales: {
      x: { display: false },
      y: { display: false },
    },
    elements: {
      point: { radius: 0 },
      line: { tension: 0.4, borderWidth: 2 },
    },
  };

  readonly anomalyRate = computed(() => {
    const m = this.metrics();
    if (!m || m.transactionCount === 0) return 0;
    return (m.anomalyCount / m.transactionCount) * 100;
  });

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.checkedAt.set(new Date());

    forkJoin({
      metrics: this.api.getMetrics(),
      anomalies: this.api.getAnomalies(0, 20),
      reports: this.api.getReports(3),
      transactions: this.api.getTransactions(0, 50),
    }).subscribe({
      next: ({ metrics, anomalies, reports, transactions }) => {
        this.metrics.set(metrics);

        const highRiskCount =
          (metrics.anomaliesBySeverity['HIGH'] ?? 0) +
          (metrics.anomaliesBySeverity['CRITICAL'] ?? 0);
        const avgRisk =
          metrics.transactionCount > 0
            ? Math.round((metrics.anomalyCount / metrics.transactionCount) * 100)
            : 0;

        this.kpiCards.set([
          {
            label: 'Transactions',
            subtitle: 'Total ingested events',
            value: metrics.transactionCount.toLocaleString(),
            icon: 'cilTransfer',
            color: 'primary',
            route: '/transactions',
          },
          {
            label: 'Fraud Alerts',
            subtitle: 'Anomalies detected',
            value: metrics.anomalyCount.toLocaleString(),
            icon: 'cilWarning',
            color: 'warning',
            route: '/anomalies',
          },
          {
            label: 'High Risk',
            subtitle: 'HIGH + CRITICAL signals',
            value: highRiskCount.toLocaleString(),
            icon: 'cilShieldAlt',
            color: 'danger',
            route: '/anomalies',
          },
          {
            label: 'AI Reports',
            subtitle: 'Claude analyses generated',
            value: metrics.reportCount.toLocaleString(),
            icon: 'cilNotes',
            color: 'info',
            route: '/reports',
          },
          {
            label: 'Risk Score',
            subtitle: 'Anomaly detection rate',
            value: `${avgRisk}%`,
            icon: 'cilChartLine',
            color: 'success',
            route: '/anomalies',
          },
        ]);

        this.criticalAnomalies.set(
          anomalies.content
            .filter((a) => a.severity === 'HIGH' || a.severity === 'CRITICAL')
            .slice(0, 5)
        );
        this.latestReports.set(reports.slice(0, 3));

        const total =
          Object.values(metrics.anomaliesBySeverity).reduce((a, b) => a + b, 0) || 1;
        const order: Array<keyof typeof SEVERITY_COLOR> = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
        this.severityEntries.set(
          order
            .filter((k) => metrics.anomaliesBySeverity[k] !== undefined)
            .map((k) => ({
              key: k,
              value: metrics.anomaliesBySeverity[k],
              pct: (metrics.anomaliesBySeverity[k] / total) * 100,
              color: SEVERITY_COLOR[k],
            }))
        );

        this.sparklineData.set(buildSparkline(transactions.content));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  severityColor(s: string): ThemeColor {
    return SEVERITY_COLOR[s as keyof typeof SEVERITY_COLOR] ?? 'secondary';
  }
}

const SEVERITY_COLOR = {
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'danger',
  CRITICAL: 'primary',
} as const satisfies Record<string, ThemeColor>;

function buildSparkline(transactions: { occurredAt: string }[]): ChartData<'line'> {
  const BUCKETS = 12;
  if (transactions.length === 0) {
    return {
      labels: Array(BUCKETS).fill(''),
      datasets: [
        {
          data: Array(BUCKETS).fill(0),
          borderColor: 'rgba(255,255,255,0.55)',
          backgroundColor: 'transparent',
        },
      ],
    };
  }

  const times = transactions
    .map((t) => new Date(t.occurredAt).getTime())
    .filter((t) => !isNaN(t));
  const min = Math.min(...times);
  const max = Math.max(...times);
  const span = Math.max(max - min, 1);
  const buckets = Array<number>(BUCKETS).fill(0);
  for (const t of times) {
    const idx = Math.min(BUCKETS - 1, Math.floor(((t - min) / span) * BUCKETS));
    buckets[idx] += 1;
  }

  return {
    labels: buckets.map((_, i) => `${i}`),
    datasets: [
      {
        data: buckets,
        borderColor: 'rgba(255,255,255,0.85)',
        backgroundColor: 'rgba(255,255,255,0.18)',
        fill: true,
      },
    ],
  };
}

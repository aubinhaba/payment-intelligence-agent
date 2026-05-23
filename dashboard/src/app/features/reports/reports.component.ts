import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  CardModule,
  GridModule,
  BadgeModule,
  FormModule,
  SpinnerModule,
} from '@coreui/angular';
import { IconModule } from '@coreui/icons-angular';
import { ApiService } from '../../core/services/api.service';
import { Report } from '../../core/models/report.model';

type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

interface ReportMeta {
  title: string;
  riskLevel: RiskLevel | null;
  riskScore: number | null;
  hasSummary: boolean;
}

interface EnrichedReport extends Report {
  meta: ReportMeta;
}

const TX_TYPE_LABELS: Record<string, string> = {
  geo: 'GEO Anomaly',
  amount: 'Amount Anomaly',
  velocity: 'Velocity Anomaly',
  mcc: 'MCC Anomaly',
  card: 'Card Testing',
};

const TX_SEVERITY_MAP: Record<string, RiskLevel> = {
  crit: 'CRITICAL',
  critical: 'CRITICAL',
  high: 'HIGH',
  med: 'MEDIUM',
  medium: 'MEDIUM',
  low: 'LOW',
};

function parseTxId(txId: string): { title: string | null; riskLevel: RiskLevel | null } {
  const parts = txId.toLowerCase().split('-');
  let title: string | null = null;
  let riskLevel: RiskLevel | null = null;
  for (const part of parts) {
    if (!title && TX_TYPE_LABELS[part]) title = TX_TYPE_LABELS[part];
    if (!riskLevel && TX_SEVERITY_MAP[part]) riskLevel = TX_SEVERITY_MAP[part];
  }
  return { title, riskLevel };
}

function extractReportMeta(summary: string, transactionId: string): ReportMeta {
  const lvlMatch = summary.match(/\b(CRITICAL|HIGH|MEDIUM|LOW)\b/i);
  const riskLevel = lvlMatch ? (lvlMatch[1].toUpperCase() as RiskLevel) : null;

  const scoreMatch = summary.match(/\b(\d{1,3})\s*\/\s*100\b/);
  const riskScore = scoreMatch ? parseInt(scoreMatch[1], 10) : null;

  let title = 'Transaction Analysis';
  if (/\bGEO\b/i.test(summary)) title = 'GEO Anomaly';
  else if (/\bCARD[\s_]TESTING\b/i.test(summary)) title = 'Card Testing';
  else if (/\bVELOCITY\b/i.test(summary)) title = 'Velocity Anomaly';
  else if (/\bMCC\b/i.test(summary)) title = 'MCC Anomaly';
  else if (/\bAMOUNT\b/i.test(summary)) title = 'Amount Anomaly';

  const trimmed = summary.trim();
  const hasSummary = trimmed.length > 20 && trimmed !== 'Analysis complete';

  // Fallback to transactionId when summary has no useful metadata
  if (!riskLevel || title === 'Transaction Analysis') {
    const txMeta = parseTxId(transactionId);
    return {
      title: title !== 'Transaction Analysis' ? title : (txMeta.title ?? title),
      riskLevel: riskLevel ?? txMeta.riskLevel,
      riskScore,
      hasSummary,
    };
  }

  return { title, riskLevel, riskScore, hasSummary };
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    DatePipe,
    SlicePipe,
    RouterLink,
    FormsModule,
    CardModule,
    GridModule,
    BadgeModule,
    FormModule,
    SpinnerModule,
    IconModule,
  ],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-start mb-4 gap-3">
      <div>
        <h1 class="mb-1">Reports</h1>
        <p class="text-body-secondary mb-0">AI-generated analysis reports from Claude</p>
      </div>
      @if (!loading() && reports().length > 0) {
        <div class="d-flex align-items-center gap-2">
          <c-input-group sizing="sm" style="width: 240px;">
            <span cInputGroupText>
              <svg cIcon name="cilSearch" size="sm"></svg>
            </span>
            <input
              cFormControl
              type="text"
              placeholder="Search reports…"
              [(ngModel)]="searchInput"
              (input)="searchQuery.set(searchInput)"
            />
          </c-input-group>
          <small class="text-body-secondary font-monospace text-nowrap">
            {{ filtered().length }} / {{ reports().length }}
          </small>
        </div>
      }
    </div>

    @if (loading()) {
      <div class="d-flex justify-content-center py-5">
        <c-spinner color="primary" />
      </div>
    } @else if (reports().length === 0) {
      <c-card>
        <c-card-body class="text-center text-body-secondary py-5">
          <p class="mb-1">No reports generated yet</p>
          <small>Reports are created when anomalies are analyzed by the AI agent</small>
        </c-card-body>
      </c-card>
    } @else if (filtered().length === 0) {
      <c-card>
        <c-card-body class="text-center text-body-secondary py-5">
          <p class="mb-1">No reports match your search</p>
          <small>Try a different search term</small>
        </c-card-body>
      </c-card>
    } @else {
      <c-row [xs]="1" [md]="2" [xl]="3" class="g-4">
        @for (r of filteredEnriched(); track r.id) {
          <c-col>
            <a [routerLink]="['/reports', r.id]" class="text-decoration-none">
              <c-card class="h-100">
                <c-card-body class="d-flex flex-column gap-2">

                  <!-- Title + severity badge -->
                  <div class="d-flex justify-content-between align-items-start gap-2">
                    <span class="fw-semibold text-body lh-sm">{{ r.meta.title }}</span>
                    @if (r.meta.riskLevel) {
                      <c-badge [color]="severityColor(r.meta.riskLevel)" class="flex-shrink-0">
                        {{ r.meta.riskLevel }}
                      </c-badge>
                    } @else {
                      <c-badge color="secondary" class="flex-shrink-0">AI</c-badge>
                    }
                  </div>

                  <!-- Risk bar: shown whenever severity is known; label only when score is real -->
                  @if (r.meta.riskLevel) {
                    <div class="d-flex align-items-center gap-2">
                      <small class="text-body-secondary text-nowrap">Risk</small>
                      <div class="progress flex-grow-1" style="height: 4px;">
                        <div
                          role="progressbar"
                          [class]="progressBarClass(r.meta.riskLevel)"
                          [style.width.%]="r.meta.riskScore ?? defaultBarWidth(r.meta.riskLevel)"
                        ></div>
                      </div>
                      @if (r.meta.riskScore !== null) {
                        <small class="text-body-secondary font-monospace text-nowrap">{{ r.meta.riskScore }}/100</small>
                      }
                    </div>
                  }

                  <!-- Summary preview -->
                  @if (r.meta.hasSummary) {
                    <p class="text-body-secondary small mb-0"
                       style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">
                      {{ r.summary }}
                    </p>
                  } @else {
                    <p class="text-body-secondary small fst-italic mb-0">
                      Analysis complete — click to view full report
                    </p>
                  }

                </c-card-body>
                <c-card-footer class="d-flex justify-content-between align-items-center small text-body-secondary">
                  <span class="font-monospace text-truncate" [title]="r.transactionId">
                    {{ r.transactionId | slice:0:14 }}…
                  </span>
                  <span class="text-nowrap">{{ r.generatedAt | date:'dd MMM, HH:mm' }}</span>
                </c-card-footer>
              </c-card>
            </a>
          </c-col>
        }
      </c-row>
    }
  `,
})
export class ReportsComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly reports = signal<Report[]>([]);
  readonly searchQuery = signal('');
  searchInput = '';

  readonly filtered = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    if (!q) return this.reports();
    return this.reports().filter(
      (r) =>
        r.id.toLowerCase().includes(q) ||
        r.summary.toLowerCase().includes(q) ||
        r.transactionId.toLowerCase().includes(q)
    );
  });

  readonly filteredEnriched = computed<EnrichedReport[]>(() =>
    this.filtered().map((r) => ({ ...r, meta: extractReportMeta(r.summary, r.transactionId) }))
  );

  severityColor(level: RiskLevel | null): string {
    switch (level) {
      case 'CRITICAL':
      case 'HIGH': return 'danger';
      case 'MEDIUM': return 'warning';
      case 'LOW': return 'success';
      default: return 'secondary';
    }
  }

  progressBarClass(level: RiskLevel | null): string {
    return `progress-bar bg-${this.severityColor(level)}`;
  }

  defaultBarWidth(level: RiskLevel | null): number {
    switch (level) {
      case 'CRITICAL': return 90;
      case 'HIGH': return 75;
      case 'MEDIUM': return 50;
      case 'LOW': return 25;
      default: return 0;
    }
  }

  ngOnInit(): void {
    this.api.getReports(50).subscribe({
      next: (data) => {
        this.reports.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { Report } from '../../core/models/report.model';

@Component({
  selector: 'app-report-detail',
  standalone: true,
  imports: [DatePipe, RouterLink],
  template: `
    <div class="page-container">
      <a routerLink="/reports" class="back-link">← Back to reports</a>

      @if (loading()) {
        <div class="loading-state"><span>Loading report…</span></div>
      } @else if (!report()) {
        <div class="card empty-state">
          <span>⚠</span>
          <p>Report not found</p>
        </div>
      } @else {
        <div class="detail-header">
          <div>
            <h1>Report</h1>
            <span class="report-id mono">{{ report()!.id }}</span>
          </div>
          <span class="report-date">Generated {{ report()!.generatedAt | date: 'dd MMM yyyy, HH:mm' }}</span>
        </div>

        <div class="meta-row">
          <div class="meta-chip">
            <span class="meta-label">Transaction</span>
            <span class="meta-value mono">{{ report()!.transactionId }}</span>
          </div>
          <div class="meta-chip">
            <span class="meta-label">Anomaly</span>
            <span class="meta-value mono">{{ report()!.anomalyId }}</span>
          </div>
        </div>

        <div class="card summary-card">
          <h2 class="section-title">Executive Summary</h2>
          <p class="summary-text">{{ report()!.summary }}</p>
        </div>

        <div class="card analysis-card">
          <h2 class="section-title">Full Analysis</h2>
          <pre class="analysis-pre">{{ report()!.analysis }}</pre>
        </div>
      }
    </div>
  `,
  styles: `
    .back-link {
      display: inline-block;
      color: var(--color-text-secondary);
      text-decoration: none;
      font-size: 0.875rem;
      margin-bottom: 1.5rem;
      transition: color var(--transition-fast);

      &:hover { color: var(--color-text-primary); }
    }

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
    }

    .report-id   { font-size: 0.8rem; color: var(--color-accent); font-family: var(--font-mono); }
    .report-date { font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.5rem; }

    .meta-row {
      display: flex;
      gap: 1rem;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
    }

    .meta-chip {
      background: var(--color-surface-1);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-sm);
      padding: 0.5rem 0.875rem;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .meta-label { font-size: 0.65rem; text-transform: uppercase; letter-spacing: 0.08em; color: var(--color-text-muted); }
    .meta-value { font-size: 0.8rem; color: var(--color-text-primary); }

    .section-title {
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--color-text-muted);
      margin-bottom: 1rem;
    }

    .summary-card { margin-bottom: 1rem; }
    .summary-text { color: var(--color-text-primary); line-height: 1.7; font-size: 0.9375rem; }

    .analysis-card { margin-bottom: 1rem; }
    .analysis-pre {
      white-space: pre-wrap;
      word-break: break-word;
      font-family: var(--font-mono);
      font-size: 0.8rem;
      line-height: 1.7;
      color: var(--color-text-secondary);
    }
  `,
})
export class ReportDetailComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly report = signal<Report | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.api.getReport(id).subscribe({
      next: (r) => {
        this.report.set(r);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

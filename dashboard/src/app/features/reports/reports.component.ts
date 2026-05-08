import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { Report } from '../../core/models/report.model';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [DatePipe, RouterLink],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Reports</h1>
        <p>AI-generated analysis reports from Claude</p>
      </div>

      @if (loading()) {
        <div class="loading-state"><span>Loading reports…</span></div>
      } @else if (reports().length === 0) {
        <div class="card empty-state">
          <span>◎</span>
          <p>No reports generated yet</p>
        </div>
      } @else {
        <div class="reports-grid">
          @for (r of reports(); track r.id) {
            <a [routerLink]="['/reports', r.id]" class="report-card">
              <div class="report-meta">
                <span class="report-id mono">{{ r.id }}</span>
                <span class="report-date">{{ r.generatedAt | date: 'dd MMM yyyy, HH:mm' }}</span>
              </div>
              <p class="report-summary">{{ r.summary }}</p>
              <div class="report-footer">
                <span class="tx-ref mono">tx: {{ r.transactionId }}</span>
                <span class="read-more">Read report →</span>
              </div>
            </a>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .reports-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
      gap: 1rem;
    }

    .report-card {
      background: var(--color-surface-1);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-md);
      padding: 1.25rem;
      text-decoration: none;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      transition: border-color var(--transition-fast), transform var(--transition-fast);

      &:hover {
        border-color: var(--color-accent);
        transform: translateY(-1px);
      }
    }

    .report-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .report-id   { font-size: 0.75rem; color: var(--color-accent); }
    .report-date { font-size: 0.75rem; color: var(--color-text-muted); }

    .report-summary {
      color: var(--color-text-primary);
      font-size: 0.875rem;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .report-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: auto;
      padding-top: 0.75rem;
      border-top: 1px solid var(--color-border);
    }

    .tx-ref    { font-size: 0.7rem; color: var(--color-text-muted); }
    .read-more { font-size: 0.75rem; color: var(--color-accent); font-weight: 500; }
  `,
})
export class ReportsComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly reports = signal<Report[]>([]);

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

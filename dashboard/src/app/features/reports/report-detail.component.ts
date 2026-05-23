import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  CardModule,
  BadgeModule,
  ButtonModule,
  SpinnerModule,
} from '@coreui/angular';
import { IconModule } from '@coreui/icons-angular';
import { MarkdownComponent } from 'ngx-markdown';
import { ApiService } from '../../core/services/api.service';
import { Report } from '../../core/models/report.model';

@Component({
  selector: 'app-report-detail',
  standalone: true,
  imports: [
    DatePipe,
    SlicePipe,
    RouterLink,
    CardModule,
    BadgeModule,
    ButtonModule,
    SpinnerModule,
    IconModule,
    MarkdownComponent,
  ],
  template: `
    <a routerLink="/reports" class="d-inline-flex align-items-center gap-1 text-body-secondary text-decoration-none mb-4 small">
      <svg cIcon name="cilArrowLeft" size="sm"></svg>
      Back to reports
    </a>

    @if (loading()) {
      <div class="d-flex justify-content-center py-5">
        <c-spinner color="primary" />
      </div>
    } @else if (!report()) {
      <c-card>
        <c-card-body class="text-center text-body-secondary py-5">
          <p class="mb-1">Report not found</p>
          <small>This report may have been deleted or the ID is incorrect</small>
        </c-card-body>
      </c-card>
    } @else {
      <div class="d-flex flex-wrap justify-content-between align-items-start mb-3 gap-3">
        <div>
          <h1 class="mb-1">Analysis Report</h1>
          <small class="font-monospace text-primary">{{ report()!.id }}</small>
        </div>
        <div class="d-flex align-items-center gap-2 flex-wrap">
          <small class="text-body-secondary">
            Generated {{ report()!.generatedAt | date:'dd MMM yyyy, HH:mm' }}
          </small>
          <c-badge color="info">Claude AI</c-badge>
        </div>
      </div>

      <div class="d-flex flex-wrap gap-2 mb-4">
        <c-card class="border-0 bg-body-tertiary">
          <c-card-body class="py-2 px-3">
            <div class="text-uppercase text-body-secondary" style="font-size: 0.65rem; letter-spacing: 0.08em;">
              Transaction
            </div>
            <div class="font-monospace small" [title]="report()!.transactionId">
              {{ report()!.transactionId }}
            </div>
          </c-card-body>
        </c-card>
        <c-card class="border-0 bg-body-tertiary">
          <c-card-body class="py-2 px-3">
            <div class="text-uppercase text-body-secondary" style="font-size: 0.65rem; letter-spacing: 0.08em;">
              Report ID
            </div>
            <div class="font-monospace small">{{ report()!.id | slice:0:16 }}…</div>
          </c-card-body>
        </c-card>
      </div>

      <c-card class="mb-3 border-start border-primary border-4">
        <c-card-header>
          <span class="text-uppercase fw-semibold text-body-secondary" style="font-size: 0.72rem; letter-spacing: 0.1em;">
            Executive Summary
          </span>
        </c-card-header>
        <c-card-body>
          <p class="mb-0">{{ report()!.summary }}</p>
        </c-card-body>
      </c-card>

      <c-card class="mb-4">
        <c-card-header class="d-flex justify-content-between align-items-center">
          <span class="text-uppercase fw-semibold text-body-secondary" style="font-size: 0.72rem; letter-spacing: 0.1em;">
            Full Analysis
          </span>
          <button
            cButton
            color="secondary"
            variant="outline"
            size="sm"
            (click)="copyAnalysis()"
          >
            @if (copied()) {
              <svg cIcon name="cilCheckCircle" size="sm" class="me-1"></svg>
              Copied
            } @else {
              <svg cIcon name="cilCopy" size="sm" class="me-1"></svg>
              Copy
            }
          </button>
        </c-card-header>
        <c-card-body class="p-3 markdown-body" style="max-height: 600px; overflow: auto;">
          <markdown [data]="bodyText()" />
        </c-card-body>
      </c-card>
    }
  `,
})
export class ReportDetailComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly report = signal<Report | null>(null);
  readonly copied = signal(false);

  readonly bodyText = computed(() => {
    const body = this.report()?.markdownBody;
    if (!body) return '';
    const stripped = body.replace(/^```[\w]*\n/, '').replace(/\n?```$/, '').trim();
    try {
      const parsed = JSON.parse(stripped);
      if (typeof parsed['analysis'] === 'string') return parsed['analysis'] as string;
    } catch {
      // not JSON — use as-is
    }
    return body;
  });

  copyAnalysis(): void {
    const text = this.bodyText();
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => {
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    });
  }

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

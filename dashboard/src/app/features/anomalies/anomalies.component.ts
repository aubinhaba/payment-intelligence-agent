import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { Anomaly } from '../../core/models/anomaly.model';
import { BadgeComponent } from '../../shared/components/badge/badge.component';

@Component({
  selector: 'app-anomalies',
  standalone: true,
  imports: [DatePipe, RouterLink, BadgeComponent],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Anomalies</h1>
        <p>Detected fraud signals requiring review</p>
      </div>

      @if (loading()) {
        <div class="loading-state"><span>Loading anomalies…</span></div>
      } @else if (anomalies().length === 0) {
        <div class="card empty-state">
          <span>◎</span>
          <p>No anomalies detected</p>
        </div>
      } @else {
        <div class="card table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Transaction</th>
                <th>Type</th>
                <th>Severity</th>
                <th>Description</th>
                <th>Detected at</th>
              </tr>
            </thead>
            <tbody>
              @for (a of anomalies(); track a.id) {
                <tr>
                  <td class="mono">{{ a.id }}</td>
                  <td>
                    <a [routerLink]="['/transactions']" class="tx-link mono">
                      {{ a.transactionId }}
                    </a>
                  </td>
                  <td>
                    <app-badge [label]="a.type" variant="type" />
                  </td>
                  <td>
                    <app-badge
                      [label]="a.severity"
                      [variant]="severityVariant(a.severity)"
                    />
                  </td>
                  <td class="desc-cell">{{ a.description }}</td>
                  <td class="date-cell">{{ a.detectedAt | date: 'dd MMM, HH:mm' }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
  styles: `
    .table-wrap { padding: 0; overflow-x: auto; }
    .desc-cell  { max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .date-cell  { color: var(--color-text-muted); font-size: 0.8rem; white-space: nowrap; }
    .tx-link    { color: var(--color-accent); text-decoration: none; font-size: 0.8rem; &:hover { text-decoration: underline; } }
  `,
})
export class AnomaliesComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly anomalies = signal<Anomaly[]>([]);

  ngOnInit(): void {
    this.api.getAnomalies(50).subscribe({
      next: (data) => {
        this.anomalies.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  severityVariant(
    s: string
  ): 'severity-low' | 'severity-medium' | 'severity-high' | 'severity-critical' {
    const map: Record<string, 'severity-low' | 'severity-medium' | 'severity-high' | 'severity-critical'> = {
      LOW: 'severity-low',
      MEDIUM: 'severity-medium',
      HIGH: 'severity-high',
      CRITICAL: 'severity-critical',
    };
    return map[s] ?? 'severity-low';
  }
}

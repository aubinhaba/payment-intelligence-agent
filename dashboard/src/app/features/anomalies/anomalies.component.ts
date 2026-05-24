import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  CardModule,
  TableModule,
  BadgeModule,
  FormModule,
  ButtonModule,
  SpinnerModule,
} from '@coreui/angular';
import { IconModule } from '@coreui/icons-angular';
import { ApiService } from '../../core/services/api.service';
import { Anomaly } from '../../core/models/anomaly.model';

type AnomalyType = 'ALL' | 'VELOCITY' | 'AMOUNT' | 'GEO' | 'CARD_TESTING' | 'MCC';
type SeverityFilter = 'ALL' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
type ThemeColor = 'primary' | 'info' | 'warning' | 'danger' | 'success' | 'secondary';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-anomalies',
  standalone: true,
  imports: [
    DatePipe,
    SlicePipe,
    RouterLink,
    FormsModule,
    CardModule,
    TableModule,
    BadgeModule,
    FormModule,
    ButtonModule,
    SpinnerModule,
    IconModule,
  ],
  template: `
    <h1 class="mb-1">Anomalies</h1>
    <p class="text-body-secondary mb-4">Detected fraud signals requiring review</p>

    <c-card class="mb-4">
      <c-card-header class="d-flex flex-wrap gap-2 align-items-center justify-content-between">
        <div class="small text-body-secondary">
          <strong class="text-body">{{ filtered().length }}</strong> results
          @if (filtered().length < anomalies().length && !loading()) {
            <c-badge color="primary" class="ms-2">
              filtered from {{ anomalies().length }}
            </c-badge>
          }
        </div>
        <div class="d-flex gap-2 flex-wrap">
          <c-input-group sizing="sm" style="width: 240px;">
            <span cInputGroupText>
              <svg cIcon name="cilSearch" size="sm"></svg>
            </span>
            <input
              cFormControl
              type="text"
              placeholder="Search ID, description…"
              [(ngModel)]="searchInput"
              (input)="searchQuery.set(searchInput)"
            />
          </c-input-group>
          <select
            cSelect
            sizing="sm"
            [value]="typeFilter()"
            (change)="typeFilter.set($any($event.target).value)"
            style="width: 150px;"
          >
            <option value="ALL">All types</option>
            <option value="VELOCITY">Velocity</option>
            <option value="AMOUNT">Amount</option>
            <option value="GEO">Geo</option>
            <option value="CARD_TESTING">Card Testing</option>
            <option value="MCC">MCC</option>
          </select>
          <select
            cSelect
            sizing="sm"
            [value]="severityFilter()"
            (change)="severityFilter.set($any($event.target).value)"
            style="width: 150px;"
          >
            <option value="ALL">All severities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>
      </c-card-header>

      <c-card-body class="p-0">
        @if (loading()) {
          <div class="d-flex justify-content-center py-5">
            <c-spinner color="primary" />
          </div>
        } @else if (anomalies().length === 0) {
          <div class="text-center text-body-secondary py-5">
            <p class="mb-1">No anomalies detected</p>
            <small>The system is monitoring transactions in real-time</small>
          </div>
        } @else if (filtered().length === 0) {
          <div class="text-center text-body-secondary py-5">
            <p class="mb-1">No anomalies match your filters</p>
            <small>Try adjusting the type or severity filter</small>
          </div>
        } @else {
          <div class="table-responsive">
            <table cTable hover small class="mb-0 align-middle">
              <thead>
                <tr>
                  @for (col of columns; track col) {
                    <th scope="col">{{ col }}</th>
                  }
                </tr>
              </thead>
              <tbody>
                @for (a of filtered(); track a.id) {
                  <tr>
                    <td class="font-monospace small" [title]="a.id">{{ a.id | slice:0:12 }}…</td>
                    <td>
                      <a [routerLink]="['/transactions']" class="font-monospace small" [title]="a.transactionId">
                        {{ a.transactionId | slice:0:12 }}…
                      </a>
                    </td>
                    <td>
                      <c-badge color="info">{{ a.type }}</c-badge>
                    </td>
                    <td>
                      <c-badge [color]="severityColor(a.severity)">{{ a.severity }}</c-badge>
                    </td>
                    <td class="small text-body-secondary text-truncate" style="max-width: 260px;" [title]="a.description">
                      {{ a.description }}
                    </td>
                    <td class="small text-body-secondary text-nowrap font-monospace">
                      {{ a.detectedAt | date:'dd MMM, HH:mm' }}
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </c-card-body>

      @if (!loading() && anomalies().length > 0) {
        <c-card-footer class="d-flex align-items-center justify-content-between py-2">
          <small class="text-body-secondary">
            Page <strong>{{ page() + 1 }}</strong>
            &nbsp;·&nbsp; {{ anomalies().length }} items
          </small>
          <div class="d-flex gap-2">
            <button
              cButton
              color="secondary"
              size="sm"
              variant="outline"
              [disabled]="page() === 0"
              (click)="prevPage()"
            >
              ← Prev
            </button>
            <button
              cButton
              color="secondary"
              size="sm"
              variant="outline"
              [disabled]="!hasMore()"
              (click)="nextPage()"
            >
              Next →
            </button>
          </div>
        </c-card-footer>
      }
    </c-card>
  `,
})
export class AnomaliesComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly anomalies = signal<Anomaly[]>([]);
  readonly page = signal(0);
  readonly hasMore = signal(false);
  readonly searchQuery = signal('');
  readonly typeFilter = signal<AnomalyType>('ALL');
  readonly severityFilter = signal<SeverityFilter>('ALL');

  searchInput = '';

  readonly columns = ['ID', 'Transaction', 'Type', 'Severity', 'Description', 'Detected at'];

  readonly filtered = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const t = this.typeFilter();
    const s = this.severityFilter();

    return this.anomalies().filter((a) => {
      const matchesSearch =
        !q ||
        a.id.toLowerCase().includes(q) ||
        a.transactionId.toLowerCase().includes(q) ||
        a.description.toLowerCase().includes(q);
      const matchesType = t === 'ALL' || a.type === t;
      const matchesSeverity = s === 'ALL' || a.severity === s;
      return matchesSearch && matchesType && matchesSeverity;
    });
  });

  ngOnInit(): void {
    this.loadPage(0);
  }

  prevPage(): void {
    if (this.page() > 0) this.loadPage(this.page() - 1);
  }

  nextPage(): void {
    if (this.hasMore()) this.loadPage(this.page() + 1);
  }

  private loadPage(page: number): void {
    this.loading.set(true);
    this.api.getAnomalies(page, PAGE_SIZE).subscribe({
      next: (resp) => {
        this.anomalies.set(resp.content);
        this.hasMore.set(resp.content.length === PAGE_SIZE);
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  severityColor(s: string): ThemeColor {
    const map: Record<string, ThemeColor> = {
      LOW: 'success',
      MEDIUM: 'warning',
      HIGH: 'danger',
      CRITICAL: 'primary',
    };
    return map[s] ?? 'secondary';
  }
}

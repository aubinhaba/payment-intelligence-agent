import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
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
import { Transaction } from '../../core/models/transaction.model';

type StatusFilter = 'ALL' | 'AUTHORIZED' | 'DECLINED' | 'FLAGGED';
type StatusColor = 'success' | 'danger' | 'warning' | 'secondary';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [
    DatePipe,
    SlicePipe,
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
    <h1 class="mb-1">Transactions</h1>
    <p class="text-body-secondary mb-4">Recent ingested payment events</p>

    <c-card class="mb-4">
      <c-card-header class="d-flex flex-wrap gap-2 align-items-center justify-content-between">
        <div class="small text-body-secondary">
          <strong class="text-body">{{ filtered().length }}</strong> results
          @if (filtered().length < transactions().length && !loading()) {
            <c-badge color="primary" class="ms-2">
              filtered from {{ transactions().length }}
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
              placeholder="Search ID, merchant, country…"
              [(ngModel)]="searchInput"
              (input)="searchQuery.set(searchInput)"
            />
          </c-input-group>
          <select
            cSelect
            sizing="sm"
            [value]="statusFilter()"
            (change)="statusFilter.set($any($event.target).value)"
            style="width: 150px;"
          >
            <option value="ALL">All status</option>
            <option value="AUTHORIZED">Authorized</option>
            <option value="DECLINED">Declined</option>
            <option value="FLAGGED">Flagged</option>
          </select>
        </div>
      </c-card-header>

      <c-card-body class="p-0">
        @if (loading()) {
          <div class="d-flex justify-content-center py-5">
            <c-spinner color="primary" />
          </div>
        } @else if (transactions().length === 0) {
          <div class="text-center text-body-secondary py-5">
            <p class="mb-1">No transactions yet</p>
            <small>Start the event simulator to generate payment events</small>
          </div>
        } @else if (filtered().length === 0) {
          <div class="text-center text-body-secondary py-5">
            <p class="mb-1">No results match your filters</p>
            <small>Try adjusting your search query or status filter</small>
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
                @for (tx of filtered(); track tx.id) {
                  <tr>
                    <td class="font-monospace small" [title]="tx.id">{{ tx.id | slice:0:12 }}…</td>
                    <td class="font-monospace">
                      <strong>{{ tx.amount }}</strong>
                      <small class="text-body-secondary ms-1">{{ tx.currency }}</small>
                    </td>
                    <td class="font-monospace small text-body-secondary">····&nbsp;{{ tx.last4 }}</td>
                    <td class="font-monospace small text-body-secondary text-truncate" style="max-width: 160px;" [title]="tx.merchantId">
                      {{ tx.merchantId | slice:0:16 }}
                    </td>
                    <td class="font-monospace small text-body-secondary">{{ tx.mcc }}</td>
                    <td class="small text-body-secondary">{{ tx.country }}</td>
                    <td>
                      <c-badge [color]="statusColor(tx.status)">{{ tx.status }}</c-badge>
                    </td>
                    <td class="small text-body-secondary text-nowrap font-monospace">
                      {{ tx.occurredAt | date:'dd MMM, HH:mm:ss' }}
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </c-card-body>

      @if (!loading() && transactions().length > 0) {
        <c-card-footer class="d-flex align-items-center justify-content-between py-2">
          <small class="text-body-secondary">
            Page <strong>{{ page() + 1 }}</strong>
            &nbsp;·&nbsp; {{ transactions().length }} items
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
export class TransactionsComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly transactions = signal<Transaction[]>([]);
  readonly page = signal(0);
  readonly hasMore = signal(false);
  readonly searchQuery = signal('');
  readonly statusFilter = signal<StatusFilter>('ALL');

  searchInput = '';

  readonly columns = [
    'Transaction ID',
    'Amount',
    'Card',
    'Merchant',
    'MCC',
    'Country',
    'Status',
    'Timestamp',
  ];

  readonly filtered = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const s = this.statusFilter();

    return this.transactions().filter((tx) => {
      const matchesSearch =
        !q ||
        tx.id.toLowerCase().includes(q) ||
        tx.merchantId.toLowerCase().includes(q) ||
        tx.last4.includes(q) ||
        tx.country.toLowerCase().includes(q) ||
        tx.mcc.includes(q);
      const matchesStatus = s === 'ALL' || tx.status === s;
      return matchesSearch && matchesStatus;
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
    this.api.getTransactions(page, PAGE_SIZE).subscribe({
      next: (resp) => {
        this.transactions.set(resp.content);
        this.hasMore.set(resp.content.length === PAGE_SIZE);
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  statusColor(status: string): StatusColor {
    if (status === 'AUTHORIZED') return 'success';
    if (status === 'DECLINED') return 'danger';
    if (status === 'FLAGGED') return 'warning';
    return 'secondary';
  }
}

import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { Transaction } from '../../core/models/transaction.model';
import { BadgeComponent } from '../../shared/components/badge/badge.component';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [DatePipe, DecimalPipe, BadgeComponent],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Transactions</h1>
        <p>{{ transactions().length }} most recent ingested payment events</p>
      </div>

      @if (loading()) {
        <div class="loading-state"><span>Fetching transactions…</span></div>
      } @else if (transactions().length === 0) {
        <div class="card empty-state">
          <span>◎</span>
          <p>No transactions yet — start the event simulator</p>
        </div>
      } @else {
        <div class="card table-card">
          <table class="data-table">
            <thead>
              <tr>
                <th>Transaction ID</th>
                <th>Amount</th>
                <th>Card</th>
                <th>Merchant</th>
                <th>MCC</th>
                <th>Country</th>
                <th>Status</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              @for (tx of transactions(); track tx.id) {
                <tr>
                  <td class="mono id-cell">{{ tx.id }}</td>
                  <td class="amount-cell mono">
                    <span class="amount-value">{{ tx.amount }}</span>
                    <span class="currency-tag">{{ tx.currency }}</span>
                  </td>
                  <td class="mono muted">····&nbsp;{{ tx.last4 }}</td>
                  <td class="mono muted">{{ tx.merchantId }}</td>
                  <td class="mono muted">{{ tx.mcc }}</td>
                  <td class="muted">{{ tx.country }}</td>
                  <td>
                    <app-badge [label]="tx.status" [variant]="statusVariant(tx.status)" />
                  </td>
                  <td class="date-cell">{{ tx.occurredAt | date: 'dd MMM, HH:mm:ss' }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
  styles: `
    .table-card { padding: 0; overflow-x: auto; }

    .id-cell   { color: var(--color-text-primary) !important; }
    .muted     { color: var(--color-text-muted) !important; }
    .date-cell { color: var(--color-text-muted); font-size: 0.75rem; white-space: nowrap; font-family: var(--font-mono); }

    .amount-cell {
      display: flex;
      align-items: baseline;
      gap: 0.25rem;
    }

    .amount-value {
      color: var(--color-text-primary);
      font-weight: 600;
    }

    .currency-tag {
      font-size: 0.65rem;
      color: var(--color-text-muted);
      letter-spacing: 0.04em;
    }
  `,
})
export class TransactionsComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly transactions = signal<Transaction[]>([]);

  ngOnInit(): void {
    this.api.getTransactions(50).subscribe({
      next: (data) => { this.transactions.set(data); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  statusVariant(status: string): 'status-authorized' | 'status-declined' | 'status-flagged' {
    if (status === 'AUTHORIZED') return 'status-authorized';
    if (status === 'DECLINED')   return 'status-declined';
    return 'status-flagged';
  }
}

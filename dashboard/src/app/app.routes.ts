import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/overview/overview.component').then(
        (m) => m.OverviewComponent
      ),
  },
  {
    path: 'transactions',
    loadComponent: () =>
      import('./features/transactions/transactions.component').then(
        (m) => m.TransactionsComponent
      ),
  },
  {
    path: 'anomalies',
    loadComponent: () =>
      import('./features/anomalies/anomalies.component').then(
        (m) => m.AnomaliesComponent
      ),
  },
  {
    path: 'reports',
    loadComponent: () =>
      import('./features/reports/reports.component').then(
        (m) => m.ReportsComponent
      ),
  },
  {
    path: 'reports/:id',
    loadComponent: () =>
      import('./features/reports/report-detail.component').then(
        (m) => m.ReportDetailComponent
      ),
  },
  {
    path: 'health',
    loadComponent: () =>
      import('./features/health/health.component').then(
        (m) => m.HealthComponent
      ),
  },
  { path: '**', redirectTo: '' },
];

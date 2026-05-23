import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/components/main-layout/main-layout.component').then(
        (m) => m.MainLayoutComponent
      ),
    children: [
      {
        path: '',
        data: { title: 'Overview' },
        loadComponent: () =>
          import('./features/overview/overview.component').then(
            (m) => m.OverviewComponent
          ),
      },
      {
        path: 'transactions',
        data: { title: 'Transactions' },
        loadComponent: () =>
          import('./features/transactions/transactions.component').then(
            (m) => m.TransactionsComponent
          ),
      },
      {
        path: 'anomalies',
        data: { title: 'Anomalies' },
        loadComponent: () =>
          import('./features/anomalies/anomalies.component').then(
            (m) => m.AnomaliesComponent
          ),
      },
      {
        path: 'reports',
        data: { title: 'Reports' },
        loadComponent: () =>
          import('./features/reports/reports.component').then(
            (m) => m.ReportsComponent
          ),
      },
      {
        path: 'reports/:id',
        data: { title: 'Report Detail' },
        loadComponent: () =>
          import('./features/reports/report-detail.component').then(
            (m) => m.ReportDetailComponent
          ),
      },
      {
        path: 'health',
        data: { title: 'System Health' },
        loadComponent: () =>
          import('./features/health/health.component').then(
            (m) => m.HealthComponent
          ),
      },
      { path: '**', redirectTo: '' },
    ],
  },
];

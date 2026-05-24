import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction } from '../models/transaction.model';
import { Anomaly } from '../models/anomaly.model';
import { Report } from '../models/report.model';
import { MetricsSummary } from '../models/metrics.model';
import { PagedResponse } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  getTransactions(page = 0, size = 20): Observable<PagedResponse<Transaction>> {
    return this.http.get<PagedResponse<Transaction>>(`${this.base}/transactions`, {
      params: { page, size },
    });
  }

  getAnomalies(page = 0, size = 20): Observable<PagedResponse<Anomaly>> {
    return this.http.get<PagedResponse<Anomaly>>(`${this.base}/anomalies`, {
      params: { page, size },
    });
  }

  getReports(limit = 50): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.base}/reports`, {
      params: { limit },
    });
  }

  getReport(id: string): Observable<Report> {
    return this.http.get<Report>(`${this.base}/reports/${id}`);
  }

  getMetrics(): Observable<MetricsSummary> {
    return this.http.get<MetricsSummary>(`${this.base}/metrics/summary`);
  }

  getHealth(): Observable<unknown> {
    return this.http.get(`/actuator/health`);
  }
}

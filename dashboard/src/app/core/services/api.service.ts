import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction } from '../models/transaction.model';
import { Anomaly } from '../models/anomaly.model';
import { Report } from '../models/report.model';
import { MetricsSummary } from '../models/metrics.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  getTransactions(limit = 50): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.base}/transactions`, {
      params: { limit },
    });
  }

  getAnomalies(limit = 50): Observable<Anomaly[]> {
    return this.http.get<Anomaly[]>(`${this.base}/anomalies`, {
      params: { limit },
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

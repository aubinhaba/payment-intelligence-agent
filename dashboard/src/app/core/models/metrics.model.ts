export interface MetricsSummary {
  transactionCount: number;
  anomalyCount: number;
  reportCount: number;
  anomaliesBySeverity: Record<string, number>;
}

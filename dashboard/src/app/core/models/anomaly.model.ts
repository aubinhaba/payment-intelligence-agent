export interface Anomaly {
  id: string;
  transactionId: string;
  type: 'VELOCITY' | 'AMOUNT' | 'GEO' | 'CARD_TESTING' | 'MCC';
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  description: string;
  detectedAt: string;
}

export interface Transaction {
  id: string;
  amount: string;
  currency: string;
  cardHash: string;
  last4: string;
  merchantId: string;
  mcc: string;
  country: string;
  occurredAt: string;
  status: 'AUTHORIZED' | 'DECLINED' | 'FLAGGED';
}

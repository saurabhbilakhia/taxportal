import type { DocumentResponse } from './document';
import type { PaymentResponse } from './payment';

export type OrderStatus =
  | 'OPEN'
  | 'SUBMITTED'
  | 'IN_REVIEW'
  | 'PENDING_APPROVAL'
  | 'FILED'
  | 'CANCELLED';

export interface CreateOrderRequest {
  taxYear: number;
  notes?: string;
}

export interface OrderResponse {
  id: string;
  status: OrderStatus;
  taxYear: number;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
  submittedAt?: string;
  filedAt?: string;
  documents?: DocumentResponse[];
  payments?: PaymentResponse[];
}

export interface OrderListResponse {
  orders: OrderResponse[];
  total: number;
}

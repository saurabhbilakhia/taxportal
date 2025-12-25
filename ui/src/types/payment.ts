export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export interface CreateCheckoutRequest {
  successUrl: string;
  cancelUrl: string;
}

export interface CheckoutResponse {
  checkoutUrl: string;
  sessionId: string;
}

export interface PaymentResponse {
  id: string;
  amountCents: number;
  currency: string;
  status: PaymentStatus;
  createdAt?: string;
  paidAt?: string;
}

import { apiClient } from './axios';
import type { CreateCheckoutRequest, CheckoutResponse } from '../types/payment';

export const paymentsApi = {
  createCheckout: async (
    orderId: string,
    data: CreateCheckoutRequest
  ): Promise<CheckoutResponse> => {
    const response = await apiClient.post<CheckoutResponse>(`/orders/${orderId}/pay`, data);
    return response.data;
  },
};

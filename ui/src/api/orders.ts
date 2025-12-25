import { apiClient } from './axios';
import type {
  CreateOrderRequest,
  OrderResponse,
  OrderListResponse,
  OrderStatus,
} from '../types/order';

export const ordersApi = {
  create: async (data: CreateOrderRequest): Promise<OrderResponse> => {
    const response = await apiClient.post<OrderResponse>('/orders', data);
    return response.data;
  },

  list: async (status?: OrderStatus): Promise<OrderListResponse> => {
    const params = status ? { status } : {};
    const response = await apiClient.get<OrderListResponse>('/orders', { params });
    return response.data;
  },

  getById: async (orderId: string): Promise<OrderResponse> => {
    const response = await apiClient.get<OrderResponse>(`/orders/${orderId}`);
    return response.data;
  },

  submit: async (orderId: string): Promise<OrderResponse> => {
    const response = await apiClient.post<OrderResponse>(`/orders/${orderId}/submit`);
    return response.data;
  },

  cancel: async (orderId: string): Promise<void> => {
    await apiClient.delete(`/orders/${orderId}`);
  },
};

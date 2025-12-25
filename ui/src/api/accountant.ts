import { apiClient } from './axios';
import type {
  DashboardStatsResponse,
  OrderSearchRequest,
  OrderPageResponse,
  ClientResponse,
  ClientDetailResponse,
} from '../types/accountant';
import type { OrderResponse, OrderStatus } from '../types/order';
import type { OrderExtractionResponse, ExtractionResultResponse } from '../types/extraction';

export const accountantApi = {
  getDashboardStats: async (): Promise<DashboardStatsResponse> => {
    const response = await apiClient.get<DashboardStatsResponse>('/accountant/dashboard');
    return response.data;
  },

  searchOrders: async (params: OrderSearchRequest): Promise<OrderPageResponse> => {
    const response = await apiClient.get<OrderPageResponse>('/accountant/orders', { params });
    return response.data;
  },

  getOrderDetail: async (orderId: string): Promise<OrderResponse> => {
    const response = await apiClient.get<OrderResponse>(`/accountant/orders/${orderId}`);
    return response.data;
  },

  updateOrderStatus: async (orderId: string, status: OrderStatus): Promise<OrderResponse> => {
    const response = await apiClient.patch<OrderResponse>(`/accountant/orders/${orderId}/status`, {
      status,
    });
    return response.data;
  },

  getOrderExtractions: async (orderId: string): Promise<OrderExtractionResponse> => {
    const response = await apiClient.get<OrderExtractionResponse>(
      `/accountant/orders/${orderId}/extractions`
    );
    return response.data;
  },

  retryExtraction: async (orderId: string, documentId: string): Promise<ExtractionResultResponse> => {
    const response = await apiClient.post<ExtractionResultResponse>(
      `/accountant/orders/${orderId}/extractions/${documentId}/retry`
    );
    return response.data;
  },

  getClients: async (): Promise<ClientResponse[]> => {
    const response = await apiClient.get<ClientResponse[]>('/accountant/clients');
    return response.data;
  },

  getClientDetail: async (clientId: string): Promise<ClientDetailResponse> => {
    const response = await apiClient.get<ClientDetailResponse>(`/accountant/clients/${clientId}`);
    return response.data;
  },
};

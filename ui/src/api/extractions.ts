import { apiClient } from './axios';
import type { OrderExtractionResponse, ExtractionResultResponse } from '../types/extraction';

export const extractionsApi = {
  getOrderExtractions: async (orderId: string): Promise<OrderExtractionResponse> => {
    const response = await apiClient.get<OrderExtractionResponse>(
      `/orders/${orderId}/extractions`
    );
    return response.data;
  },

  retryExtraction: async (
    orderId: string,
    documentId: string
  ): Promise<ExtractionResultResponse> => {
    const response = await apiClient.post<ExtractionResultResponse>(
      `/orders/${orderId}/extractions/${documentId}/retry`
    );
    return response.data;
  },
};

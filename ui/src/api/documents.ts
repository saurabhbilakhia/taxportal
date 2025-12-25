import { apiClient } from './axios';
import type { DocumentResponse, DocumentUploadResponse } from '../types/document';

export const documentsApi = {
  upload: async (
    orderId: string,
    file: File,
    slipType?: string
  ): Promise<DocumentUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    if (slipType) {
      formData.append('slip_type', slipType);
    }
    const response = await apiClient.post<DocumentUploadResponse>(
      `/orders/${orderId}/documents`,
      formData,
      { headers: { 'Content-Type': undefined } }
    );
    return response.data;
  },

  list: async (orderId: string): Promise<DocumentResponse[]> => {
    const response = await apiClient.get<DocumentResponse[]>(`/orders/${orderId}/documents`);
    return response.data;
  },

  getById: async (orderId: string, documentId: string): Promise<DocumentResponse> => {
    const response = await apiClient.get<DocumentResponse>(
      `/orders/${orderId}/documents/${documentId}`
    );
    return response.data;
  },

  download: async (orderId: string, documentId: string): Promise<Blob> => {
    const response = await apiClient.get(`/orders/${orderId}/documents/${documentId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  delete: async (orderId: string, documentId: string): Promise<void> => {
    await apiClient.delete(`/orders/${orderId}/documents/${documentId}`);
  },
};

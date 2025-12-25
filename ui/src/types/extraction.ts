export type ExtractionStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface ExtractionResultResponse {
  id: string;
  documentId: string;
  documentName: string;
  status: ExtractionStatus;
  extractedData?: Record<string, unknown>;
  errorMessage?: string;
  createdAt?: string;
  completedAt?: string;
}

export interface OrderExtractionResponse {
  orderId: string;
  totalDocuments: number;
  completedExtractions: number;
  pendingExtractions: number;
  failedExtractions: number;
  results: ExtractionResultResponse[];
}

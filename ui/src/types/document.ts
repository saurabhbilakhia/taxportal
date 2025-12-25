export interface DocumentResponse {
  id: string;
  fileName: string;
  originalFileName: string;
  fileSize?: number;
  mimeType?: string;
  slipType?: string;
  uploadedAt: string;
}

export interface DocumentUploadResponse {
  id: string;
  fileName: string;
  slipType?: string;
  uploadedAt: string;
}

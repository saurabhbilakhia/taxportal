import type { OrderStatus } from './order';

export interface DashboardStatsResponse {
  totalOrders: number;
  ordersByStatus: Record<string, number>;
  pendingReview: number;
  filedThisMonth: number;
  filedThisYear: number;
  totalClients: number;
}

export interface OrderSearchRequest {
  status?: OrderStatus;
  clientEmail?: string;
  taxYear?: number;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface OrderListItemResponse {
  id: string;
  clientEmail: string;
  clientName?: string;
  taxYear: number;
  status: OrderStatus;
  documentCount: number;
  createdAt?: string;
  submittedAt?: string;
  filedAt?: string;
}

export interface OrderPageResponse {
  orders: OrderListItemResponse[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface ClientResponse {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  orderCount: number;
  createdAt?: string;
}

export interface ClientDetailResponse {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  createdAt?: string;
  orders: OrderListItemResponse[];
}

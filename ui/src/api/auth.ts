import { apiClient } from './axios';
import type {
  RegisterRequest,
  ClientRegisterRequest,
  AccountantRegisterRequest,
  LoginRequest,
  AuthResponse,
  AccountantRegisterResponse,
  UserResponse,
  ChangePasswordRequest,
} from '../types/auth';

export const authApi = {
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/register', data);
    return response.data;
  },

  registerClient: async (data: ClientRegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/register/client', data);
    return response.data;
  },

  registerAccountant: async (data: AccountantRegisterRequest): Promise<AccountantRegisterResponse> => {
    const response = await apiClient.post<AccountantRegisterResponse>('/auth/register/accountant', data);
    return response.data;
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', data);
    return response.data;
  },

  getCurrentUser: async (): Promise<UserResponse> => {
    const response = await apiClient.get<UserResponse>('/auth/me');
    return response.data;
  },

  changePassword: async (data: ChangePasswordRequest): Promise<void> => {
    await apiClient.post('/auth/change-password', data);
  },
};

export type UserRole = 'CLIENT' | 'ACCOUNTANT' | 'ADMIN';

export type AccountStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface RegisterRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
}

export interface ClientRegisterRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
}

export interface AccountantRegisterRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  licenseNumber: string;
  firmName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
  user: UserResponse;
}

export interface AccountantRegisterResponse {
  message: string;
  status: string;
}

export interface UserResponse {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role?: UserRole;
  status?: AccountStatus;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

import { createContext } from 'react';
import type {
  UserResponse,
  LoginRequest,
  RegisterRequest,
  ClientRegisterRequest,
  AccountantRegisterRequest,
} from '../types/auth';

export interface AuthContextType {
  user: UserResponse | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  registerClient: (data: ClientRegisterRequest) => Promise<void>;
  registerAccountant: (data: AccountantRegisterRequest) => Promise<{ requiresApproval: boolean }>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

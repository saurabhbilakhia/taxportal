import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { authApi } from '../api/auth';
import type {
  UserResponse,
  LoginRequest,
  RegisterRequest,
  ClientRegisterRequest,
  AccountantRegisterRequest,
  AuthResponse,
} from '../types/auth';

interface AuthContextType {
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

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('auth_token'));
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      if (token) {
        try {
          const currentUser = await authApi.getCurrentUser();
          setUser(currentUser);
        } catch {
          localStorage.removeItem('auth_token');
          setToken(null);
        }
      }
      setIsLoading(false);
    };
    initAuth();
  }, [token]);

  const handleAuthResponse = (response: AuthResponse) => {
    localStorage.setItem('auth_token', response.token);
    setToken(response.token);
    setUser(response.user);
  };

  const login = async (data: LoginRequest) => {
    const response = await authApi.login(data);
    handleAuthResponse(response);
  };

  const register = async (data: RegisterRequest) => {
    const response = await authApi.register(data);
    handleAuthResponse(response);
  };

  const registerClient = async (data: ClientRegisterRequest) => {
    const response = await authApi.registerClient(data);
    handleAuthResponse(response);
  };

  const registerAccountant = async (data: AccountantRegisterRequest) => {
    await authApi.registerAccountant(data);
    return { requiresApproval: true };
  };

  const logout = () => {
    localStorage.removeItem('auth_token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isLoading,
        isAuthenticated: !!token && !!user,
        login,
        register,
        registerClient,
        registerAccountant,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

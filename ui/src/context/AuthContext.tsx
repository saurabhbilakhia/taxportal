import { useState, useEffect, type ReactNode } from 'react';
import { authApi } from '../api/auth';
import type { AuthResponse } from '../types/auth';
import { AuthContext } from './authContextDef';
import type {
  LoginRequest,
  RegisterRequest,
  ClientRegisterRequest,
  AccountantRegisterRequest,
} from '../types/auth';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<import('../types/auth').UserResponse | null>(null);
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

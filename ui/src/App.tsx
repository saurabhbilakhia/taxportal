import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProtectedRoute, RoleBasedRoute } from './components/navigation';
import { ClientLayout, AccountantLayout } from './components/layout';
import { LoadingPage } from './components/shared';

import LoginPage from './pages/auth/LoginPage';
import RegisterSelectPage from './pages/auth/RegisterSelectPage';
import ClientRegisterPage from './pages/auth/ClientRegisterPage';
import AccountantRegisterPage from './pages/auth/AccountantRegisterPage';
import ChangePasswordPage from './pages/auth/ChangePasswordPage';

import ClientDashboard from './pages/client/Dashboard';
import ClientOrdersPage from './pages/client/OrdersPage';
import ClientOrderDetailPage from './pages/client/OrderDetailPage';
import ProfilePage from './pages/client/ProfilePage';

import AccountantDashboard from './pages/accountant/Dashboard';
import AccountantOrdersPage from './pages/accountant/OrdersPage';
import AccountantOrderDetailPage from './pages/accountant/OrderDetailPage';
import ClientsPage from './pages/accountant/ClientsPage';
import ClientDetailPage from './pages/accountant/ClientDetailPage';

import NotFoundPage from './pages/error/NotFoundPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      retry: 1,
    },
  },
});

function RootRedirect() {
  const { user, isLoading, isAuthenticated } = useAuth();

  if (isLoading) {
    return <LoadingPage />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role === 'ACCOUNTANT' || user?.role === 'ADMIN') {
    return <Navigate to="/accountant" replace />;
  }

  return <Navigate to="/client" replace />;
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<RootRedirect />} />

            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterSelectPage />} />
            <Route path="/register/client" element={<ClientRegisterPage />} />
            <Route path="/register/accountant" element={<AccountantRegisterPage />} />

            <Route element={<ProtectedRoute />}>
              <Route path="/change-password" element={<ChangePasswordPage />} />

              <Route element={<RoleBasedRoute allowedRoles={['CLIENT']} />}>
                <Route element={<ClientLayout />}>
                  <Route path="/client" element={<ClientDashboard />} />
                  <Route path="/client/orders" element={<ClientOrdersPage />} />
                  <Route path="/client/orders/:orderId" element={<ClientOrderDetailPage />} />
                  <Route path="/client/profile" element={<ProfilePage />} />
                </Route>
              </Route>

              <Route element={<RoleBasedRoute allowedRoles={['ACCOUNTANT', 'ADMIN']} />}>
                <Route element={<AccountantLayout />}>
                  <Route path="/accountant" element={<AccountantDashboard />} />
                  <Route path="/accountant/orders" element={<AccountantOrdersPage />} />
                  <Route path="/accountant/orders/:orderId" element={<AccountantOrderDetailPage />} />
                  <Route path="/accountant/clients" element={<ClientsPage />} />
                  <Route path="/accountant/clients/:clientId" element={<ClientDetailPage />} />
                </Route>
              </Route>
            </Route>

            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;

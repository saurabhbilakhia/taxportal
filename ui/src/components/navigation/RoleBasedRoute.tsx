import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LoadingPage } from '../shared/LoadingSpinner';
import type { UserRole } from '../../types/auth';

interface RoleBasedRouteProps {
  allowedRoles: UserRole[];
}

export function RoleBasedRoute({ allowedRoles }: RoleBasedRouteProps) {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingPage />;
  }

  if (!user?.role || !allowedRoles.includes(user.role)) {
    const redirectPath = user?.role === 'CLIENT' ? '/client' : '/accountant';
    return <Navigate to={redirectPath} replace />;
  }

  return <Outlet />;
}

import { Link } from 'react-router-dom';
import { Button } from '../../components/shared';
import { useAuth } from '../../hooks/useAuth';

export default function UnauthorizedPage() {
  const { user, logout } = useAuth();

  const homePath = user?.role === 'ACCOUNTANT' || user?.role === 'ADMIN'
    ? '/accountant'
    : '/client';

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-gray-50">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-brand-green mb-4">403</h1>
        <h2 className="text-2xl font-semibold text-gray-700 mb-4">Access Denied</h2>
        <p className="text-gray-500 mb-8 max-w-md">
          You don't have permission to access this page.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link to={homePath}>
            <Button>Go to Dashboard</Button>
          </Link>
          <Button variant="secondary" onClick={logout}>
            Sign Out
          </Button>
        </div>
      </div>
    </div>
  );
}

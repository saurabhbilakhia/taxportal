import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { Card, Button } from '../../components/shared';

export default function ProfilePage() {
  const { user, logout } = useAuth();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-brand-green">Profile</h1>

      <Card>
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 rounded-full bg-brand-light flex items-center justify-center">
            <span className="text-2xl font-bold text-brand-green">
              {user?.firstName?.[0] || user?.email?.[0]?.toUpperCase() || 'U'}
            </span>
          </div>
          <div>
            <p className="text-lg font-semibold text-brand-green">
              {user?.firstName && user?.lastName
                ? `${user.firstName} ${user.lastName}`
                : 'Tax Portal User'}
            </p>
            <p className="text-gray-500">{user?.email}</p>
          </div>
        </div>

        <dl className="space-y-4">
          <div className="flex justify-between py-3 border-t border-gray-200">
            <dt className="text-gray-500">Email</dt>
            <dd className="font-medium">{user?.email}</dd>
          </div>
          {user?.firstName && (
            <div className="flex justify-between py-3 border-t border-gray-200">
              <dt className="text-gray-500">First Name</dt>
              <dd className="font-medium">{user.firstName}</dd>
            </div>
          )}
          {user?.lastName && (
            <div className="flex justify-between py-3 border-t border-gray-200">
              <dt className="text-gray-500">Last Name</dt>
              <dd className="font-medium">{user.lastName}</dd>
            </div>
          )}
          <div className="flex justify-between py-3 border-t border-gray-200">
            <dt className="text-gray-500">Account Type</dt>
            <dd className="font-medium">Client</dd>
          </div>
        </dl>
      </Card>

      <Card>
        <h2 className="text-lg font-semibold text-brand-green mb-4">Security</h2>
        <Link to="/change-password">
          <Button variant="secondary" className="w-full">
            Change Password
          </Button>
        </Link>
      </Card>

      <Card>
        <h2 className="text-lg font-semibold text-brand-green mb-4">Account Actions</h2>
        <Button variant="danger" onClick={logout} className="w-full">
          Sign Out
        </Button>
      </Card>
    </div>
  );
}

import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../context/AuthContext';
import { ordersApi } from '../../api/orders';
import { Card, Button, OrderStatusBadge, LoadingSpinner, ErrorState } from '../../components/shared';
import { formatDate } from '../../utils/formatters';

export default function ClientDashboard() {
  const { user } = useAuth();

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['orders'],
    queryFn: () => ordersApi.list(),
  });

  const recentOrders = data?.orders?.slice(0, 3) || [];
  const totalOrders = data?.total || 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-brand-green">
          Welcome back{user?.firstName ? `, ${user.firstName}` : ''}!
        </h1>
        <p className="text-gray-600 mt-1">Manage your tax filings</p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Card variant="secondary">
          <p className="text-sm text-gray-600">Total Orders</p>
          <p className="text-2xl font-bold text-brand-green">{totalOrders}</p>
        </Card>
        <Card variant="secondary">
          <p className="text-sm text-gray-600">Tax Year</p>
          <p className="text-2xl font-bold text-brand-green">{new Date().getFullYear()}</p>
        </Card>
      </div>

      <Card padding="none">
        <div className="p-4 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-brand-green">Recent Orders</h2>
            <Link to="/client/orders">
              <Button variant="ghost" size="sm">
                View All
              </Button>
            </Link>
          </div>
        </div>

        <div className="divide-y divide-gray-200">
          {isLoading && (
            <div className="flex justify-center py-8">
              <LoadingSpinner />
            </div>
          )}

          {error && (
            <div className="p-4">
              <ErrorState message="Failed to load orders" onRetry={refetch} />
            </div>
          )}

          {!isLoading && !error && recentOrders.length === 0 && (
            <div className="p-8 text-center">
              <p className="text-gray-500 mb-4">No orders yet</p>
              <Link to="/client/orders">
                <Button>Create Your First Order</Button>
              </Link>
            </div>
          )}

          {recentOrders.map((order) => (
            <Link
              key={order.id}
              to={`/client/orders/${order.id}`}
              className="block p-4 hover:bg-gray-50 transition-colors"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium text-brand-green">Tax Year {order.taxYear}</p>
                  <p className="text-sm text-gray-500">Created {formatDate(order.createdAt)}</p>
                </div>
                <OrderStatusBadge status={order.status} />
              </div>
            </Link>
          ))}
        </div>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2">
        <Link to="/client/orders">
          <Card className="hover:shadow-md transition-shadow cursor-pointer">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-brand-light flex items-center justify-center">
                <svg
                  className="w-6 h-6 text-brand-green"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 4v16m8-8H4"
                  />
                </svg>
              </div>
              <div>
                <p className="font-medium text-brand-green">New Tax Filing</p>
                <p className="text-sm text-gray-500">Start a new order</p>
              </div>
            </div>
          </Card>
        </Link>

        <Link to="/client/profile">
          <Card className="hover:shadow-md transition-shadow cursor-pointer">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-brand-light flex items-center justify-center">
                <svg
                  className="w-6 h-6 text-brand-green"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                  />
                </svg>
              </div>
              <div>
                <p className="font-medium text-brand-green">Your Profile</p>
                <p className="text-sm text-gray-500">View account settings</p>
              </div>
            </div>
          </Card>
        </Link>
      </div>
    </div>
  );
}

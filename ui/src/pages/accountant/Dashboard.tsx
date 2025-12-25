import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { accountantApi } from '../../api/accountant';
import { Card, LoadingSpinner, ErrorState } from '../../components/shared';

export default function AccountantDashboard() {
  const { data: stats, isLoading, error, refetch } = useQuery({
    queryKey: ['accountant-dashboard'],
    queryFn: accountantApi.getDashboardStats,
  });

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return <ErrorState message="Failed to load dashboard" onRetry={refetch} />;
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-brand-green">Dashboard</h1>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <Card variant="secondary">
          <p className="text-sm text-gray-600">Total Orders</p>
          <p className="text-3xl font-bold text-brand-green">{stats?.totalOrders || 0}</p>
        </Card>
        <Card variant="secondary">
          <p className="text-sm text-gray-600">Pending Review</p>
          <p className="text-3xl font-bold text-brand-green">{stats?.pendingReview || 0}</p>
        </Card>
        <Card variant="secondary">
          <p className="text-sm text-gray-600">Filed This Month</p>
          <p className="text-3xl font-bold text-brand-green">{stats?.filedThisMonth || 0}</p>
        </Card>
        <Card variant="secondary">
          <p className="text-sm text-gray-600">Total Clients</p>
          <p className="text-3xl font-bold text-brand-green">{stats?.totalClients || 0}</p>
        </Card>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card>
          <h2 className="text-lg font-semibold text-brand-green mb-4">Orders by Status</h2>
          {stats?.ordersByStatus && Object.keys(stats.ordersByStatus).length > 0 ? (
            <div className="space-y-3">
              {Object.entries(stats.ordersByStatus).map(([status, count]) => (
                <div key={status} className="flex items-center justify-between">
                  <span className="text-gray-600">{status.replace('_', ' ')}</span>
                  <span className="font-semibold text-brand-green">{count}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-500">No orders yet</p>
          )}
        </Card>

        <Card>
          <h2 className="text-lg font-semibold text-brand-green mb-4">Quick Actions</h2>
          <div className="space-y-3">
            <Link
              to="/accountant/orders?status=SUBMITTED"
              className="flex items-center justify-between p-3 rounded-lg bg-brand-light hover:bg-brand-light/80 transition-colors"
            >
              <span className="font-medium text-brand-green">Review Submitted Orders</span>
              <svg className="w-5 h-5 text-brand-green" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
            <Link
              to="/accountant/orders?status=IN_REVIEW"
              className="flex items-center justify-between p-3 rounded-lg bg-brand-light hover:bg-brand-light/80 transition-colors"
            >
              <span className="font-medium text-brand-green">Orders In Review</span>
              <svg className="w-5 h-5 text-brand-green" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
            <Link
              to="/accountant/clients"
              className="flex items-center justify-between p-3 rounded-lg bg-brand-light hover:bg-brand-light/80 transition-colors"
            >
              <span className="font-medium text-brand-green">View All Clients</span>
              <svg className="w-5 h-5 text-brand-green" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
          </div>
        </Card>
      </div>

      <Card>
        <h2 className="text-lg font-semibold text-brand-green mb-4">Year Summary</h2>
        <div className="grid grid-cols-2 gap-6">
          <div>
            <p className="text-sm text-gray-500">Filed This Year</p>
            <p className="text-2xl font-bold text-brand-green">{stats?.filedThisYear || 0}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Active Clients</p>
            <p className="text-2xl font-bold text-brand-green">{stats?.totalClients || 0}</p>
          </div>
        </div>
      </Card>
    </div>
  );
}

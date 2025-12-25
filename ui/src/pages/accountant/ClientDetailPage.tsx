import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { accountantApi } from '../../api/accountant';
import {
  Card,
  Button,
  OrderStatusBadge,
  LoadingSpinner,
  ErrorState,
  EmptyState,
} from '../../components/shared';
import { formatDate } from '../../utils/formatters';

export default function ClientDetailPage() {
  const { clientId } = useParams<{ clientId: string }>();

  const { data: client, isLoading, error, refetch } = useQuery({
    queryKey: ['accountant-client', clientId],
    queryFn: () => accountantApi.getClientDetail(clientId!),
    enabled: !!clientId,
  });

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <LoadingSpinner />
      </div>
    );
  }

  if (error || !client) {
    return <ErrorState message="Failed to load client" onRetry={refetch} />;
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-brand-green">Client Details</h1>

      <Card>
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 rounded-full bg-brand-light flex items-center justify-center">
            <span className="text-2xl font-bold text-brand-green">
              {client.firstName?.[0] || client.email[0].toUpperCase()}
            </span>
          </div>
          <div>
            <p className="text-lg font-semibold text-brand-green">
              {client.firstName && client.lastName
                ? `${client.firstName} ${client.lastName}`
                : 'Client'}
            </p>
            <p className="text-gray-500">{client.email}</p>
          </div>
        </div>

        <dl className="space-y-3 text-sm">
          <div className="flex justify-between py-2 border-t border-gray-200">
            <dt className="text-gray-500">Email</dt>
            <dd className="font-medium">{client.email}</dd>
          </div>
          {client.firstName && (
            <div className="flex justify-between py-2 border-t border-gray-200">
              <dt className="text-gray-500">First Name</dt>
              <dd className="font-medium">{client.firstName}</dd>
            </div>
          )}
          {client.lastName && (
            <div className="flex justify-between py-2 border-t border-gray-200">
              <dt className="text-gray-500">Last Name</dt>
              <dd className="font-medium">{client.lastName}</dd>
            </div>
          )}
          {client.phone && (
            <div className="flex justify-between py-2 border-t border-gray-200">
              <dt className="text-gray-500">Phone</dt>
              <dd className="font-medium">{client.phone}</dd>
            </div>
          )}
          <div className="flex justify-between py-2 border-t border-gray-200">
            <dt className="text-gray-500">Member Since</dt>
            <dd className="font-medium">{formatDate(client.createdAt)}</dd>
          </div>
        </dl>
      </Card>

      <Card padding="none">
        <div className="p-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-brand-green">Orders</h2>
          <p className="text-sm text-gray-500">{client.orders?.length || 0} total orders</p>
        </div>

        {(!client.orders || client.orders.length === 0) && (
          <div className="p-8">
            <EmptyState
              title="No orders"
              description="This client hasn't created any orders yet"
            />
          </div>
        )}

        <div className="divide-y divide-gray-200">
          {client.orders?.map((order) => (
            <Link
              key={order.id}
              to={`/accountant/orders/${order.id}`}
              className="block p-4 hover:bg-gray-50 transition-colors"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium text-brand-green">Tax Year {order.taxYear}</p>
                  <p className="text-sm text-gray-500">
                    {order.documentCount} documents • Created {formatDate(order.createdAt)}
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  <OrderStatusBadge status={order.status} />
                  <svg
                    className="w-5 h-5 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 5l7 7-7 7"
                    />
                  </svg>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </Card>

      <div className="flex justify-center">
        <Link to="/accountant/clients">
          <Button variant="secondary">Back to Clients</Button>
        </Link>
      </div>
    </div>
  );
}

import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { accountantApi } from '../../api/accountant';
import {
  Card,
  Button,
  Input,
  Select,
  OrderStatusBadge,
  LoadingSpinner,
  ErrorState,
  EmptyState,
} from '../../components/shared';
import { formatDate } from '../../utils/formatters';
import { ORDER_STATUS_OPTIONS, TAX_YEARS } from '../../utils/constants';
import type { OrderStatus } from '../../types/order';

export default function AccountantOrdersPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [filters, setFilters] = useState({
    status: (searchParams.get('status') as OrderStatus) || '',
    clientEmail: searchParams.get('clientEmail') || '',
    taxYear: searchParams.get('taxYear') ? Number(searchParams.get('taxYear')) : undefined,
    page: 0,
    size: 20,
  });

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['accountant-orders', filters],
    queryFn: () =>
      accountantApi.searchOrders({
        status: filters.status || undefined,
        clientEmail: filters.clientEmail || undefined,
        taxYear: filters.taxYear,
        page: filters.page,
        size: filters.size,
      }),
  });

  const handleFilterChange = (key: string, value: string | number | undefined) => {
    setFilters((prev) => ({ ...prev, [key]: value, page: 0 }));
    if (value) {
      searchParams.set(key, String(value));
    } else {
      searchParams.delete(key);
    }
    setSearchParams(searchParams);
  };

  const orders = data?.orders || [];
  const totalPages = data?.totalPages || 0;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-brand-green">Orders</h1>

      <Card>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Select
            label="Status"
            value={filters.status}
            onChange={(e) => handleFilterChange('status', e.target.value)}
            options={[{ value: '', label: 'All Statuses' }, ...ORDER_STATUS_OPTIONS]}
          />
          <Select
            label="Tax Year"
            value={filters.taxYear || ''}
            onChange={(e) =>
              handleFilterChange('taxYear', e.target.value ? Number(e.target.value) : undefined)
            }
            options={[{ value: '', label: 'All Years' }, ...TAX_YEARS]}
          />
          <Input
            label="Client Email"
            placeholder="Search by email..."
            value={filters.clientEmail}
            onChange={(e) => handleFilterChange('clientEmail', e.target.value)}
          />
        </div>
      </Card>

      {isLoading && (
        <div className="flex justify-center py-12">
          <LoadingSpinner />
        </div>
      )}

      {error && <ErrorState message="Failed to load orders" onRetry={refetch} />}

      {!isLoading && !error && orders.length === 0 && (
        <EmptyState
          title="No orders found"
          description="Try adjusting your filters"
        />
      )}

      {!isLoading && !error && orders.length > 0 && (
        <>
          <div className="hidden lg:block">
            <Card padding="none">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Client
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Tax Year
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Status
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Documents
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Created
                    </th>
                    <th className="px-4 py-3"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {orders.map((order) => (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-4 py-4">
                        <p className="font-medium text-brand-green">
                          {order.clientName || order.clientEmail}
                        </p>
                        {order.clientName && (
                          <p className="text-sm text-gray-500">{order.clientEmail}</p>
                        )}
                      </td>
                      <td className="px-4 py-4 text-gray-600">{order.taxYear}</td>
                      <td className="px-4 py-4">
                        <OrderStatusBadge status={order.status} />
                      </td>
                      <td className="px-4 py-4 text-gray-600">{order.documentCount}</td>
                      <td className="px-4 py-4 text-gray-500">{formatDate(order.createdAt)}</td>
                      <td className="px-4 py-4 text-right">
                        <Link to={`/accountant/orders/${order.id}`}>
                          <Button variant="ghost" size="sm">
                            View
                          </Button>
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </Card>
          </div>

          <div className="lg:hidden space-y-4">
            {orders.map((order) => (
              <Link key={order.id} to={`/accountant/orders/${order.id}`}>
                <Card className="hover:shadow-md transition-shadow">
                  <div className="flex items-start justify-between">
                    <div>
                      <p className="font-semibold text-brand-green">
                        {order.clientName || order.clientEmail}
                      </p>
                      {order.clientName && (
                        <p className="text-sm text-gray-500">{order.clientEmail}</p>
                      )}
                      <p className="text-sm text-gray-600 mt-1">
                        Tax Year {order.taxYear} • {order.documentCount} documents
                      </p>
                      <p className="text-sm text-gray-500 mt-1">{formatDate(order.createdAt)}</p>
                    </div>
                    <OrderStatusBadge status={order.status} />
                  </div>
                </Card>
              </Link>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <Button
                variant="secondary"
                size="sm"
                disabled={filters.page === 0}
                onClick={() => setFilters((prev) => ({ ...prev, page: prev.page - 1 }))}
              >
                Previous
              </Button>
              <span className="text-sm text-gray-500">
                Page {filters.page + 1} of {totalPages}
              </span>
              <Button
                variant="secondary"
                size="sm"
                disabled={filters.page >= totalPages - 1}
                onClick={() => setFilters((prev) => ({ ...prev, page: prev.page + 1 }))}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

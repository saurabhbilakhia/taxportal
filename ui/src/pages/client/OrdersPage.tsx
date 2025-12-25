import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ordersApi } from '../../api/orders';
import {
  Card,
  Button,
  Select,
  Modal,
  Input,
  OrderStatusBadge,
  LoadingSpinner,
  ErrorState,
  EmptyState,
} from '../../components/shared';
import { formatDate } from '../../utils/formatters';
import { ORDER_STATUS_OPTIONS, TAX_YEARS } from '../../utils/constants';
import type { OrderStatus } from '../../types/order';

export default function ClientOrdersPage() {
  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState<OrderStatus | ''>('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newOrderYear, setNewOrderYear] = useState(new Date().getFullYear());
  const [newOrderNotes, setNewOrderNotes] = useState('');

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['orders', statusFilter],
    queryFn: () => ordersApi.list(statusFilter || undefined),
  });

  const createMutation = useMutation({
    mutationFn: ordersApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      setShowCreateModal(false);
      setNewOrderNotes('');
    },
  });

  const handleCreateOrder = () => {
    createMutation.mutate({
      taxYear: newOrderYear,
      notes: newOrderNotes || undefined,
    });
  };

  const orders = data?.orders || [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <h1 className="text-2xl font-bold text-brand-green">My Orders</h1>
        <Button onClick={() => setShowCreateModal(true)}>New Order</Button>
      </div>

      <Card padding="sm">
        <Select
          label="Filter by Status"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as OrderStatus | '')}
          options={[{ value: '', label: 'All Statuses' }, ...ORDER_STATUS_OPTIONS]}
        />
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
          description={
            statusFilter
              ? 'Try changing the filter or create a new order'
              : 'Create your first order to get started'
          }
          action={{
            label: 'Create Order',
            onClick: () => setShowCreateModal(true),
          }}
        />
      )}

      {!isLoading && !error && orders.length > 0 && (
        <div className="space-y-4">
          {orders.map((order) => (
            <Link key={order.id} to={`/client/orders/${order.id}`}>
              <Card className="hover:shadow-md transition-shadow">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-semibold text-brand-green">Tax Year {order.taxYear}</p>
                    <p className="text-sm text-gray-500 mt-1">
                      Created {formatDate(order.createdAt)}
                    </p>
                    {order.notes && (
                      <p className="text-sm text-gray-600 mt-2 line-clamp-1">{order.notes}</p>
                    )}
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <OrderStatusBadge status={order.status} />
                    <span className="text-sm text-gray-500">
                      {order.documents?.length || 0} documents
                    </span>
                  </div>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}

      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Create New Order"
      >
        <div className="space-y-4">
          <Select
            label="Tax Year"
            value={newOrderYear}
            onChange={(e) => setNewOrderYear(Number(e.target.value))}
            options={TAX_YEARS}
          />
          <Input
            label="Notes (optional)"
            placeholder="Any additional notes..."
            value={newOrderNotes}
            onChange={(e) => setNewOrderNotes(e.target.value)}
          />
          <div className="flex gap-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => setShowCreateModal(false)}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button
              onClick={handleCreateOrder}
              isLoading={createMutation.isPending}
              className="flex-1"
            >
              Create
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

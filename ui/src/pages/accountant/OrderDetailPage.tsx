import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { accountantApi } from '../../api/accountant';
import { documentsApi } from '../../api/documents';
import {
  Card,
  Button,
  Select,
  Modal,
  OrderStatusBadge,
  PaymentStatusBadge,
  ExtractionStatusBadge,
  LoadingSpinner,
  ErrorState,
} from '../../components/shared';
import { formatDate, formatFileSize, formatCurrency } from '../../utils/formatters';
import { ORDER_STATUS_OPTIONS } from '../../utils/constants';
import type { OrderStatus } from '../../types/order';

export default function AccountantOrderDetailPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const queryClient = useQueryClient();

  const [showStatusModal, setShowStatusModal] = useState(false);
  const [newStatus, setNewStatus] = useState<OrderStatus | ''>('');

  const { data: order, isLoading, error, refetch } = useQuery({
    queryKey: ['accountant-order', orderId],
    queryFn: () => accountantApi.getOrderDetail(orderId!),
    enabled: !!orderId,
  });

  const { data: extractions } = useQuery({
    queryKey: ['extractions', orderId],
    queryFn: () => accountantApi.getOrderExtractions(orderId!),
    enabled: !!orderId,
  });

  const updateStatusMutation = useMutation({
    mutationFn: (status: OrderStatus) => accountantApi.updateOrderStatus(orderId!, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accountant-order', orderId] });
      queryClient.invalidateQueries({ queryKey: ['accountant-orders'] });
      setShowStatusModal(false);
    },
  });

  const retryExtractionMutation = useMutation({
    mutationFn: (documentId: string) => accountantApi.retryExtraction(orderId!, documentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['extractions', orderId] });
    },
  });

  const handleDownload = async (documentId: string, fileName: string) => {
    const blob = await documentsApi.download(orderId!, documentId);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <LoadingSpinner />
      </div>
    );
  }

  if (error || !order) {
    return <ErrorState message="Failed to load order" onRetry={refetch} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-brand-green">Order Details</h1>
          <p className="text-gray-500 mt-1">Tax Year {order.taxYear}</p>
        </div>
        <div className="flex items-center gap-3">
          <OrderStatusBadge status={order.status} />
          <Button size="sm" onClick={() => setShowStatusModal(true)}>
            Update Status
          </Button>
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card>
          <h2 className="text-lg font-semibold text-brand-green mb-4">Order Information</h2>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between">
              <dt className="text-gray-500">Order ID</dt>
              <dd className="font-mono text-xs">{order.id}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">Tax Year</dt>
              <dd className="font-medium">{order.taxYear}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">Status</dt>
              <dd className="font-medium">{order.status.replace('_', ' ')}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">Created</dt>
              <dd>{formatDate(order.createdAt)}</dd>
            </div>
            {order.submittedAt && (
              <div className="flex justify-between">
                <dt className="text-gray-500">Submitted</dt>
                <dd>{formatDate(order.submittedAt)}</dd>
              </div>
            )}
            {order.filedAt && (
              <div className="flex justify-between">
                <dt className="text-gray-500">Filed</dt>
                <dd>{formatDate(order.filedAt)}</dd>
              </div>
            )}
          </dl>
          {order.notes && (
            <div className="mt-4 pt-4 border-t border-gray-200">
              <p className="text-sm text-gray-500 mb-1">Notes</p>
              <p className="text-sm">{order.notes}</p>
            </div>
          )}
        </Card>

        {order.payments && order.payments.length > 0 && (
          <Card>
            <h2 className="text-lg font-semibold text-brand-green mb-4">Payments</h2>
            <div className="space-y-3">
              {order.payments.map((payment) => (
                <div key={payment.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium">
                      {formatCurrency(payment.amountCents, payment.currency)}
                    </p>
                    <p className="text-sm text-gray-500">{formatDate(payment.createdAt)}</p>
                  </div>
                  <PaymentStatusBadge status={payment.status} />
                </div>
              ))}
            </div>
          </Card>
        )}
      </div>

      <Card padding="none">
        <div className="p-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-brand-green">Documents</h2>
        </div>
        <div className="divide-y divide-gray-200">
          {(!order.documents || order.documents.length === 0) && (
            <div className="p-8 text-center text-gray-500">No documents uploaded</div>
          )}
          {order.documents?.map((doc) => (
            <div key={doc.id} className="p-4 flex items-center justify-between">
              <div className="min-w-0 flex-1">
                <p className="font-medium truncate">{doc.originalFileName}</p>
                <p className="text-sm text-gray-500">
                  {doc.slipType && <span className="mr-2">{doc.slipType}</span>}
                  {formatFileSize(doc.fileSize)} • {formatDate(doc.uploadedAt)}
                </p>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => handleDownload(doc.id, doc.originalFileName)}
              >
                Download
              </Button>
            </div>
          ))}
        </div>
      </Card>

      {extractions && extractions.results.length > 0 && (
        <Card padding="none">
          <div className="p-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-brand-green">Extraction Results</h2>
            <p className="text-sm text-gray-500 mt-1">
              {extractions.completedExtractions} of {extractions.totalDocuments} completed
            </p>
          </div>
          <div className="divide-y divide-gray-200">
            {extractions.results.map((result) => (
              <div key={result.id} className="p-4">
                <div className="flex items-center justify-between mb-2">
                  <p className="font-medium">{result.documentName}</p>
                  <ExtractionStatusBadge status={result.status} />
                </div>
                {result.errorMessage && (
                  <p className="text-sm text-red-600 mb-2">{result.errorMessage}</p>
                )}
                {result.status === 'FAILED' && (
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => retryExtractionMutation.mutate(result.documentId)}
                    isLoading={retryExtractionMutation.isPending}
                  >
                    Retry Extraction
                  </Button>
                )}
                {result.status === 'COMPLETED' && result.extractedData && (
                  <details className="mt-2">
                    <summary className="text-sm text-brand-green cursor-pointer">
                      View Extracted Data
                    </summary>
                    <pre className="mt-2 p-3 bg-gray-50 rounded text-xs overflow-auto max-h-48">
                      {JSON.stringify(result.extractedData, null, 2)}
                    </pre>
                  </details>
                )}
              </div>
            ))}
          </div>
        </Card>
      )}

      <Modal
        isOpen={showStatusModal}
        onClose={() => setShowStatusModal(false)}
        title="Update Order Status"
      >
        <div className="space-y-4">
          <Select
            label="New Status"
            value={newStatus}
            onChange={(e) => setNewStatus(e.target.value as OrderStatus)}
            options={ORDER_STATUS_OPTIONS}
            placeholder="Select status..."
          />
          <div className="flex gap-3 pt-4">
            <Button variant="secondary" onClick={() => setShowStatusModal(false)} className="flex-1">
              Cancel
            </Button>
            <Button
              onClick={() => newStatus && updateStatusMutation.mutate(newStatus)}
              isLoading={updateStatusMutation.isPending}
              disabled={!newStatus}
              className="flex-1"
            >
              Update
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

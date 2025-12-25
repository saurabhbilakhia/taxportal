import { useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ordersApi } from '../../api/orders';
import { documentsApi } from '../../api/documents';
import { paymentsApi } from '../../api/payments';
import { extractionsApi } from '../../api/extractions';
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
import { SLIP_TYPES } from '../../utils/constants';

export default function ClientOrderDetailPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadSlipType, setUploadSlipType] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [showCancelModal, setShowCancelModal] = useState(false);

  const { data: order, isLoading, error, refetch } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => ordersApi.getById(orderId!),
    enabled: !!orderId,
  });

  const { data: extractions } = useQuery({
    queryKey: ['extractions', orderId],
    queryFn: () => extractionsApi.getOrderExtractions(orderId!),
    enabled: !!orderId && order?.status !== 'OPEN',
  });

  const uploadMutation = useMutation({
    mutationFn: ({ file, slipType }: { file: File; slipType?: string }) =>
      documentsApi.upload(orderId!, file, slipType),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
      setShowUploadModal(false);
      setSelectedFile(null);
      setUploadSlipType('');
    },
  });

  const submitMutation = useMutation({
    mutationFn: () => ordersApi.submit(orderId!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });

  const cancelMutation = useMutation({
    mutationFn: () => ordersApi.cancel(orderId!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      navigate('/client/orders');
    },
  });

  const deleteDocMutation = useMutation({
    mutationFn: (documentId: string) => documentsApi.delete(orderId!, documentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
    },
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setShowUploadModal(true);
    }
  };

  const handleUpload = () => {
    if (selectedFile) {
      uploadMutation.mutate({
        file: selectedFile,
        slipType: uploadSlipType || undefined,
      });
    }
  };

  const handleDownload = async (documentId: string, fileName: string) => {
    const blob = await documentsApi.download(orderId!, documentId);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handlePayment = async () => {
    const checkout = await paymentsApi.createCheckout(orderId!, {
      successUrl: `${window.location.origin}/client/orders/${orderId}?payment=success`,
      cancelUrl: `${window.location.origin}/client/orders/${orderId}?payment=cancelled`,
    });
    window.location.href = checkout.checkoutUrl;
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

  const canEdit = order.status === 'OPEN';
  const canSubmit = order.status === 'OPEN' && (order.documents?.length || 0) > 0;
  const canPay = order.status === 'PENDING_APPROVAL';

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-brand-green">Tax Year {order.taxYear}</h1>
          <p className="text-gray-500 mt-1">Order Details</p>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <Card>
        <h2 className="text-lg font-semibold text-brand-green mb-4">Order Information</h2>
        <dl className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <dt className="text-gray-500">Created</dt>
            <dd className="font-medium">{formatDate(order.createdAt)}</dd>
          </div>
          <div>
            <dt className="text-gray-500">Status</dt>
            <dd className="font-medium">{order.status.replace('_', ' ')}</dd>
          </div>
          {order.submittedAt && (
            <div>
              <dt className="text-gray-500">Submitted</dt>
              <dd className="font-medium">{formatDate(order.submittedAt)}</dd>
            </div>
          )}
          {order.filedAt && (
            <div>
              <dt className="text-gray-500">Filed</dt>
              <dd className="font-medium">{formatDate(order.filedAt)}</dd>
            </div>
          )}
        </dl>
        {order.notes && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <p className="text-sm text-gray-500">Notes</p>
            <p className="mt-1">{order.notes}</p>
          </div>
        )}
      </Card>

      <Card padding="none">
        <div className="p-4 border-b border-gray-200 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-brand-green">Documents</h2>
          {canEdit && (
            <>
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={handleFileChange}
              />
              <Button size="sm" onClick={() => fileInputRef.current?.click()}>
                Upload
              </Button>
            </>
          )}
        </div>

        <div className="divide-y divide-gray-200">
          {(!order.documents || order.documents.length === 0) && (
            <div className="p-8 text-center text-gray-500">No documents uploaded yet</div>
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
              <div className="flex items-center gap-2 ml-4">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleDownload(doc.id, doc.originalFileName)}
                >
                  Download
                </Button>
                {canEdit && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => deleteDocMutation.mutate(doc.id)}
                  >
                    Delete
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      </Card>

      {order.payments && order.payments.length > 0 && (
        <Card>
          <h2 className="text-lg font-semibold text-brand-green mb-4">Payments</h2>
          <div className="space-y-3">
            {order.payments.map((payment) => (
              <div key={payment.id} className="flex items-center justify-between">
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

      {extractions && extractions.results.length > 0 && (
        <Card>
          <h2 className="text-lg font-semibold text-brand-green mb-4">Extraction Status</h2>
          <div className="space-y-3">
            {extractions.results.map((result) => (
              <div key={result.id} className="flex items-center justify-between">
                <div>
                  <p className="font-medium">{result.documentName}</p>
                  {result.errorMessage && (
                    <p className="text-sm text-red-600">{result.errorMessage}</p>
                  )}
                </div>
                <ExtractionStatusBadge status={result.status} />
              </div>
            ))}
          </div>
        </Card>
      )}

      <div className="flex flex-col sm:flex-row gap-3">
        {canSubmit && (
          <Button
            onClick={() => submitMutation.mutate()}
            isLoading={submitMutation.isPending}
            className="flex-1"
          >
            Submit Order
          </Button>
        )}
        {canPay && (
          <Button onClick={handlePayment} className="flex-1">
            Pay Now
          </Button>
        )}
        {canEdit && (
          <Button variant="danger" onClick={() => setShowCancelModal(true)} className="flex-1">
            Cancel Order
          </Button>
        )}
      </div>

      <Modal
        isOpen={showUploadModal}
        onClose={() => {
          setShowUploadModal(false);
          setSelectedFile(null);
        }}
        title="Upload Document"
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            File: <span className="font-medium">{selectedFile?.name}</span>
          </p>
          <Select
            label="Document Type (optional)"
            value={uploadSlipType}
            onChange={(e) => setUploadSlipType(e.target.value)}
            options={[{ value: '', label: 'Select type...' }, ...SLIP_TYPES]}
          />
          <div className="flex gap-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => {
                setShowUploadModal(false);
                setSelectedFile(null);
              }}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button onClick={handleUpload} isLoading={uploadMutation.isPending} className="flex-1">
              Upload
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={showCancelModal}
        onClose={() => setShowCancelModal(false)}
        title="Cancel Order"
      >
        <div className="space-y-4">
          <p className="text-gray-600">
            Are you sure you want to cancel this order? This action cannot be undone.
          </p>
          <div className="flex gap-3 pt-4">
            <Button variant="secondary" onClick={() => setShowCancelModal(false)} className="flex-1">
              Keep Order
            </Button>
            <Button
              variant="danger"
              onClick={() => cancelMutation.mutate()}
              isLoading={cancelMutation.isPending}
              className="flex-1"
            >
              Cancel Order
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

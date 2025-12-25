import clsx from 'clsx';
import type { OrderStatus } from '../../types/order';
import type { PaymentStatus } from '../../types/payment';
import type { ExtractionStatus } from '../../types/extraction';
import {
  getOrderStatusLabel,
  getOrderStatusColor,
  getPaymentStatusLabel,
  getPaymentStatusColor,
  getExtractionStatusLabel,
  getExtractionStatusColor,
} from '../../utils/formatters';

interface OrderStatusBadgeProps {
  status: OrderStatus;
  className?: string;
}

export function OrderStatusBadge({ status, className }: OrderStatusBadgeProps) {
  return (
    <span
      className={clsx(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        getOrderStatusColor(status),
        className
      )}
    >
      {getOrderStatusLabel(status)}
    </span>
  );
}

interface PaymentStatusBadgeProps {
  status: PaymentStatus;
  className?: string;
}

export function PaymentStatusBadge({ status, className }: PaymentStatusBadgeProps) {
  return (
    <span
      className={clsx(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        getPaymentStatusColor(status),
        className
      )}
    >
      {getPaymentStatusLabel(status)}
    </span>
  );
}

interface ExtractionStatusBadgeProps {
  status: ExtractionStatus;
  className?: string;
}

export function ExtractionStatusBadge({ status, className }: ExtractionStatusBadgeProps) {
  return (
    <span
      className={clsx(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        getExtractionStatusColor(status),
        className
      )}
    >
      {getExtractionStatusLabel(status)}
    </span>
  );
}

export const SLIP_TYPES = [
  { value: 'T4', label: 'T4 - Employment Income' },
  { value: 'T4A', label: 'T4A - Pension & Other Income' },
  { value: 'T5', label: 'T5 - Investment Income' },
  { value: 'T2202', label: 'T2202 - Tuition' },
  { value: 'T3', label: 'T3 - Trust Income' },
  { value: 'T5007', label: 'T5007 - Benefits' },
  { value: 'T5008', label: 'T5008 - Securities Transactions' },
  { value: 'RRSP', label: 'RRSP Contribution Receipt' },
  { value: 'CHARITY', label: 'Charitable Donation Receipt' },
  { value: 'MEDICAL', label: 'Medical Expenses' },
  { value: 'OTHER', label: 'Other' },
] as const;

export const TAX_YEARS = Array.from({ length: 10 }, (_, i) => {
  const year = new Date().getFullYear() - i;
  return { value: year, label: year.toString() };
});

export const ORDER_STATUS_OPTIONS = [
  { value: 'OPEN', label: 'Open' },
  { value: 'SUBMITTED', label: 'Submitted' },
  { value: 'IN_REVIEW', label: 'In Review' },
  { value: 'PENDING_APPROVAL', label: 'Pending Approval' },
  { value: 'FILED', label: 'Filed' },
  { value: 'CANCELLED', label: 'Cancelled' },
] as const;

/**
 * @fileoverview TypeScript type definitions for the Payroll Reconciliation application.
 *
 * @module types
 * @author Payroll Reconciliation Team
 */

/** Represents a project configuration with billing rates and LCA settings. */
export type Project = {
  id: number;
  clientName: string;
  vendorName: string;
  projectName: string;
  employeeHourlyRate: number;
  vendorFeePercentage: number;
  eightyTwentyRate: number;
  lcaAmount: number;
  startDate: string;
  endDate?: string;
  active: boolean;
};

/**
 * Represents a monthly payroll entry.
 * Insurance is auto-populated from the insurance_rates table on save.
 */
export type MonthlyEntry = {
  id: number;
  projectId: number;
  projectName: string;
  clientName: string;
  vendorName: string;
  month: string;
  hoursWorked: number;
  employeeHourlyRate: number;
  actualEarnings: number;
  paystubAmountReceived: number;
  insuranceDeduction: number;
  monthlyBalance: number;
  notes?: string;
};

/** Payment type — determines gross-up and balance impact. */
export type PaymentType = 'DIRECT_PAYMENT' | 'FEE_DEDUCTION' | 'PERSONAL_EXPENSE';

/** How the payment was received. Required for DIRECT_PAYMENT. */
export type PaymentMode = 'ZELLE' | 'CASH' | 'CHECK' | 'ONLINE' | 'PAYSTUB';

/** Represents a payment ledger entry. */
export type DirectPayment = {
  id: number;
  paymentDate: string;
  paymentType: PaymentType;
  paymentMode?: PaymentMode;
  amountReceived: number;
  grossAmount: number;
  affectsBalance: boolean;
  notes?: string;
};

/** Represents an insurance rate for a Sep–Aug period. */
export type InsuranceRate = {
  id: number;
  effectiveFrom: string;
  effectiveTo?: string;
  monthlyAmount: number;
  notes?: string;
};

/** Aggregated dashboard summary. */
export type DashboardSummary = {
  currentBalance: number;
  totalHours: number;
  totalActualEarnings: number;
  totalPaystubAmount: number;
  totalInsuranceDeductions: number;
  totalVendorFee: number;
  totalEmployerMargin: number;
  totalDirectPaymentsGross: number;
  balanceByYear: { name: string; amount: number }[];
  balanceByProject: { name: string; amount: number }[];
  vendorFeeByYear: { name: string; amount: number }[];
  employerMarginByYear: { name: string; amount: number }[];
  marginByProject: ProjectMarginSummary[];
};

export type ProjectMarginSummary = {
  projectId: number;
  projectName: string;
  clientName: string;
  vendorName: string;
  totalHours: number;
  totalVendorFee: number;
  totalEmployerMargin: number;
  vendorFeePerHour: number;
  employerMarginPerHour: number;
  monthlyBreakdown: MonthlyMarginDetail[];
};

export type MonthlyMarginDetail = {
  month: string;
  hoursWorked: number;
  clientPays: number;
  vendorFee: number;
  employerGets: number;
  youGet: number;
  employerMargin: number;
};

/**
 * @fileoverview TypeScript type definitions for the Payroll Reconciliation application.
 *
 * Defines the shapes of data objects exchanged between the frontend and backend API.
 * These types mirror the backend DTO response records.
 *
 * @module types
 * @author Payroll Reconciliation Team
 */

/**
 * Represents a project configuration with billing rates and LCA settings.
 *
 * @property id - Unique project identifier
 * @property clientName - Name of the end client
 * @property vendorName - Name of the staffing vendor
 * @property projectName - Short name for the project/engagement
 * @property employeeHourlyRate - Base hourly rate before adjustments
 * @property vendorFeePercentage - Vendor fee percentage (e.g., 3 for 3%)
 * @property eightyTwentyRate - Effective rate after vendor fee and 80-20 split
 * @property lcaAmount - LCA (Labor Condition Application) / paystub amount
 * @property startDate - Project start date (ISO string)
 * @property endDate - Optional project end date (ISO string)
 * @property active - Whether the project is currently active
 */
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
 * Represents a monthly payroll entry with all computed balance fields.
 *
 * @property id - Unique entry identifier
 * @property projectId - Foreign key to the associated project
 * @property projectName - Denormalized project name for display
 * @property clientName - Denormalized client name for display
 * @property vendorName - Denormalized vendor name for display
 * @property month - Entry month as ISO date string (YYYY-MM-DD, first of month)
 * @property hoursWorked - Total hours worked in the month
 * @property employeeHourlyRate - Hourly rate used for this entry (80-20 rate)
 * @property actualEarnings - Computed: hoursWorked × employeeHourlyRate
 * @property paystubAmountReceived - LCA/paystub amount received from payroll
 * @property directEmployerPayment - Net amount received directly from employer
 * @property directEmployerGrossAmount - Computed: directEmployerPayment ÷ 0.80
 * @property insuranceDeduction - Insurance deduction for the month
 * @property otherAdjustment - Other adjustments (negative = fee, positive = credit)
 * @property monthlyBalance - Computed final balance for the month
 * @property notes - Optional free-text notes
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
  directEmployerPayment: number;
  directEmployerGrossAmount: number;
  insuranceDeduction: number;
  otherAdjustment: number;
  monthlyBalance: number;
  notes?: string;
};

/**
 * Aggregated dashboard summary data returned by the /api/dashboard endpoint.
 *
 * @property currentBalance - Overall cumulative balance across all years/projects
 * @property totalHours - Sum of all hours worked
 * @property totalActualEarnings - Sum of all actual earnings
 * @property totalPaystubAmount - Sum of all paystub amounts received
 * @property totalEmployerPayments - Sum of all direct employer payments
 * @property totalInsuranceDeductions - Sum of all insurance deductions
 * @property balanceByYear - Balance breakdown grouped by year
 * @property balanceByProject - Balance breakdown grouped by project
 */
export type DashboardSummary = {
  currentBalance: number;
  totalHours: number;
  totalActualEarnings: number;
  totalPaystubAmount: number;
  totalEmployerPayments: number;
  totalInsuranceDeductions: number;
  totalVendorFee: number;
  totalEmployerMargin: number;
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

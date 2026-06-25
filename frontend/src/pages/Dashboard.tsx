/**
 * @fileoverview Dashboard page displaying payroll balance summary and charts.
 *
 * Shows aggregated metrics (current balance, total hours, earnings, etc.)
 * and bar charts for balance by year and balance by project.
 * Data is fetched from the /api/dashboard endpoint.
 *
 * @module pages/Dashboard
 * @author Payroll Reconciliation Team
 */

import RefreshIcon from '@mui/icons-material/Refresh';
import { Box, Button, Grid, Paper, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import api from '../api/client';
import { DashboardSummary } from '../types';

/**
 * Formats a numeric value as US dollar currency.
 *
 * @param value - The numeric amount to format
 * @returns Formatted currency string (e.g., "$1,234.56")
 */
const money = (value: number) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0);

/**
 * Metric card component displaying a label and formatted value.
 *
 * @param props.label - The metric description text
 * @param props.value - The formatted metric value to display
 * @returns A Paper card with label and value
 */
function Metric({ label, value }: { label: string; value: string }) {
  return <Paper sx={{ p: 2, height: '100%' }}><Typography color="text.secondary" variant="body2">{label}</Typography><Typography variant="h5">{value}</Typography></Paper>;
}

/**
 * Dashboard page component.
 *
 * Fetches summary data on mount and displays:
 * - Key metric cards (balance, hours, earnings, paystub, payments, deductions)
 * - Balance By Year bar chart
 * - Balance By Project bar chart
 * - Refresh button to reload data
 *
 * @returns The dashboard page UI with metrics and charts
 */
export default function Dashboard() {
  const [data, setData] = useState<DashboardSummary | null>(null);

  /** Fetches dashboard summary data from the API. */
  const load = async () => setData((await api.get('/dashboard')).data);

  useEffect(() => { load(); }, []);

  return (
    <Stack spacing={3}>
      <Stack direction="row" alignItems="center" justifyContent="space-between">
        <Box><Typography variant="h4">Dashboard</Typography><Typography color="text.secondary">Current payroll balance and workbook-style totals</Typography></Box>
        <Button startIcon={<RefreshIcon />} onClick={load}>Refresh</Button>
      </Stack>
      <Grid container spacing={2}>
        <Grid item xs={12} md={3}><Metric label="Current Balance" value={money(data?.currentBalance ?? 0)} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Total Hours" value={(data?.totalHours ?? 0).toLocaleString()} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Actual Earnings" value={money(data?.totalActualEarnings ?? 0)} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Paystub Amount" value={money(data?.totalPaystubAmount ?? 0)} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Employer Payments" value={money(data?.totalEmployerPayments ?? 0)} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Insurance Deductions" value={money(data?.totalInsuranceDeductions ?? 0)} /></Grid>
      </Grid>
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 360 }}><Typography variant="h6">Balance By Year</Typography><ResponsiveContainer width="100%" height="88%"><BarChart data={data?.balanceByYear ?? []}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="name" /><YAxis /><Tooltip formatter={(v) => money(Number(v))} /><Bar dataKey="amount" fill="#2457a6" /></BarChart></ResponsiveContainer></Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 360 }}><Typography variant="h6">Balance By Project</Typography><ResponsiveContainer width="100%" height="88%"><BarChart data={data?.balanceByProject ?? []}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="name" /><YAxis /><Tooltip formatter={(v) => money(Number(v))} /><Bar dataKey="amount" fill="#0f8b8d" /></BarChart></ResponsiveContainer></Paper>
        </Grid>
      </Grid>
    </Stack>
  );
}

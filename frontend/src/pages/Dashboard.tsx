/**
 * @fileoverview Dashboard page displaying payroll balance summary,
 * vendor fee analytics, and employer margin charts.
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

const money = (value: number) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0);

function Metric({ label, value, color }: { label: string; value: string; color?: string }) {
  return (
    <Paper sx={{ p: 2, height: '100%' }}>
      <Typography color="text.secondary" variant="body2">{label}</Typography>
      <Typography variant="h5" sx={{ color: color || 'inherit' }}>{value}</Typography>
    </Paper>
  );
}

export default function Dashboard() {
  const [data, setData] = useState<DashboardSummary | null>(null);

  const load = async () => setData((await api.get('/dashboard')).data);
  useEffect(() => { load(); }, []);

  return (
    <Stack spacing={3}>
      <Stack direction="row" alignItems="center" justifyContent="space-between">
        <Box>
          <Typography variant="h4">Dashboard</Typography>
          <Typography color="text.secondary">Payroll balance, vendor fees, and employer margin analytics</Typography>
        </Box>
        <Button startIcon={<RefreshIcon />} onClick={load}>Refresh</Button>
      </Stack>

      {/* Balance Metrics */}
      <Typography variant="h6" sx={{ mt: 1 }}>Balance Overview</Typography>
      <Grid container spacing={2}>
        <Grid item xs={12} md={3}><Metric label="Current Balance" value={money(data?.currentBalance ?? 0)} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Total Hours" value={(data?.totalHours ?? 0).toLocaleString()} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Actual Earnings" value={money(data?.totalActualEarnings ?? 0)} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Paystub Amount" value={money(data?.totalPaystubAmount ?? 0)} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Employer Payments" value={money(data?.totalEmployerPayments ?? 0)} /></Grid>
        <Grid item xs={12} md={3}><Metric label="Insurance Deductions" value={money(data?.totalInsuranceDeductions ?? 0)} /></Grid>
      </Grid>

      {/* Vendor Fee & Employer Margin Metrics */}
      <Typography variant="h6" sx={{ mt: 1 }}>Who's Taking What</Typography>
      <Grid container spacing={2}>
        <Grid item xs={12} md={3}>
          <Metric label="Total Vendor Fee (All Time)" value={money(data?.totalVendorFee ?? 0)} color="#d32f2f" />
        </Grid>
        <Grid item xs={12} md={3}>
          <Metric label="Total Employer Margin (All Time)" value={money(data?.totalEmployerMargin ?? 0)} color="#ed6c02" />
        </Grid>
      </Grid>

      {/* Balance Charts */}
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 360 }}>
            <Typography variant="h6">Balance By Year</Typography>
            <ResponsiveContainer width="100%" height="88%">
              <BarChart data={data?.balanceByYear ?? []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip formatter={(v) => money(Number(v))} />
                <Bar dataKey="amount" fill="#2457a6" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 360 }}>
            <Typography variant="h6">Balance By Project</Typography>
            <ResponsiveContainer width="100%" height="88%">
              <BarChart data={data?.balanceByProject ?? []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip formatter={(v) => money(Number(v))} />
                <Bar dataKey="amount" fill="#0f8b8d" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>

      {/* Vendor Fee & Employer Margin Charts */}
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 360 }}>
            <Typography variant="h6">Vendor Fee By Year</Typography>
            <ResponsiveContainer width="100%" height="88%">
              <BarChart data={data?.vendorFeeByYear ?? []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip formatter={(v) => money(Number(v))} />
                <Bar dataKey="amount" fill="#d32f2f" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 360 }}>
            <Typography variant="h6">Employer Margin By Year</Typography>
            <ResponsiveContainer width="100%" height="88%">
              <BarChart data={data?.employerMarginByYear ?? []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip formatter={(v) => money(Number(v))} />
                <Bar dataKey="amount" fill="#ed6c02" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>

      {/* Per-Project Margin Table */}
      <Typography variant="h6">Margin By Project</Typography>
      <Grid container spacing={2}>
        {(data?.marginByProject ?? []).map((p) => (
          <Grid item xs={12} md={6} key={p.projectId}>
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6">{p.projectName}</Typography>
              <Typography color="text.secondary" variant="body2">{p.clientName} / {p.vendorName}</Typography>
              <Grid container spacing={1} sx={{ mt: 1 }}>
                <Grid item xs={6}><Typography variant="body2" color="text.secondary">Total Hours</Typography><Typography>{p.totalHours.toLocaleString()}</Typography></Grid>
                <Grid item xs={6}><Typography variant="body2" color="text.secondary">Vendor Fee (Total)</Typography><Typography color="error">{money(p.totalVendorFee)}</Typography></Grid>
                <Grid item xs={6}><Typography variant="body2" color="text.secondary">Employer Margin (Total)</Typography><Typography sx={{ color: '#ed6c02' }}>{money(p.totalEmployerMargin)}</Typography></Grid>
                <Grid item xs={6}><Typography variant="body2" color="text.secondary">Margin/Hour</Typography><Typography sx={{ color: '#ed6c02' }}>{money(p.employerMarginPerHour)}/hr</Typography></Grid>
              </Grid>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Stack>
  );
}

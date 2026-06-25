/**
 * @fileoverview Reports page for generating and downloading payroll reports.
 *
 * Provides controls to generate and download:
 * - Monthly reports (by year and month)
 * - Quarterly reports (by year and quarter)
 * - Yearly reports (by year)
 * - Employee balance PDF
 *
 * Also allows triggering yearly balance recalculation for carry-forward updates.
 * Reports can be exported in Excel (.xlsx) or PDF format.
 *
 * @module pages/Reports
 * @author Payroll Reconciliation Team
 */

import DownloadIcon from '@mui/icons-material/Download';
import SyncIcon from '@mui/icons-material/Sync';
import { Button, Grid, MenuItem, Paper, Stack, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import api from '../api/client';

/**
 * Reports page component.
 *
 * Renders filter controls (year, month, quarter, format) and action buttons
 * for downloading various report types. Downloads are triggered via blob
 * responses from the API and presented as file downloads to the user.
 *
 * @returns The reports page UI with download controls
 */
export default function Reports() {
  const currentYear = new Date().getFullYear();
  const [year, setYear] = useState(currentYear);
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [quarter, setQuarter] = useState(1);
  const [format, setFormat] = useState('xlsx');

  /**
   * Downloads a file from the API by fetching it as a blob and triggering
   * a browser download via a temporary anchor element.
   *
   * @param path - The API path to fetch (e.g., "/reports/monthly?year=2024&month=1&format=xlsx")
   * @param fileName - The filename for the downloaded file
   */
  const download = async (path: string, fileName: string) => {
    const response = await api.get(path, { responseType: 'blob' });
    const url = URL.createObjectURL(response.data);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  };

  /**
   * Triggers recalculation of yearly carry-forward balances via the API.
   * Shows an alert on completion.
   */
  const recalc = async () => {
    await api.post('/yearly-balances/recalculate');
    alert('Yearly balances recalculated.');
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h4">Reports</Typography>
      <Paper sx={{ p: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={2}><TextField label="Year" type="number" value={year} onChange={(e) => setYear(Number(e.target.value))} fullWidth /></Grid>
          <Grid item xs={12} md={2}><TextField label="Month" type="number" value={month} onChange={(e) => setMonth(Number(e.target.value))} fullWidth /></Grid>
          <Grid item xs={12} md={2}><TextField label="Quarter" select value={quarter} onChange={(e) => setQuarter(Number(e.target.value))} fullWidth>{[1, 2, 3, 4].map((q) => <MenuItem key={q} value={q}>Q{q}</MenuItem>)}</TextField></Grid>
          <Grid item xs={12} md={2}><TextField label="Format" select value={format} onChange={(e) => setFormat(e.target.value)} fullWidth><MenuItem value="xlsx">Excel</MenuItem><MenuItem value="pdf">PDF</MenuItem></TextField></Grid>
          <Grid item xs={12}><Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            <Button variant="contained" startIcon={<DownloadIcon />} onClick={() => download(`/reports/monthly?year=${year}&month=${month}&format=${format}`, `monthly-${year}-${month}.${format}`)}>Monthly</Button>
            <Button variant="contained" startIcon={<DownloadIcon />} onClick={() => download(`/reports/quarterly?year=${year}&quarter=${quarter}&format=${format}`, `quarterly-${year}-q${quarter}.${format}`)}>Quarterly</Button>
            <Button variant="contained" startIcon={<DownloadIcon />} onClick={() => download(`/reports/yearly?year=${year}&format=${format}`, `yearly-${year}.${format}`)}>Yearly</Button>
            <Button variant="outlined" startIcon={<DownloadIcon />} onClick={() => download(`/reports/employee-balance?year=${year}&format=pdf`, `employee-balance-${year}.pdf`)}>Employee Balance PDF</Button>
            <Button variant="outlined" startIcon={<SyncIcon />} onClick={recalc}>Recalculate Carry Forward</Button>
          </Stack></Grid>
        </Grid>
      </Paper>
    </Stack>
  );
}

/**
 * @fileoverview Insurance Rates page for managing insurance deduction rates.
 *
 * Insurance runs on a Sep–Aug cycle. Enter one rate per period.
 * When a monthly entry is saved, the system automatically looks up the
 * active rate for that month and applies it as the insurance deduction.
 *
 * Use the "Recalculate All" button to re-apply rates to all existing entries.
 *
 * @module pages/Insurance
 * @author Payroll Reconciliation Team
 */

import AddIcon from '@mui/icons-material/Add';
import CancelIcon from '@mui/icons-material/Cancel';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import SyncIcon from '@mui/icons-material/Sync';
import {
    Alert,
    Button,
    Chip,
    Grid,
    IconButton,
    Paper,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    TextField,
    Tooltip,
    Typography
} from '@mui/material';
import { FormEvent, useEffect, useState } from 'react';
import api from '../api/client';
import { InsuranceRate } from '../types';

const money = (value: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0);

const emptyForm = () => ({
    effectiveFrom: '',
    effectiveTo: '',
    monthlyAmount: '',
    notes: ''
});

/**
 * Insurance Rates page component.
 *
 * @returns The insurance rates management page UI
 */
export default function Insurance() {
    const [rates, setRates]           = useState<InsuranceRate[]>([]);
    const [form, setForm]             = useState(emptyForm());
    const [editingId, setEditingId]   = useState<number | null>(null);
    const [recalcMsg, setRecalcMsg]   = useState<string | null>(null);

    const load = async () => {
        const res = await api.get('/insurance-rates');
        setRates(res.data);
    };

    useEffect(() => { load(); }, []);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        const payload = {
            effectiveFrom:  form.effectiveFrom ? `${form.effectiveFrom}-01` : null,
            effectiveTo:    form.effectiveTo   ? `${form.effectiveTo}-01`   : null,
            monthlyAmount:  Number(form.monthlyAmount),
            notes:          form.notes || null
        };
        if (editingId !== null) {
            await api.put(`/insurance-rates/${editingId}`, payload);
        } else {
            await api.post('/insurance-rates', payload);
        }
        setForm(emptyForm());
        setEditingId(null);
        load();
    };

    const startEdit = (rate: InsuranceRate) => {
        setEditingId(rate.id);
        setForm({
            effectiveFrom: rate.effectiveFrom.substring(0, 7),
            effectiveTo:   rate.effectiveTo ? rate.effectiveTo.substring(0, 7) : '',
            monthlyAmount: String(rate.monthlyAmount),
            notes:         rate.notes ?? ''
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const cancelEdit = () => { setEditingId(null); setForm(emptyForm()); };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Delete this insurance rate?')) return;
        await api.delete(`/insurance-rates/${id}`);
        load();
    };

    const recalculate = async () => {
        const res = await api.post('/insurance-rates/recalculate');
        setRecalcMsg(`${res.data.message} (${res.data.entriesUpdated} entries updated)`);
        setTimeout(() => setRecalcMsg(null), 5000);
        load();
    };

    return (
        <Stack spacing={3}>
            <Stack direction="row" justifyContent="space-between" alignItems="center">
                <Typography variant="h4">Insurance Rates</Typography>
                <Tooltip title="Re-apply all insurance rates to existing monthly entries">
                    <Button variant="outlined" startIcon={<SyncIcon />} onClick={recalculate}>
                        Recalculate All Entries
                    </Button>
                </Tooltip>
            </Stack>

            <Typography variant="body2" color="text.secondary">
                Insurance runs Sep–Aug each year. Enter one rate per period.
                When you save a monthly entry, the system automatically applies the matching rate.
                When you add a new rate, the previous open rate is automatically closed.
            </Typography>

            {recalcMsg && <Alert severity="success">{recalcMsg}</Alert>}

            {/* FORM */}
            <Paper component="form" onSubmit={handleSubmit}
                sx={{ p: 2, border: editingId !== null ? '2px solid #1976d2' : undefined }}>
                <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                    {editingId !== null ? 'Edit Rate' : 'Add Rate'}
                </Typography>
                <Grid container spacing={2} alignItems="flex-start">
                    <Grid item xs={12} md={2}>
                        <TextField
                            label="Effective From"
                            type="month"
                            InputLabelProps={{ shrink: true }}
                            value={form.effectiveFrom}
                            onChange={e => setForm({ ...form, effectiveFrom: e.target.value })}
                            fullWidth required
                            helperText="e.g. Sep-2023"
                        />
                    </Grid>
                    <Grid item xs={12} md={2}>
                        <TextField
                            label="Effective To"
                            type="month"
                            InputLabelProps={{ shrink: true }}
                            value={form.effectiveTo}
                            onChange={e => setForm({ ...form, effectiveTo: e.target.value })}
                            fullWidth
                            helperText="Leave blank if still active"
                        />
                    </Grid>
                    <Grid item xs={12} md={2}>
                        <TextField
                            label="Monthly Amount"
                            type="number"
                            inputProps={{ step: '0.01', min: '0' }}
                            value={form.monthlyAmount}
                            onChange={e => setForm({ ...form, monthlyAmount: e.target.value })}
                            fullWidth required
                        />
                    </Grid>
                    <Grid item xs={12} md={3}>
                        <TextField
                            label="Notes"
                            value={form.notes}
                            onChange={e => setForm({ ...form, notes: e.target.value })}
                            fullWidth
                            placeholder="e.g. Health only, Health + Dental"
                        />
                    </Grid>
                    <Grid item xs={12} md={3}>
                        <Stack direction="row" spacing={2} sx={{ pt: 1 }}>
                            <Button type="submit" variant="contained"
                                startIcon={editingId !== null ? <SaveIcon /> : <AddIcon />}>
                                {editingId !== null ? 'Update Rate' : 'Add Rate'}
                            </Button>
                            {editingId !== null && (
                                <Button variant="outlined" startIcon={<CancelIcon />} onClick={cancelEdit}>
                                    Cancel
                                </Button>
                            )}
                        </Stack>
                    </Grid>
                </Grid>
            </Paper>

            {/* TABLE */}
            <Paper sx={{ p: 2 }}>
                <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                    Rate History ({rates.length} rates)
                </Typography>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>Effective From</TableCell>
                            <TableCell>Effective To</TableCell>
                            <TableCell align="right">Monthly Amount</TableCell>
                            <TableCell>Notes</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {rates.map(r => (
                            <TableRow key={r.id} selected={editingId === r.id}>
                                <TableCell>{r.effectiveFrom.substring(0, 7)}</TableCell>
                                <TableCell>{r.effectiveTo ? r.effectiveTo.substring(0, 7) : '—'}</TableCell>
                                <TableCell align="right">{money(r.monthlyAmount)}</TableCell>
                                <TableCell>{r.notes ?? '—'}</TableCell>
                                <TableCell>
                                    {!r.effectiveTo
                                        ? <Chip label="Active" color="success" size="small" />
                                        : <Chip label="Closed" size="small" />
                                    }
                                </TableCell>
                                <TableCell align="center">
                                    <Tooltip title="Edit">
                                        <IconButton size="small" onClick={() => startEdit(r)}>
                                            <EditIcon fontSize="small" />
                                        </IconButton>
                                    </Tooltip>
                                    <Tooltip title="Delete">
                                        <IconButton size="small" color="error" onClick={() => handleDelete(r.id)}>
                                            <DeleteIcon fontSize="small" />
                                        </IconButton>
                                    </Tooltip>
                                </TableCell>
                            </TableRow>
                        ))}
                        {rates.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={6} align="center" sx={{ color: 'text.secondary', py: 4 }}>
                                    No insurance rates configured yet.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </Paper>
        </Stack>
    );
}

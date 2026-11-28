/**
 * @fileoverview Payments ledger page for recording all deductions from the total balance.
 *
 * Payment types:
 * - DIRECT_PAYMENT  — cash/check from employer (÷ 0.80 gross-up). Payment mode required.
 * - FEE_DEDUCTION   — perm fee, legal fee (deducted as-is)
 * - PERSONAL_EXPENSE — perm/Green Card fees paid personally (tracked only, no balance impact)
 *
 * @module pages/Payments
 * @author Payroll Reconciliation Team
 */

import AddIcon from '@mui/icons-material/Add';
import CancelIcon from '@mui/icons-material/Cancel';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import RemoveCircleIcon from '@mui/icons-material/RemoveCircle';
import SaveIcon from '@mui/icons-material/Save';
import {
    Button, Chip, Divider, Grid, IconButton, MenuItem,
    Paper, Stack, Table, TableBody, TableCell, TableHead,
    TableRow, TextField, Tooltip, Typography
} from '@mui/material';
import { FormEvent, useEffect, useState } from 'react';
import api from '../api/client';
import { DirectPayment, PaymentMode, PaymentType } from '../types';

const money = (v: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(v ?? 0);

const PAYMENT_TYPE_LABELS: Record<PaymentType, string> = {
    DIRECT_PAYMENT:   'Direct Payment (Cash/Check)',
    FEE_DEDUCTION:    'Fee Deduction (Perm/Legal)',
    PERSONAL_EXPENSE: 'Personal Expense (Perm/GC)'
};

const PAYMENT_TYPE_COLORS: Record<PaymentType, 'primary' | 'warning' | 'secondary'> = {
    DIRECT_PAYMENT:   'primary',
    FEE_DEDUCTION:    'warning',
    PERSONAL_EXPENSE: 'secondary'
};

const PAYMENT_MODE_LABELS: Record<PaymentMode, string> = {
    ZELLE:   'Zelle',
    CASH:    'Cash',
    CHECK:   'Check',
    ONLINE:  'Online Transfer',
    PAYSTUB: 'Paystub'
};

type PaymentRow = {
    paymentDate:    string;
    paymentType:    PaymentType;
    paymentMode:    PaymentMode | '';
    amountReceived: string;
    notes:          string;
};

const emptyRow = (date = ''): PaymentRow => ({
    paymentDate:    date,
    paymentType:    'DIRECT_PAYMENT',
    paymentMode:    'ZELLE',
    amountReceived: '',
    notes:          ''
});

const calcGross = (row: PaymentRow): string => {
    const amt = Number(row.amountReceived);
    if (!amt) return '$0.00';
    return money(row.paymentType === 'DIRECT_PAYMENT' ? amt / 0.80 : amt);
};

export default function Payments() {
    const [payments, setPayments]   = useState<DirectPayment[]>([]);
    const [rows, setRows]           = useState<PaymentRow[]>([emptyRow()]);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [editForm, setEditForm]   = useState<PaymentRow>(emptyRow());

    const load = async () => {
        const res = await api.get('/direct-payments');
        setPayments(res.data);
    };

    useEffect(() => { load(); }, []);

    // Totals — PERSONAL_EXPENSE excluded from deducted total
    const totalDeducted   = payments.filter(p => p.affectsBalance).reduce((s, p) => s + p.grossAmount, 0);
    const totalDirect     = payments.filter(p => p.paymentType === 'DIRECT_PAYMENT').reduce((s, p) => s + p.grossAmount, 0);
    const totalFees       = payments.filter(p => p.paymentType === 'FEE_DEDUCTION').reduce((s, p) => s + p.grossAmount, 0);
    const totalPersonal   = payments.filter(p => p.paymentType === 'PERSONAL_EXPENSE').reduce((s, p) => s + p.amountReceived, 0);

    // Multi-row add
    const updateRow = (idx: number, field: keyof PaymentRow, value: string) => {
        setRows(prev => {
            const updated = [...prev];
            updated[idx] = { ...updated[idx], [field]: value };
            // Auto-clear paymentMode when switching to FEE_DEDUCTION
            if (field === 'paymentType' && value === 'FEE_DEDUCTION') {
                updated[idx].paymentMode = '';
            }
            if (field === 'paymentType' && (value === 'DIRECT_PAYMENT' || value === 'PERSONAL_EXPENSE')) {
                updated[idx].paymentMode = 'ZELLE';
            }
            return updated;
        });
    };

    const addRow = () => {
        const lastDate = rows.length > 0 ? rows[rows.length - 1].paymentDate : '';
        setRows(prev => [...prev, emptyRow(lastDate)]);
    };

    const removeRow = (idx: number) => {
        if (rows.length <= 1) return;
        setRows(prev => prev.filter((_, i) => i !== idx));
    };

    const submitAll = async (e: FormEvent) => {
        e.preventDefault();
        const valid = rows.filter(r => r.paymentDate && r.amountReceived && Number(r.amountReceived) > 0);
        if (valid.length === 0) return;
        await api.post('/direct-payments/batch', {
            payments: valid.map(r => ({
                paymentDate:    r.paymentDate,
                paymentType:    r.paymentType,
                paymentMode:    r.paymentMode || null,
                amountReceived: Number(r.amountReceived),
                notes:          r.notes || null
            }))
        });
        setRows([emptyRow()]);
        load();
    };

    // Edit single
    const startEdit = (p: DirectPayment) => {
        setEditingId(p.id);
        setEditForm({
            paymentDate:    p.paymentDate,
            paymentType:    p.paymentType,
            paymentMode:    p.paymentMode ?? '',
            amountReceived: String(p.amountReceived),
            notes:          p.notes ?? ''
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const cancelEdit = () => { setEditingId(null); setEditForm(emptyRow()); };

    const submitEdit = async (e: FormEvent) => {
        e.preventDefault();
        await api.put(`/direct-payments/${editingId}`, {
            paymentDate:    editForm.paymentDate,
            paymentType:    editForm.paymentType,
            paymentMode:    editForm.paymentMode || null,
            amountReceived: Number(editForm.amountReceived),
            notes:          editForm.notes || null
        });
        setEditingId(null);
        setEditForm(emptyRow());
        load();
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Delete this entry?')) return;
        await api.delete(`/direct-payments/${id}`);
        load();
    };

    const renderModeField = (row: PaymentRow, onChange: (val: string) => void) =>
        row.paymentType !== 'FEE_DEDUCTION' ? (
            <TextField select label="Payment Mode" value={row.paymentMode} onChange={e => onChange(e.target.value)} fullWidth required size="small">
                {(Object.keys(PAYMENT_MODE_LABELS) as PaymentMode[]).map(m => (
                    <MenuItem key={m} value={m}>{PAYMENT_MODE_LABELS[m]}</MenuItem>
                ))}
            </TextField>
        ) : null;

    return (
        <Stack spacing={3}>
            <Typography variant="h4">Payments Ledger</Typography>
            <Typography variant="body2" color="text.secondary">
                Direct Payments use 80-20 gross-up (÷ 0.80). Fees deducted as-is.
                Personal expenses are tracked only and do not affect your balance.
            </Typography>

            {/* EDIT FORM */}
            {editingId !== null && (
                <Paper component="form" onSubmit={submitEdit} sx={{ p: 2, border: '2px solid #1976d2' }}>
                    <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>Edit Entry</Typography>
                    <Grid container spacing={2} alignItems="flex-start">
                        <Grid item xs={12} md={2}>
                            <TextField label="Date" type="date" InputLabelProps={{ shrink: true }}
                                value={editForm.paymentDate}
                                onChange={e => setEditForm({ ...editForm, paymentDate: e.target.value })}
                                fullWidth required />
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <TextField select label="Type" value={editForm.paymentType}
                                onChange={e => {
                                    const t = e.target.value as PaymentType;
                                    setEditForm({ ...editForm, paymentType: t, paymentMode: t === 'FEE_DEDUCTION' ? '' : 'ZELLE' });
                                }} fullWidth required>
                                {(Object.keys(PAYMENT_TYPE_LABELS) as PaymentType[]).map(t => (
                                    <MenuItem key={t} value={t}>{PAYMENT_TYPE_LABELS[t]}</MenuItem>
                                ))}
                            </TextField>
                        </Grid>
                        {editForm.paymentType !== 'FEE_DEDUCTION' && (
                            <Grid item xs={12} md={2}>
                                {renderModeField(editForm, v => setEditForm({ ...editForm, paymentMode: v as PaymentMode }))}
                            </Grid>
                        )}
                        <Grid item xs={12} md={2}>
                            <TextField label="Amount" type="number" inputProps={{ step: '0.01', min: '0' }}
                                value={editForm.amountReceived}
                                onChange={e => setEditForm({ ...editForm, amountReceived: e.target.value })}
                                fullWidth required helperText={`Gross: ${calcGross(editForm)}`} />
                        </Grid>
                        <Grid item xs={12} md={3}>
                            <TextField label="Notes" value={editForm.notes}
                                onChange={e => setEditForm({ ...editForm, notes: e.target.value })}
                                fullWidth />
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <Stack direction="row" spacing={1} sx={{ pt: 1 }}>
                                <Button type="submit" variant="contained" startIcon={<SaveIcon />}>Update</Button>
                                <Button variant="outlined" startIcon={<CancelIcon />} onClick={cancelEdit}>Cancel</Button>
                            </Stack>
                        </Grid>
                    </Grid>
                </Paper>
            )}

            {/* MULTI-ROW ADD */}
            {editingId === null && (
                <Paper component="form" onSubmit={submitAll} sx={{ p: 2 }}>
                    <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 600 }}>Add Entries</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        Add multiple entries at once. Gross deduction calculated per row.
                    </Typography>
                    {rows.map((row, idx) => (
                        <div key={idx}>
                            {idx > 0 && <Divider sx={{ my: 1.5 }} />}
                            <Grid container spacing={1.5} alignItems="flex-start">
                                <Grid item xs={6} md={1.8}>
                                    <TextField label="Date" type="date" InputLabelProps={{ shrink: true }}
                                        value={row.paymentDate} size="small"
                                        onChange={e => updateRow(idx, 'paymentDate', e.target.value)}
                                        fullWidth required />
                                </Grid>
                                <Grid item xs={6} md={2}>
                                    <TextField select label="Type" value={row.paymentType} size="small"
                                        onChange={e => updateRow(idx, 'paymentType', e.target.value)}
                                        fullWidth required>
                                        {(Object.keys(PAYMENT_TYPE_LABELS) as PaymentType[]).map(t => (
                                            <MenuItem key={t} value={t}>{PAYMENT_TYPE_LABELS[t]}</MenuItem>
                                        ))}
                                    </TextField>
                                </Grid>
                                {row.paymentType !== 'FEE_DEDUCTION' && (
                                    <Grid item xs={6} md={1.8}>
                                        <TextField select label="Mode" value={row.paymentMode} size="small"
                                            onChange={e => updateRow(idx, 'paymentMode', e.target.value)}
                                            fullWidth required>
                                            {(Object.keys(PAYMENT_MODE_LABELS) as PaymentMode[]).map(m => (
                                                <MenuItem key={m} value={m}>{PAYMENT_MODE_LABELS[m]}</MenuItem>
                                            ))}
                                        </TextField>
                                    </Grid>
                                )}
                                <Grid item xs={6} md={1.8}>
                                    <TextField label="Amount" type="number" inputProps={{ step: '0.01', min: '0' }}
                                        value={row.amountReceived} size="small"
                                        onChange={e => updateRow(idx, 'amountReceived', e.target.value)}
                                        fullWidth required helperText={`Gross: ${calcGross(row)}`} />
                                </Grid>
                                <Grid item xs={6} md={2.5}>
                                    <TextField label="Notes" value={row.notes} size="small"
                                        onChange={e => updateRow(idx, 'notes', e.target.value)}
                                        fullWidth />
                                </Grid>
                                <Grid item xs={2} md={0.5}>
                                    {rows.length > 1 && (
                                        <Tooltip title="Remove row">
                                            <IconButton size="small" color="error" onClick={() => removeRow(idx)}>
                                                <RemoveCircleIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    )}
                                </Grid>
                            </Grid>
                        </div>
                    ))}
                    <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
                        <Button variant="outlined" startIcon={<AddIcon />} onClick={addRow}>Add Row</Button>
                        <Button type="submit" variant="contained" startIcon={<SaveIcon />}>Save All</Button>
                    </Stack>
                </Paper>
            )}

            {/* SUMMARY */}
            <Paper sx={{ p: 2 }}>
                <Typography variant="subtitle1" sx={{ mb: 1.5, fontWeight: 600 }}>Summary</Typography>
                <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
                    <Stack direction="row" spacing={1} alignItems="center">
                        <Typography variant="body2" color="text.secondary">Total Deducted from Balance:</Typography>
                        <Chip label={money(totalDeducted)} color="error" sx={{ fontWeight: 700 }} />
                    </Stack>
                    <Stack direction="row" spacing={1} alignItems="center">
                        <Typography variant="body2" color="text.secondary">Direct Payments:</Typography>
                        <Chip label={money(totalDirect)} color="primary" />
                    </Stack>
                    <Stack direction="row" spacing={1} alignItems="center">
                        <Typography variant="body2" color="text.secondary">Fees:</Typography>
                        <Chip label={money(totalFees)} color="warning" />
                    </Stack>
                    <Stack direction="row" spacing={1} alignItems="center">
                        <Typography variant="body2" color="text.secondary">Personal Expenses (not deducted):</Typography>
                        <Chip label={money(totalPersonal)} color="default" />
                    </Stack>
                </Stack>
            </Paper>

            {/* TABLE */}
            <Paper sx={{ p: 2, overflow: 'auto' }}>
                <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                    History ({payments.length} entries)
                </Typography>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>Date</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Mode</TableCell>
                            <TableCell align="right">Amount</TableCell>
                            <TableCell align="right">Gross Deducted</TableCell>
                            <TableCell>Notes</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {payments.map(p => (
                            <TableRow key={p.id} selected={editingId === p.id}
                                sx={!p.affectsBalance ? { opacity: 0.7 } : {}}>
                                <TableCell>{p.paymentDate}</TableCell>
                                <TableCell>
                                    <Chip label={PAYMENT_TYPE_LABELS[p.paymentType]}
                                        color={PAYMENT_TYPE_COLORS[p.paymentType]} size="small" />
                                </TableCell>
                                <TableCell>{p.paymentMode ? PAYMENT_MODE_LABELS[p.paymentMode] : '—'}</TableCell>
                                <TableCell align="right">{money(p.amountReceived)}</TableCell>
                                <TableCell align="right"
                                    sx={{ color: p.affectsBalance ? 'error.main' : 'text.secondary', fontWeight: p.affectsBalance ? 600 : 400 }}>
                                    {p.affectsBalance ? money(p.grossAmount) : '—'}
                                </TableCell>
                                <TableCell>{p.notes ?? '—'}</TableCell>
                                <TableCell align="center">
                                    <Tooltip title="Edit">
                                        <IconButton size="small" onClick={() => startEdit(p)}>
                                            <EditIcon fontSize="small" />
                                        </IconButton>
                                    </Tooltip>
                                    <Tooltip title="Delete">
                                        <IconButton size="small" color="error" onClick={() => handleDelete(p.id)}>
                                            <DeleteIcon fontSize="small" />
                                        </IconButton>
                                    </Tooltip>
                                </TableCell>
                            </TableRow>
                        ))}
                        {payments.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={7} align="center" sx={{ color: 'text.secondary', py: 4 }}>
                                    No entries recorded yet.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </Paper>
        </Stack>
    );
}

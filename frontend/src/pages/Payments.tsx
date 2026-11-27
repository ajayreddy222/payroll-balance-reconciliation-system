/**
 * @fileoverview Direct Payments page for recording cash/check payments received from the employer.
 *
 * These payments are independent of any specific project or monthly entry.
 * Enter the net amount received in hand — the system automatically calculates
 * the gross amount (amountReceived ÷ 0.80) which is deducted from the total balance.
 *
 * @module pages/Payments
 * @author Payroll Reconciliation Team
 */

import AddIcon from '@mui/icons-material/Add';
import CancelIcon from '@mui/icons-material/Cancel';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import {
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
import { DirectPayment } from '../types';

/**
 * Formats a number as USD currency.
 *
 * @param value - The numeric amount to format
 * @returns Formatted currency string (e.g., "$12,500.00")
 */
const money = (value: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0);

/** Empty form state for a new payment. */
const emptyForm = () => ({
    paymentDate: '',
    amountReceived: '',
    notes: ''
});

/**
 * Direct Payments page component.
 *
 * Allows recording, editing, and deleting direct cash/check payments
 * received from the employer. The gross amount (÷ 0.80) is calculated
 * automatically and shown in the table.
 *
 * @returns The direct payments management page UI
 */
export default function Payments() {
    const [payments, setPayments] = useState<DirectPayment[]>([]);
    const [form, setForm] = useState(emptyForm());
    const [editingId, setEditingId] = useState<number | null>(null);
    const [totalGross, setTotalGross] = useState(0);

    /**
     * Loads all direct payments from the API and computes the total gross amount.
     */
    const load = async () => {
        const res = await api.get('/direct-payments');
        const data: DirectPayment[] = res.data;
        setPayments(data);
        setTotalGross(data.reduce((sum, p) => sum + p.grossAmount, 0));
    };

    useEffect(() => { load(); }, []);

    /**
     * Handles form submission for both create and update operations.
     *
     * @param e - The form submission event
     */
    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        const payload = {
            paymentDate: form.paymentDate,
            amountReceived: Number(form.amountReceived),
            notes: form.notes || null
        };
        if (editingId !== null) {
            await api.put(`/direct-payments/${editingId}`, payload);
        } else {
            await api.post('/direct-payments', payload);
        }
        setForm(emptyForm());
        setEditingId(null);
        load();
    };

    /**
     * Populates the form with an existing payment's data for editing.
     *
     * @param payment - The payment to edit
     */
    const startEdit = (payment: DirectPayment) => {
        setEditingId(payment.id);
        setForm({
            paymentDate: payment.paymentDate,
            amountReceived: String(payment.amountReceived),
            notes: payment.notes ?? ''
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    /** Cancels the current edit and resets the form. */
    const cancelEdit = () => {
        setEditingId(null);
        setForm(emptyForm());
    };

    /**
     * Deletes a direct payment after confirmation.
     *
     * @param id - The payment ID to delete
     */
    const handleDelete = async (id: number) => {
        if (!window.confirm('Delete this payment?')) return;
        await api.delete(`/direct-payments/${id}`);
        load();
    };

    /** Preview of the gross amount as the user types the net amount. */
    const previewGross = form.amountReceived
        ? (Number(form.amountReceived) / 0.80).toFixed(2)
        : '0.00';

    return (
        <Stack spacing={3}>
            <Typography variant="h4">Direct Payments</Typography>
            <Typography variant="body2" color="text.secondary">
                Enter the amount you received in hand. The system calculates the gross amount (÷ 0.80)
                which is deducted from your total balance.
            </Typography>

            {/* FORM */}
            <Paper component="form" onSubmit={handleSubmit} sx={{ p: 2, border: editingId !== null ? '2px solid #1976d2' : undefined }}>
                <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                    {editingId !== null ? 'Edit Payment' : 'Add Payment'}
                </Typography>
                <Grid container spacing={2} alignItems="center">
                    <Grid item xs={12} md={2}>
                        <TextField
                            label="Payment Date"
                            type="date"
                            InputLabelProps={{ shrink: true }}
                            value={form.paymentDate}
                            onChange={(e) => setForm({ ...form, paymentDate: e.target.value })}
                            fullWidth
                            required
                        />
                    </Grid>
                    <Grid item xs={12} md={2}>
                        <TextField
                            label="Amount Received (Net)"
                            type="number"
                            inputProps={{ step: '0.01', min: '0' }}
                            value={form.amountReceived}
                            onChange={(e) => setForm({ ...form, amountReceived: e.target.value })}
                            fullWidth
                            required
                            helperText={`Gross deduction: $${previewGross}`}
                        />
                    </Grid>
                    <Grid item xs={12} md={4}>
                        <TextField
                            label="Notes (e.g. 10L@80)"
                            value={form.notes}
                            onChange={(e) => setForm({ ...form, notes: e.target.value })}
                            fullWidth
                        />
                    </Grid>
                    <Grid item xs={12} md={4}>
                        <Stack direction="row" spacing={2}>
                            <Button type="submit" variant="contained" startIcon={editingId !== null ? <SaveIcon /> : <AddIcon />}>
                                {editingId !== null ? 'Update Payment' : 'Add Payment'}
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

            {/* SUMMARY */}
            <Paper sx={{ p: 2 }}>
                <Stack direction="row" spacing={2} alignItems="center">
                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                        Total Deducted from Balance:
                    </Typography>
                    <Chip
                        label={money(totalGross)}
                        color="error"
                        sx={{ fontWeight: 700, fontSize: '1rem', px: 1 }}
                    />
                    <Typography variant="body2" color="text.secondary">
                        (sum of all gross amounts)
                    </Typography>
                </Stack>
            </Paper>

            {/* TABLE */}
            <Paper sx={{ p: 2, overflow: 'auto' }}>
                <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                    Payment History ({payments.length} payments)
                </Typography>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>Date</TableCell>
                            <TableCell align="right">Amount Received (Net)</TableCell>
                            <TableCell align="right">Gross Deducted (÷ 0.80)</TableCell>
                            <TableCell>Notes</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {payments.map((p) => (
                            <TableRow key={p.id} selected={editingId === p.id}>
                                <TableCell>{p.paymentDate}</TableCell>
                                <TableCell align="right">{money(p.amountReceived)}</TableCell>
                                <TableCell align="right" sx={{ color: 'error.main', fontWeight: 600 }}>
                                    {money(p.grossAmount)}
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
                                <TableCell colSpan={5} align="center" sx={{ color: 'text.secondary', py: 4 }}>
                                    No payments recorded yet.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </Paper>
        </Stack>
    );
}

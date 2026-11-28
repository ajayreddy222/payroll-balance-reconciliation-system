/**
 * @fileoverview Monthly Entries page — simplified to 5 fields.
 * Insurance is auto-applied from the insurance_rates table on save.
 * Fees and direct payments are recorded in the Payments Ledger.
 *
 * @module pages/Entries
 * @author Payroll Reconciliation Team
 */

import SaveIcon from '@mui/icons-material/Save';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import RemoveCircleIcon from '@mui/icons-material/RemoveCircle';
import {
    Button, Chip, Divider, Grid, IconButton, MenuItem,
    Paper, Stack, Table, TableBody, TableCell, TableHead,
    TableRow, TextField, Tooltip, Typography
} from '@mui/material';
import { FormEvent, useEffect, useState } from 'react';
import api from '../api/client';
import { MonthlyEntry, Project } from '../types';

const money = (v: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(v ?? 0);

type EntryRow = {
    projectId:            string;
    month:                string;
    hoursWorked:          number;
    employeeHourlyRate:   number;
    paystubAmountReceived: number;
    notes:                string;
};

const createEmptyRow = (month = ''): EntryRow => ({
    projectId:            '',
    month,
    hoursWorked:          0,
    employeeHourlyRate:   0,
    paystubAmountReceived: 0,
    notes:                ''
});

export default function Entries() {
    const [projects, setProjects]       = useState<Project[]>([]);
    const [entries, setEntries]         = useState<MonthlyEntry[]>([]);
    const [rows, setRows]               = useState<EntryRow[]>([createEmptyRow()]);
    const [editingId, setEditingId]     = useState<number | null>(null);
    const [editForm, setEditForm]       = useState<EntryRow>(createEmptyRow());
    const [filterYear, setFilterYear]   = useState<string>(String(new Date().getFullYear()));
    const [filterProject, setFilterProject] = useState<string>('all');

    const load = async () => {
        const [pr, er] = await Promise.all([api.get('/projects'), api.get('/monthly-entries')]);
        setProjects(pr.data);
        setEntries(er.data);
    };

    useEffect(() => { load(); }, []);

    const getDefaults = (projectId: string) => {
        const p = projects.find(p => String(p.id) === String(projectId));
        return {
            rate:    (p?.eightyTwentyRate && p.eightyTwentyRate > 0) ? p.eightyTwentyRate : (p?.employeeHourlyRate || 0),
            paystub: (p?.lcaAmount && p.lcaAmount > 0) ? p.lcaAmount : 0
        };
    };

    // --- Multi-row add ---
    const updateRow = (idx: number, field: keyof EntryRow, value: string | number) => {
        setRows(prev => { const u = [...prev]; u[idx] = { ...u[idx], [field]: value }; return u; });
    };

    const chooseProject = (idx: number, projectId: string) => {
        const { rate, paystub } = getDefaults(projectId);
        setRows(prev => {
            const u = [...prev];
            u[idx] = { ...u[idx], projectId, employeeHourlyRate: rate, paystubAmountReceived: paystub };
            return u;
        });
    };

    const addRow = () => {
        const lastMonth = rows[rows.length - 1]?.month ?? '';
        setRows(prev => [...prev, createEmptyRow(lastMonth)]);
    };

    const removeRow = (idx: number) => {
        if (rows.length <= 1) return;
        setRows(prev => prev.filter((_, i) => i !== idx));
    };

    const submitAll = async (e: FormEvent) => {
        e.preventDefault();
        const valid = rows.filter(r => r.projectId && r.month);
        if (!valid.length) return;
        await Promise.all(valid.map(r =>
            api.post('/monthly-entries', {
                projectId:            Number(r.projectId),
                month:                `${r.month}-01`,
                hoursWorked:          r.hoursWorked,
                employeeHourlyRate:   r.employeeHourlyRate,
                paystubAmountReceived: r.paystubAmountReceived,
                notes:                r.notes || null
            })
        ));
        setRows([createEmptyRow()]);
        load();
    };

    // --- Edit ---
    const startEdit = (entry: MonthlyEntry) => {
        setEditingId(entry.id);
        setEditForm({
            projectId:            String(entry.projectId),
            month:                entry.month.substring(0, 7),
            hoursWorked:          entry.hoursWorked,
            employeeHourlyRate:   entry.employeeHourlyRate,
            paystubAmountReceived: entry.paystubAmountReceived,
            notes:                entry.notes ?? ''
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const cancelEdit = () => { setEditingId(null); setEditForm(createEmptyRow()); };

    const chooseProjectEdit = (projectId: string) => {
        const { rate, paystub } = getDefaults(projectId);
        setEditForm(prev => ({ ...prev, projectId, employeeHourlyRate: rate, paystubAmountReceived: paystub }));
    };

    const submitEdit = async (e: FormEvent) => {
        e.preventDefault();
        await api.put(`/monthly-entries/${editingId}`, {
            projectId:            Number(editForm.projectId),
            month:                `${editForm.month}-01`,
            hoursWorked:          editForm.hoursWorked,
            employeeHourlyRate:   editForm.employeeHourlyRate,
            paystubAmountReceived: editForm.paystubAmountReceived,
            notes:                editForm.notes || null
        });
        setEditingId(null);
        setEditForm(createEmptyRow());
        load();
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Delete this entry?')) return;
        await api.delete(`/monthly-entries/${id}`);
        load();
    };

    const filtered = entries.filter(e => {
        const matchYear    = filterYear === 'all' || e.month.startsWith(filterYear);
        const matchProject = filterProject === 'all' || String(e.projectId) === filterProject;
        return matchYear && matchProject;
    });

    return (
        <Stack spacing={3}>
            <Typography variant="h4">Monthly Entries</Typography>
            <Typography variant="body2" color="text.secondary">
                Insurance is auto-applied from the Insurance Rates table. Fees and direct payments go in the Payments Ledger.
            </Typography>

            {/* EDIT FORM */}
            {editingId !== null && (
                <Paper component="form" onSubmit={submitEdit} sx={{ p: 2, border: '2px solid #1976d2' }}>
                    <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 600 }}>Edit Entry</Typography>
                    <Grid container spacing={2}>
                        <Grid item xs={12} md={3}>
                            <TextField select label="Project" value={editForm.projectId}
                                onChange={e => chooseProjectEdit(e.target.value)} fullWidth required>
                                {projects.map(p => <MenuItem key={p.id} value={p.id}>{p.projectName} - {p.vendorName}</MenuItem>)}
                            </TextField>
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <TextField label="Month" type="month" InputLabelProps={{ shrink: true }}
                                value={editForm.month}
                                onChange={e => setEditForm({ ...editForm, month: e.target.value })}
                                fullWidth required />
                        </Grid>
                        <Grid item xs={12} md={1.5}>
                            <TextField label="Hours" type="number" value={editForm.hoursWorked}
                                onChange={e => setEditForm({ ...editForm, hoursWorked: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12} md={1.5}>
                            <TextField label="Rate" type="number" value={editForm.employeeHourlyRate}
                                onChange={e => setEditForm({ ...editForm, employeeHourlyRate: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12} md={1.5}>
                            <TextField label="Paystub (LCA)" type="number" value={editForm.paystubAmountReceived}
                                onChange={e => setEditForm({ ...editForm, paystubAmountReceived: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12} md={2.5}>
                            <TextField label="Notes" value={editForm.notes}
                                onChange={e => setEditForm({ ...editForm, notes: e.target.value })} fullWidth />
                        </Grid>
                        <Grid item xs={12}>
                            <Stack direction="row" spacing={2}>
                                <Button type="submit" variant="contained" startIcon={<SaveIcon />}>Update Entry</Button>
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
                        Rate and Paystub auto-fill from project settings. Insurance auto-applied on save.
                    </Typography>
                    {rows.map((row, idx) => (
                        <div key={idx}>
                            {idx > 0 && <Divider sx={{ my: 1.5 }} />}
                            <Grid container spacing={1.5} alignItems="center">
                                <Grid item xs={12} md={3}>
                                    <TextField select label="Project" value={row.projectId} size="small"
                                        onChange={e => chooseProject(idx, e.target.value)} fullWidth required>
                                        {projects.map(p => <MenuItem key={p.id} value={p.id}>{p.projectName} - {p.vendorName}</MenuItem>)}
                                    </TextField>
                                </Grid>
                                <Grid item xs={6} md={1.5}>
                                    <TextField label="Month" type="month" InputLabelProps={{ shrink: true }}
                                        value={row.month} size="small"
                                        onChange={e => updateRow(idx, 'month', e.target.value)} fullWidth required />
                                </Grid>
                                <Grid item xs={6} md={1.3}>
                                    <TextField label="Hours" type="number" value={row.hoursWorked} size="small"
                                        onChange={e => updateRow(idx, 'hoursWorked', Number(e.target.value))} fullWidth />
                                </Grid>
                                <Grid item xs={6} md={1.3}>
                                    <TextField label="Rate" type="number" value={row.employeeHourlyRate} size="small"
                                        onChange={e => updateRow(idx, 'employeeHourlyRate', Number(e.target.value))} fullWidth />
                                </Grid>
                                <Grid item xs={6} md={1.3}>
                                    <TextField label="Paystub" type="number" value={row.paystubAmountReceived} size="small"
                                        onChange={e => updateRow(idx, 'paystubAmountReceived', Number(e.target.value))} fullWidth />
                                </Grid>
                                <Grid item xs={12} md={2.5}>
                                    <TextField label="Notes" value={row.notes} size="small"
                                        onChange={e => updateRow(idx, 'notes', e.target.value)} fullWidth />
                                </Grid>
                                <Grid item xs={2} md={0.6}>
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
                        <Button type="submit" variant="contained" startIcon={<SaveIcon />}>Save All Entries</Button>
                    </Stack>
                </Paper>
            )}

            {/* TABLE */}
            <Paper sx={{ p: 2, overflow: 'auto' }}>
                <Stack direction="row" spacing={2} sx={{ mb: 2 }} alignItems="center">
                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Entries</Typography>
                    <TextField select label="Year" value={filterYear}
                        onChange={e => setFilterYear(e.target.value)} size="small" sx={{ minWidth: 100 }}>
                        <MenuItem value="all">All Years</MenuItem>
                        {[...new Set(entries.map(e => e.month.substring(0, 4)))].sort().reverse().map(y => (
                            <MenuItem key={y} value={y}>{y}</MenuItem>
                        ))}
                    </TextField>
                    <TextField select label="Project" value={filterProject}
                        onChange={e => setFilterProject(e.target.value)} size="small" sx={{ minWidth: 180 }}>
                        <MenuItem value="all">All Projects</MenuItem>
                        {projects.map(p => (
                            <MenuItem key={p.id} value={String(p.id)}>{p.projectName} - {p.vendorName}</MenuItem>
                        ))}
                    </TextField>
                    <Chip label={`${filtered.length} entries`} size="small" />
                </Stack>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>Month</TableCell>
                            <TableCell>Project</TableCell>
                            <TableCell align="right">Hours</TableCell>
                            <TableCell align="right">Rate</TableCell>
                            <TableCell align="right">Actual</TableCell>
                            <TableCell align="right">Paystub</TableCell>
                            <TableCell align="right">Insurance</TableCell>
                            <TableCell align="right">Balance</TableCell>
                            <TableCell>Notes</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {filtered.map(e => (
                            <TableRow key={e.id} selected={editingId === e.id}>
                                <TableCell>{e.month.substring(0, 7)}</TableCell>
                                <TableCell>{e.projectName}</TableCell>
                                <TableCell align="right">{e.hoursWorked}</TableCell>
                                <TableCell align="right">{money(e.employeeHourlyRate)}</TableCell>
                                <TableCell align="right">{money(e.actualEarnings)}</TableCell>
                                <TableCell align="right">{money(e.paystubAmountReceived)}</TableCell>
                                <TableCell align="right">{money(e.insuranceDeduction)}</TableCell>
                                <TableCell align="right">{money(e.monthlyBalance)}</TableCell>
                                <TableCell>{e.notes ?? '—'}</TableCell>
                                <TableCell align="center">
                                    <Tooltip title="Edit">
                                        <IconButton size="small" onClick={() => startEdit(e)}>
                                            <EditIcon fontSize="small" />
                                        </IconButton>
                                    </Tooltip>
                                    <Tooltip title="Delete">
                                        <IconButton size="small" color="error" onClick={() => handleDelete(e.id)}>
                                            <DeleteIcon fontSize="small" />
                                        </IconButton>
                                    </Tooltip>
                                </TableCell>
                            </TableRow>
                        ))}
                        {filtered.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={10} align="center" sx={{ color: 'text.secondary', py: 4 }}>
                                    No entries found.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </Paper>
        </Stack>
    );
}

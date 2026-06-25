/**
 * @fileoverview Monthly Entries page for managing payroll data entries.
 *
 * Provides functionality to:
 * - Add multiple entries at once (multi-row form)
 * - Edit existing entries individually
 * - Delete entries with confirmation
 * - Filter the entries table by year and/or project
 * - Auto-fill Rate and Paystub from project defaults when selecting a project
 *
 * Each entry represents one month's payroll data for one project,
 * including hours worked, rate, paystub, direct payments, insurance, and adjustments.
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
    Button,
    Divider,
    Grid,
    IconButton,
    MenuItem,
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
import { MonthlyEntry, Project } from '../types';

/**
 * Formats a numeric value as US dollar currency.
 *
 * @param value - The numeric amount to format
 * @returns Formatted currency string (e.g., "$1,234.56")
 */
const money = (value: number) => new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
}).format(value ?? 0);

/** Shape of a single entry row in the multi-add form. */
type EntryRow = {
    projectId: string;
    month: string;
    hoursWorked: number;
    employeeHourlyRate: number;
    paystubAmountReceived: number;
    directEmployerPayment: number;
    insuranceDeduction: number;
    otherAdjustment: number;
    notes: string;
};

/**
 * Creates a new empty entry row with default zero values.
 *
 * @param month - The month to pre-fill (YYYY-MM format, or empty string)
 * @returns A new EntryRow with all fields initialized
 */
const createEmptyRow = (month: string): EntryRow => ({
    projectId: '',
    month,
    hoursWorked: 0,
    employeeHourlyRate: 0,
    paystubAmountReceived: 0,
    directEmployerPayment: 0,
    insuranceDeduction: 0,
    otherAdjustment: 0,
    notes: ''
});

/**
 * Monthly Entries page component.
 *
 * Manages monthly payroll entry CRUD with:
 * - Multi-row add form for batch entry creation
 * - Single-entry edit form with project defaults auto-fill
 * - Filterable data table (by year and project)
 * - Delete with confirmation dialog
 *
 * @returns The monthly entries management page UI
 */
export default function Entries() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [entries, setEntries] = useState<MonthlyEntry[]>([]);
    const [rows, setRows] = useState<EntryRow[]>([createEmptyRow('')]);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [editForm, setEditForm] = useState<EntryRow>(createEmptyRow(''));
    const [filterYear, setFilterYear] = useState<string>(String(new Date().getFullYear()));
    const [filterProject, setFilterProject] = useState<string>('all');

    const load = async () => {
        const [projectResponse, entryResponse] = await Promise.all([api.get('/projects'), api.get('/monthly-entries')]);
        setProjects(projectResponse.data);
        setEntries(entryResponse.data);
    };

    useEffect(() => { load(); }, []);

    const getProjectDefaults = (projectId: string) => {
        const project = projects.find((p) => String(p.id) === String(projectId));
        const rate = (project?.eightyTwentyRate && project.eightyTwentyRate > 0)
            ? project.eightyTwentyRate
            : (project?.employeeHourlyRate || 0);
        const paystub = (project?.lcaAmount && project.lcaAmount > 0)
            ? project.lcaAmount
            : 0;
        return { rate, paystub };
    };

    // --- Multi-row add logic ---
    const updateRow = (index: number, field: keyof EntryRow, value: string | number) => {
        setRows(prev => {
            const updated = [...prev];
            updated[index] = { ...updated[index], [field]: value };
            return updated;
        });
    };

    const chooseProjectForRow = (index: number, projectId: string) => {
        const { rate, paystub } = getProjectDefaults(projectId);
        setRows(prev => {
            const updated = [...prev];
            updated[index] = {
                ...updated[index],
                projectId,
                employeeHourlyRate: rate,
                paystubAmountReceived: paystub
            };
            return updated;
        });
    };

    const addRow = () => {
        const lastMonth = rows.length > 0 ? rows[rows.length - 1].month : '';
        setRows(prev => [...prev, createEmptyRow(lastMonth)]);
    };

    const removeRow = (index: number) => {
        if (rows.length <= 1) return;
        setRows(prev => prev.filter((_, i) => i !== index));
    };

    const submitAll = async (event: FormEvent) => {
        event.preventDefault();
        const validRows = rows.filter(r => r.projectId && r.month);
        if (validRows.length === 0) return;

        await Promise.all(validRows.map(row =>
            api.post('/monthly-entries', {
                ...row,
                projectId: Number(row.projectId),
                month: `${row.month}-01`
            })
        ));

        setRows([createEmptyRow('')]);
        load();
    };

    // --- Edit single entry logic ---
    const startEdit = (entry: MonthlyEntry) => {
        setEditingId(entry.id);
        setEditForm({
            projectId: String(entry.projectId),
            month: entry.month.substring(0, 7),
            hoursWorked: entry.hoursWorked,
            employeeHourlyRate: entry.employeeHourlyRate,
            paystubAmountReceived: entry.paystubAmountReceived,
            directEmployerPayment: entry.directEmployerPayment,
            insuranceDeduction: entry.insuranceDeduction,
            otherAdjustment: entry.otherAdjustment,
            notes: entry.notes ?? ''
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const cancelEdit = () => {
        setEditingId(null);
        setEditForm(createEmptyRow(''));
    };

    const chooseProjectForEdit = (projectId: string) => {
        const { rate, paystub } = getProjectDefaults(projectId);
        setEditForm(prev => ({ ...prev, projectId, employeeHourlyRate: rate, paystubAmountReceived: paystub }));
    };

    const submitEdit = async (event: FormEvent) => {
        event.preventDefault();
        const payload = { ...editForm, projectId: Number(editForm.projectId), month: `${editForm.month}-01` };
        await api.put(`/monthly-entries/${editingId}`, payload);
        setEditingId(null);
        setEditForm(createEmptyRow(''));
        load();
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Delete this monthly entry?')) return;
        await api.delete(`/monthly-entries/${id}`);
        load();
    };

    return (
        <Stack spacing={3}>
            <Typography variant="h4">Monthly Entries</Typography>

            {/* EDIT FORM (shown only when editing) */}
            {editingId !== null && (
                <Paper component="form" onSubmit={submitEdit} sx={{ p: 2, border: '2px solid #1976d2' }}>
                    <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 600 }}>Edit Entry</Typography>
                    <Grid container spacing={2}>
                        <Grid item xs={12} md={3}>
                            <TextField select label="Project" value={editForm.projectId} onChange={(e) => chooseProjectForEdit(e.target.value)} fullWidth required>
                                {projects.map((p) => <MenuItem key={p.id} value={p.id}>{p.projectName} - {p.vendorName}</MenuItem>)}
                            </TextField>
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <TextField label="Month" type="month" InputLabelProps={{ shrink: true }} value={editForm.month} onChange={(e) => setEditForm({ ...editForm, month: e.target.value })} fullWidth required />
                        </Grid>
                        <Grid item xs={12} md={1.4}>
                            <TextField label="Hours" type="number" value={editForm.hoursWorked} onChange={(e) => setEditForm({ ...editForm, hoursWorked: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12} md={1.4}>
                            <TextField label="Rate" type="number" value={editForm.employeeHourlyRate} onChange={(e) => setEditForm({ ...editForm, employeeHourlyRate: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12} md={1.4}>
                            <TextField label="Paystub" type="number" value={editForm.paystubAmountReceived} onChange={(e) => setEditForm({ ...editForm, paystubAmountReceived: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12} md={1.4}>
                            <TextField label="Direct Pay" type="number" value={editForm.directEmployerPayment} onChange={(e) => setEditForm({ ...editForm, directEmployerPayment: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12} md={1.4}>
                            <TextField label="Insurance" type="number" value={editForm.insuranceDeduction} onChange={(e) => setEditForm({ ...editForm, insuranceDeduction: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12} md={1.4}>
                            <TextField label="Adjustment" type="number" value={editForm.otherAdjustment} onChange={(e) => setEditForm({ ...editForm, otherAdjustment: Number(e.target.value) })} fullWidth />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField label="Notes" value={editForm.notes} onChange={(e) => setEditForm({ ...editForm, notes: e.target.value })} fullWidth multiline minRows={2} />
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

            {/* MULTI-ROW ADD FORM (hidden when editing) */}
            {editingId === null && (
                <Paper component="form" onSubmit={submitAll} sx={{ p: 2 }}>
                    <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 600 }}>
                        Add Entries
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        Add multiple entries at once. Select a project per row — Rate and Paystub auto-fill from project settings.
                    </Typography>

                    {rows.map((row, idx) => (
                        <div key={idx}>
                            {idx > 0 && <Divider sx={{ my: 1.5 }} />}
                            <Grid container spacing={1.5} alignItems="center">
                                <Grid item xs={12} md={2.5}>
                                    <TextField select label="Project" value={row.projectId} onChange={(e) => chooseProjectForRow(idx, e.target.value)} fullWidth required size="small">
                                        {projects.map((p) => <MenuItem key={p.id} value={p.id}>{p.projectName} - {p.vendorName}</MenuItem>)}
                                    </TextField>
                                </Grid>
                                <Grid item xs={6} md={1.5}>
                                    <TextField label="Month" type="month" InputLabelProps={{ shrink: true }} value={row.month} onChange={(e) => updateRow(idx, 'month', e.target.value)} fullWidth required size="small" />
                                </Grid>
                                <Grid item xs={6} md={1}>
                                    <TextField label="Hours" type="number" value={row.hoursWorked} onChange={(e) => updateRow(idx, 'hoursWorked', Number(e.target.value))} fullWidth size="small" />
                                </Grid>
                                <Grid item xs={6} md={1}>
                                    <TextField label="Rate" type="number" value={row.employeeHourlyRate} onChange={(e) => updateRow(idx, 'employeeHourlyRate', Number(e.target.value))} fullWidth size="small" />
                                </Grid>
                                <Grid item xs={6} md={1}>
                                    <TextField label="Paystub" type="number" value={row.paystubAmountReceived} onChange={(e) => updateRow(idx, 'paystubAmountReceived', Number(e.target.value))} fullWidth size="small" />
                                </Grid>
                                <Grid item xs={6} md={1}>
                                    <TextField label="Direct Pay" type="number" value={row.directEmployerPayment} onChange={(e) => updateRow(idx, 'directEmployerPayment', Number(e.target.value))} fullWidth size="small" />
                                </Grid>
                                <Grid item xs={6} md={1}>
                                    <TextField label="Insurance" type="number" value={row.insuranceDeduction} onChange={(e) => updateRow(idx, 'insuranceDeduction', Number(e.target.value))} fullWidth size="small" />
                                </Grid>
                                <Grid item xs={6} md={1}>
                                    <TextField label="Adjust" type="number" value={row.otherAdjustment} onChange={(e) => updateRow(idx, 'otherAdjustment', Number(e.target.value))} fullWidth size="small" />
                                </Grid>
                                <Grid item xs={12} md={2}>
                                    <TextField label="Notes" value={row.notes} onChange={(e) => updateRow(idx, 'notes', e.target.value)} fullWidth size="small" />
                                </Grid>
                                <Grid item xs={6} md={0.5}>
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
                        <Button variant="outlined" startIcon={<AddIcon />} onClick={addRow}>
                            Add Row
                        </Button>
                        <Button type="submit" variant="contained" startIcon={<SaveIcon />}>
                            Save All Entries
                        </Button>
                    </Stack>
                </Paper>
            )}

            {/* ENTRIES TABLE */}
            <Paper sx={{ p: 2, overflow: 'auto' }}>
                <Stack direction="row" spacing={2} sx={{ mb: 2 }} alignItems="center">
                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Entries</Typography>
                    <TextField select label="Year" value={filterYear} onChange={(e) => setFilterYear(e.target.value)} size="small" sx={{ minWidth: 100 }}>
                        <MenuItem value="all">All Years</MenuItem>
                        {[...new Set(entries.map(e => e.month.substring(0, 4)))].sort().reverse().map(y => (
                            <MenuItem key={y} value={y}>{y}</MenuItem>
                        ))}
                    </TextField>
                    <TextField select label="Project" value={filterProject} onChange={(e) => setFilterProject(e.target.value)} size="small" sx={{ minWidth: 180 }}>
                        <MenuItem value="all">All Projects</MenuItem>
                        {projects.map(p => (
                            <MenuItem key={p.id} value={String(p.id)}>{p.projectName} - {p.vendorName}</MenuItem>
                        ))}
                    </TextField>
                    <Typography variant="body2" color="text.secondary">
                        {entries.filter(e => {
                            const matchYear = filterYear === 'all' || e.month.startsWith(filterYear);
                            const matchProject = filterProject === 'all' || String(e.projectId) === filterProject;
                            return matchYear && matchProject;
                        }).length} entries
                    </Typography>
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
                            <TableCell align="right">Direct Gross</TableCell>
                            <TableCell align="right">Insurance</TableCell>
                            <TableCell align="right">Adjustment</TableCell>
                            <TableCell align="right">Balance</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {entries
                            .filter(e => {
                                const matchYear = filterYear === 'all' || e.month.startsWith(filterYear);
                                const matchProject = filterProject === 'all' || String(e.projectId) === filterProject;
                                return matchYear && matchProject;
                            })
                            .map((e) => (
                            <TableRow key={e.id} selected={editingId === e.id}>
                                <TableCell>{e.month}</TableCell>
                                <TableCell>{e.projectName}</TableCell>
                                <TableCell align="right">{e.hoursWorked}</TableCell>
                                <TableCell align="right">{money(e.employeeHourlyRate)}</TableCell>
                                <TableCell align="right">{money(e.actualEarnings)}</TableCell>
                                <TableCell align="right">{money(e.paystubAmountReceived)}</TableCell>
                                <TableCell align="right">{money(e.directEmployerGrossAmount)}</TableCell>
                                <TableCell align="right">{money(e.insuranceDeduction)}</TableCell>
                                <TableCell align="right">{money(e.otherAdjustment)}</TableCell>
                                <TableCell align="right">{money(e.monthlyBalance)}</TableCell>
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
                    </TableBody>
                </Table>
            </Paper>
        </Stack>
    );
}

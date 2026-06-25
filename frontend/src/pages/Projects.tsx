/**
 * @fileoverview Projects page for managing payroll project configurations.
 *
 * Provides functionality to:
 * - Create new projects with rate, vendor fee, 80-20 rate, and LCA settings
 * - Edit existing projects (pre-fills form with current values)
 * - Auto-calculate the 80-20 rate from base rate and vendor fee percentage
 * - Display all projects as summary cards with key details
 *
 * @module pages/Projects
 * @author Payroll Reconciliation Team
 */

import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import CancelIcon from '@mui/icons-material/Cancel';
import SaveIcon from '@mui/icons-material/Save';
import { Button, Grid, IconButton, Paper, Stack, TextField, Tooltip, Typography } from '@mui/material';
import { FormEvent, useEffect, useState } from 'react';
import api from '../api/client';
import { Project } from '../types';

/** Default empty form state for the project form. */
const emptyForm = {
  clientName: '',
  vendorName: '',
  projectName: '',
  employeeHourlyRate: 0,
  vendorFeePercentage: 0,
  eightyTwentyRate: 0,
  lcaAmount: 0,
  startDate: '',
  endDate: ''
};

/**
 * Projects page component.
 *
 * Manages project CRUD operations with an inline form that toggles between
 * "Add" and "Edit" mode. Projects are displayed as cards in a grid layout.
 *
 * @returns The projects management page UI
 */
export default function Projects() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);

  const load = async () => setProjects((await api.get('/projects')).data);
  useEffect(() => { load(); }, []);

  /**
   * Populates the form with an existing project's data for editing.
   * Scrolls to the top of the page to show the form.
   *
   * @param project - The project to edit
   */
  const startEdit = (project: Project) => {
    setEditingId(project.id);
    setForm({
      clientName: project.clientName,
      vendorName: project.vendorName,
      projectName: project.projectName,
      employeeHourlyRate: project.employeeHourlyRate,
      vendorFeePercentage: project.vendorFeePercentage,
      eightyTwentyRate: project.eightyTwentyRate,
      lcaAmount: project.lcaAmount,
      startDate: project.startDate,
      endDate: project.endDate ?? ''
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  /** Exits edit mode and resets the form to empty state. */
  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  /**
   * Auto-calculates the 80-20 rate when the base rate or vendor fee changes.
   * Formula: 80-20 Rate = (Rate × (1 - VendorFee%)) × 0.80
   *
   * @param newRate - The base hourly rate
   * @param newFee - The vendor fee percentage
   */
  // Auto-calculate 80-20 rate when Rate or Vendor Fee changes
  const updateRate = (newRate: number, newFee: number) => {
    const afterFee = newFee > 0 ? newRate * (1 - newFee / 100) : newRate;
    const eightyTwenty = afterFee * 0.80;
    setForm(f => ({
      ...f,
      employeeHourlyRate: newRate,
      vendorFeePercentage: newFee,
      eightyTwentyRate: Math.round(eightyTwenty * 100) / 100
    }));
  };

  /**
   * Handles form submission — creates a new project or updates an existing one.
   * Resets the form and reloads the project list on success.
   *
   * @param event - The form submission event
   */
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const payload = { ...form, active: true, endDate: form.endDate || null };
    if (editingId !== null) {
      await api.put(`/projects/${editingId}`, payload);
    } else {
      await api.post('/projects', payload);
    }
    setEditingId(null);
    setForm(emptyForm);
    load();
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h4">Projects</Typography>
      <Paper component="form" onSubmit={submit} sx={{ p: 2 }}>
        <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 600 }}>
          {editingId !== null ? 'Edit Project' : 'Add Project'}
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} md={3}>
            <TextField label="Client" value={form.clientName} onChange={(e) => setForm({ ...form, clientName: e.target.value })} fullWidth required />
          </Grid>
          <Grid item xs={12} md={3}>
            <TextField label="Vendor" value={form.vendorName} onChange={(e) => setForm({ ...form, vendorName: e.target.value })} fullWidth required />
          </Grid>
          <Grid item xs={12} md={3}>
            <TextField label="Project Name" value={form.projectName} onChange={(e) => setForm({ ...form, projectName: e.target.value })} fullWidth required />
          </Grid>
          <Grid item xs={12} md={3}>
            <TextField label="Start Date" type="date" InputLabelProps={{ shrink: true }} value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} fullWidth required />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField label="Rate ($)" type="number" value={form.employeeHourlyRate} onChange={(e) => updateRate(Number(e.target.value), form.vendorFeePercentage)} fullWidth required />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField label="Vendor Fee %" type="number" value={form.vendorFeePercentage} onChange={(e) => updateRate(form.employeeHourlyRate, Number(e.target.value))} fullWidth />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField label="80-20 Rate ($)" type="number" value={form.eightyTwentyRate} onChange={(e) => setForm({ ...form, eightyTwentyRate: Number(e.target.value) })} fullWidth
              helperText="Auto-calculated or enter manually" />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField label="LCA Amount ($)" type="number" value={form.lcaAmount} onChange={(e) => setForm({ ...form, lcaAmount: Number(e.target.value) })} fullWidth />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField label="End Date" type="date" InputLabelProps={{ shrink: true }} value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} fullWidth />
          </Grid>
          <Grid item xs={12}>
            <Stack direction="row" spacing={2}>
              <Button type="submit" variant="contained" startIcon={editingId !== null ? <SaveIcon /> : <AddIcon />}>
                {editingId !== null ? 'Update Project' : 'Add Project'}
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
      <Grid container spacing={2}>
        {projects.map((p) => (
          <Grid item xs={12} md={4} key={p.id}>
            <Paper sx={{ p: 2, position: 'relative' }}>
              <Tooltip title="Edit">
                <IconButton size="small" sx={{ position: 'absolute', top: 8, right: 8 }} onClick={() => startEdit(p)}>
                  <EditIcon fontSize="small" />
                </IconButton>
              </Tooltip>
              <Typography variant="h6">{p.projectName}</Typography>
              <Typography>{p.clientName} / {p.vendorName}</Typography>
              <Typography color="text.secondary">
                Rate: ${p.employeeHourlyRate}/hr
                {p.vendorFeePercentage > 0 && ` | Vendor Fee: ${p.vendorFeePercentage}%`}
              </Typography>
              <Typography color="text.secondary">
                80-20: ${p.eightyTwentyRate}/hr
                {p.lcaAmount > 0 && ` | LCA: $${p.lcaAmount}`}
              </Typography>
              <Typography color="text.secondary">From {p.startDate}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Stack>
  );
}

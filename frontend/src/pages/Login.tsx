/**
 * @fileoverview Login page component for user authentication.
 *
 * Provides a form for email/password authentication against the backend
 * /api/auth/login endpoint. On successful login, stores the JWT token
 * in localStorage and redirects to the dashboard.
 *
 * @module pages/Login
 * @author Payroll Reconciliation Team
 */

import LockIcon from '@mui/icons-material/Lock';
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from '@mui/material';
import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';

/**
 * Login page component.
 *
 * Renders a centered login form with email and password fields.
 * On submission, authenticates via the API and stores the JWT token.
 * Displays an error alert on failed login attempts.
 *
 * @returns The login page UI
 */
export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('admin@example.com');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState('');

  /**
   * Handles form submission — sends credentials to the auth endpoint,
   * stores the returned JWT token, and navigates to the dashboard.
   *
   * @param event - The form submission event
   */
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      const { data } = await api.post('/auth/login', { email, password });
      localStorage.setItem('token', data.token);
      navigate('/');
    } catch {
      setError('Login failed. Check email and password.');
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', bgcolor: 'background.default', p: 2 }}>
      <Paper component="form" onSubmit={submit} sx={{ width: '100%', maxWidth: 420, p: 4 }}>
        <Stack spacing={2.5}>
          <Box>
            <LockIcon color="primary" />
            <Typography variant="h4">Sign in</Typography>
            <Typography color="text.secondary">Payroll balance reconciliation</Typography>
          </Box>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="Email" value={email} onChange={(e) => setEmail(e.target.value)} fullWidth />
          <TextField label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} fullWidth />
          <Button type="submit" variant="contained" size="large">Login</Button>
        </Stack>
      </Paper>
    </Box>
  );
}

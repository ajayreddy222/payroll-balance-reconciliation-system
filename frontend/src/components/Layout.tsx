/**
 * @fileoverview Application shell layout with top navigation bar and content outlet.
 *
 * Provides the consistent page structure for all authenticated routes:
 * - AppBar with navigation links (Dashboard, Projects, Monthly Entries, Reports)
 * - Logout button that clears the JWT token and redirects to /login
 * - Content container where child routes are rendered via React Router's Outlet
 *
 * @module components/Layout
 * @author Payroll Reconciliation Team
 */

import DashboardIcon from '@mui/icons-material/Dashboard';
import FolderIcon from '@mui/icons-material/Folder';
import PaidIcon from '@mui/icons-material/Paid';
import AssessmentIcon from '@mui/icons-material/Assessment';
import LogoutIcon from '@mui/icons-material/Logout';
import { AppBar, Box, Button, Container, Stack, Toolbar, Typography } from '@mui/material';
import { Link, Outlet, useNavigate } from 'react-router-dom';

/** Navigation items displayed in the top app bar. */
const nav = [
  { label: 'Dashboard', path: '/', icon: <DashboardIcon fontSize="small" /> },
  { label: 'Projects', path: '/projects', icon: <FolderIcon fontSize="small" /> },
  { label: 'Monthly Entries', path: '/entries', icon: <PaidIcon fontSize="small" /> },
  { label: 'Reports', path: '/reports', icon: <AssessmentIcon fontSize="small" /> }
];

/**
 * Main layout component that wraps all authenticated pages.
 *
 * Renders:
 * - A top AppBar with the application title, navigation links, and logout button
 * - A responsive Container that renders the active route's component via Outlet
 *
 * @returns The application layout with navigation and content area
 */
export default function Layout() {
  const navigate = useNavigate();

  /**
   * Logs the user out by removing the JWT token from localStorage
   * and redirecting to the login page.
   */
  const logout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static" color="inherit" elevation={1}>
        <Toolbar sx={{ gap: 2 }}>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Payroll Reconciliation</Typography>
          <Stack direction="row" spacing={1} sx={{ display: { xs: 'none', md: 'flex' } }}>
            {nav.map((item) => <Button key={item.path} component={Link} to={item.path} startIcon={item.icon}>{item.label}</Button>)}
          </Stack>
          <Button color="primary" onClick={logout} startIcon={<LogoutIcon />}>Logout</Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="xl" sx={{ py: 3 }}>
        <Outlet />
      </Container>
    </Box>
  );
}

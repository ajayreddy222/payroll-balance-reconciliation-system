/**
 * @fileoverview Material UI theme configuration for the Payroll Reconciliation application.
 *
 * Defines the visual design system including color palette, border radius,
 * and typography settings used across all MUI components.
 *
 * @module theme/theme
 * @author Payroll Reconciliation Team
 */

import { createTheme } from '@mui/material/styles';

/**
 * Custom Material UI theme with application-specific branding.
 *
 * - Primary color: Blue (#2457a6) — used for buttons, links, active states
 * - Secondary color: Teal (#0f8b8d) — used for charts and accents
 * - Background: Light gray (#f6f7f9) — page background
 * - Border radius: 8px — rounded corners on cards and inputs
 * - Typography: Inter/Roboto font family with bold headings
 */
export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#2457a6' },
    secondary: { main: '#0f8b8d' },
    background: { default: '#f6f7f9' }
  },
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: 'Inter, Roboto, Arial, sans-serif',
    h4: { fontWeight: 700 },
    h6: { fontWeight: 700 }
  }
});

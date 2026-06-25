/**
 * @fileoverview Application entry point — bootstraps React with MUI theme and routing.
 *
 * Renders the root React component tree wrapped with:
 * - React.StrictMode for development warnings
 * - ThemeProvider for Material UI theming
 * - CssBaseline for consistent cross-browser styling
 * - BrowserRouter for client-side routing
 *
 * @module main
 * @author Payroll Reconciliation Team
 */

import React from 'react';
import ReactDOM from 'react-dom/client';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { theme } from './theme/theme';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ThemeProvider>
  </React.StrictMode>
);

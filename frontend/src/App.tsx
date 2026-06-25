/**
 * @fileoverview Root application component defining route configuration.
 *
 * Sets up the React Router route tree with:
 * - Public route: /login
 * - Protected routes (require JWT): Dashboard, Projects, Entries, Reports
 *
 * @module App
 * @author Payroll Reconciliation Team
 */

import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Entries from './pages/Entries';
import Login from './pages/Login';
import Projects from './pages/Projects';
import Reports from './pages/Reports';

/**
 * Route guard component that checks for a valid JWT token in localStorage.
 * Redirects to /login if no token is found.
 *
 * @param props.children - The protected content to render when authenticated
 * @returns The children if authenticated, or a redirect to /login
 */
function RequireAuth({ children }: { children: JSX.Element }) {
  return localStorage.getItem('token') ? children : <Navigate to="/login" replace />;
}

/**
 * Root application component that defines all routes.
 *
 * Route structure:
 * - /login — Public login page
 * - / — Dashboard (protected)
 * - /projects — Project management (protected)
 * - /entries — Monthly entry management (protected)
 * - /reports — Report generation and download (protected)
 *
 * @returns The application route tree
 */
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<RequireAuth><Layout /></RequireAuth>}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/projects" element={<Projects />} />
        <Route path="/entries" element={<Entries />} />
        <Route path="/reports" element={<Reports />} />
      </Route>
    </Routes>
  );
}

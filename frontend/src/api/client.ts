/**
 * @fileoverview Axios HTTP client configured for the Payroll Reconciliation API.
 *
 * Creates a pre-configured Axios instance with:
 * - Base URL pointing to the backend API
 * - Automatic JWT token injection via request interceptor
 *
 * @module api/client
 * @author Payroll Reconciliation Team
 */

import axios from 'axios';

/**
 * Pre-configured Axios instance for all API communication.
 *
 * Base URL is resolved from the `VITE_API_BASE_URL` environment variable,
 * falling back to `http://localhost:8080/api` for local development.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'
});

/**
 * Request interceptor that attaches the JWT Bearer token from localStorage
 * to every outgoing request's Authorization header.
 *
 * If no token is found in localStorage, the request proceeds without authentication.
 */
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default api;

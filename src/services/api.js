import axios from 'axios';

export const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8081/CampusServiceManagementSystemBackend/web';

export const api = axios.create({
  baseURL: API_BASE,
  // The hosted backend sleeps when idle and can take ~30-60 s to wake up.
  timeout: 90000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/** Ping the backend as soon as the app opens, so it is awake by the time someone signs in. */
export function warmUp() {
  return axios.get(API_BASE, { timeout: 90000 }).catch(() => {});
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('smartCampusToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// An expired or invalid token: sign out and send the user to the login page once.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const isAuthCall = /\/api\/auth\/(login|register)/.test(error?.config?.url || '');
    if (status === 401 && !isAuthCall && localStorage.getItem('smartCampusToken')) {
      localStorage.removeItem('smartCampusToken');
      localStorage.removeItem('smartCampusUser');
      if (window.location.pathname !== '/login') window.location.assign('/login?expired=1');
    }
    return Promise.reject(error);
  },
);

export function unwrap(response) {
  return response.data;
}

export function apiError(error) {
  const status = error?.response?.status;
  const data = error?.response?.data;
  if (data && typeof data === 'object' && data.message) return data.message;
  if (status === 401) return 'Your session has expired. Please sign in again.';
  if (status === 403) return 'Your role does not have access to this.';
  if (status === 404) return 'Not found.';
  if (error?.code === 'ECONNABORTED') return 'The server took too long to respond. Please try again.';
  if (!error?.response) return 'Cannot reach the server. It may be waking up; please try again in a moment.';
  if (typeof data === 'string') {
    // Tomcat error pages are HTML: show their title, not the whole page.
    const title = data.match(/<title>([^<]*)<\/title>/i)?.[1];
    const text = (title || data.replace(/<[^>]*>/g, ' ')).replace(/\s+/g, ' ').trim();
    return text.slice(0, 200) || `Request failed (${status}).`;
  }
  return error?.message || 'Request failed';
}


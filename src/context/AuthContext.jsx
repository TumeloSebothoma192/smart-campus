import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { api, apiError, unwrap } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('smartCampusToken') || '');
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('smartCampusUser');
    return raw ? JSON.parse(raw) : null;
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!token || user) return;
    api.get('/api/auth/me')
      .then((response) => {
        setUser(response.data);
        localStorage.setItem('smartCampusUser', JSON.stringify(response.data));
      })
      .catch(() => logout());
  }, [token]);

  async function login(credentials) {
    setLoading(true);
    setError('');
    try {
      const data = unwrap(await api.post('/api/auth/login', credentials));
      localStorage.setItem('smartCampusToken', data.token);
      localStorage.setItem('smartCampusUser', JSON.stringify(data.user));
      setToken(data.token);
      setUser(data.user);
      return data.user;
    } catch (err) {
      const message = apiError(err);
      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  }

  async function register(payload) {
    setLoading(true);
    setError('');
    try {
      const data = unwrap(await api.post('/api/auth/register', payload));
      localStorage.setItem('smartCampusToken', data.token);
      localStorage.setItem('smartCampusUser', JSON.stringify(data.user));
      setToken(data.token);
      setUser(data.user);
      return data.user;
    } catch (err) {
      const message = apiError(err);
      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem('smartCampusToken');
    localStorage.removeItem('smartCampusUser');
    setToken('');
    setUser(null);
  }

  const value = useMemo(() => ({
    token,
    user,
    isAuthenticated: Boolean(token && user),
    loading,
    error,
    login,
    register,
    logout,
  }), [token, user, loading, error]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}

import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { LockKeyhole, School } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';

const demos = [
  ['admin@campus.local', 'admin123'],
  ['lecturer@campus.local', 'lecturer123'],
  ['manager@campus.local', 'manager123'],
  ['staff@campus.local', 'staff123'],
  ['student@campus.local', 'student123'],
];

export default function Login() {
  const [form, setForm] = useState({ username: 'admin@campus.local', password: 'admin123' });
  const [localError, setLocalError] = useState('');
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const expired = new URLSearchParams(location.search).get('expired');
  const [slow, setSlow] = useState(false);

  // The demo backend sleeps when idle; after a few seconds, say so instead of looking stuck.
  useEffect(() => {
    if (!loading) { setSlow(false); return undefined; }
    const id = setTimeout(() => setSlow(true), 4000);
    return () => clearTimeout(id);
  }, [loading]);

  async function submit(event) {
    event.preventDefault();
    setLocalError('');
    try {
      await login(form);
      navigate(location.state?.from?.pathname || '/dashboard', { replace: true });
    } catch (err) {
      setLocalError(err.message);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-hero">
        <div className="auth-brand"><School size={28} /> Smart Campus</div>
        <h1>One portal for rooms, modules, support, and campus operations.</h1>
        <p>Book rooms, register for modules, log maintenance issues and track service requests, with a dashboard for each campus role.</p>
      </section>

      <section className="auth-card">
        <div className="auth-icon"><LockKeyhole size={24} /></div>
        <h2>Sign in</h2>
        <p>Use a demo account or your registered campus profile.</p>
        {expired && !localError && <div className="alert warning">Your session expired. Please sign in again.</div>}
        {localError && <div className="alert error">{localError}</div>}
        {slow && <div className="alert warning">Waking up the demo server. The first sign-in can take up to a minute.</div>}
        <form className="form-stack" onSubmit={submit}>
          <label>Username or email</label>
          <input value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} required />
          <label>Password</label>
          <input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required />
          <button className="primary-button" disabled={loading}>{loading ? 'Signing in...' : 'Open Dashboard'}</button>
        </form>
        <div className="demo-list">
          {demos.map(([username, password]) => (
            <button key={username} type="button" onClick={() => setForm({ username, password })}>
              {username}
            </button>
          ))}
        </div>
        <p className="auth-switch">No account? <Link to="/register">Register here</Link></p>
      </section>
    </main>
  );
}

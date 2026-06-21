import { useState } from 'react';
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
        <p>Connect your React frontend to the Java Tomcat backend with secure Bearer-token requests.</p>
      </section>

      <section className="auth-card">
        <div className="auth-icon"><LockKeyhole size={24} /></div>
        <h2>Sign in</h2>
        <p>Use a demo account or your registered campus profile.</p>
        {localError && <div className="alert error">{localError}</div>}
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

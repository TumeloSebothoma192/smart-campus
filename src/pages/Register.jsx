import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UserPlus } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';

export default function Register() {
  const [form, setForm] = useState({
    name: '',
    surname: '',
    role: 'student',
    studentNumber: '',
    staffNumber: '',
    password: '',
  });
  const [error, setError] = useState('');
  const { register, loading } = useAuth();
  const navigate = useNavigate();

  async function submit(event) {
    event.preventDefault();
    setError('');
    try {
      await register(form);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-hero">
        <div className="auth-brand"><UserPlus size={28} /> Smart Campus</div>
        <h1>Create a campus account with role-based access.</h1>
        <p>Students, lecturers, staff, managers, and admins receive different operational dashboards.</p>
      </section>

      <section className="auth-card wide">
        <h2>Register</h2>
        <p>The backend creates your profile and returns an access token.</p>
        {error && <div className="alert error">{error}</div>}
        <form className="form-grid" onSubmit={submit}>
          <label>First name<input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required /></label>
          <label>Surname<input value={form.surname} onChange={(event) => setForm({ ...form, surname: event.target.value })} required /></label>
          <label>Role
            <select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value })}>
              <option value="student">Student</option>
              <option value="lecturer">Lecturer</option>
              <option value="staff">Staff</option>
              <option value="admin">Admin</option>
            </select>
          </label>
          <label>Student number<input value={form.studentNumber} onChange={(event) => setForm({ ...form, studentNumber: event.target.value })} /></label>
          <label>Staff number<input value={form.staffNumber} onChange={(event) => setForm({ ...form, staffNumber: event.target.value })} /></label>
          <label>Password<input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required /></label>
          <button className="primary-button full-span" disabled={loading}>{loading ? 'Creating...' : 'Create Account'}</button>
        </form>
        <p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p>
      </section>
    </main>
  );
}

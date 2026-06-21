import { useEffect, useState } from 'react';
import { api, apiError, mock } from '../services/api';
import { useAuth } from '../context/AuthContext.jsx';
import { canTeach } from '../services/roles.js';

export default function Appointments() {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [form, setForm] = useState({ lecturerId: '3', moduleId: '1', date: '' });
  const [notice, setNotice] = useState('');
  const isLecturer = canTeach(user?.role);
  const canBook = user?.role === 'STUDENT';

  function load() {
    api.get(isLecturer ? '/api/appointments/lecturer/appointments' : '/api/appointments/myAppointments')
      .then((response) => setAppointments(response.data.appointments || []))
      .catch((err) => {
        setAppointments(mock.appointments);
        setNotice(`Showing fallback appointments: ${apiError(err)}`);
      });
  }

  useEffect(load, [isLecturer]);

  async function book(event) {
    event.preventDefault();
    try {
      await api.post('/api/appointments/book', form);
      setForm({ lecturerId: '3', moduleId: '1', date: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function respond(appointmentId, status) {
    try {
      await api.post('/api/appointments/respond', { appointmentId, status });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  return (
    <section className="page-grid">
      <PageTitle title="Appointments" text={isLecturer ? 'Review and respond to student appointment requests.' : 'Book appointments with lecturers for academic support.'} />
      {notice && <div className="alert warning">{notice}</div>}
      {canBook && (
        <form className="panel form-grid" onSubmit={book}>
          <label>Lecturer ID<input value={form.lecturerId} onChange={(e) => setForm({ ...form, lecturerId: e.target.value })} required /></label>
          <label>Module ID<input value={form.moduleId} onChange={(e) => setForm({ ...form, moduleId: e.target.value })} required /></label>
          <label>Date<input type="datetime-local" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} required /></label>
          <button className="primary-button">Book Appointment</button>
        </form>
      )}
      {!canBook && !isLecturer && <div className="panel"><h3>Appointment Access</h3><p>Your role can view appointment information but cannot create or respond to academic appointments.</p></div>}
      <Table columns={['ID', 'Module', 'Lecturer', 'Date', 'Status', '']} rows={appointments.map((item) => [
        item.id, item.moduleId, item.lecturerId, item.date, item.status,
        isLecturer ? <div className="row-actions"><button className="secondary-button" onClick={() => respond(item.id, 'accepted')}>Accept</button><button className="danger-button" onClick={() => respond(item.id, 'declined')}>Decline</button></div> : 'Pending lecturer',
      ])} />
    </section>
  );
}

function PageTitle({ title, text }) {
  return <div className="page-heading"><span>Academic</span><h2>{title}</h2><p>{text}</p></div>;
}

function Table({ columns, rows }) {
  return <div className="table-card"><table><thead><tr>{columns.map((c) => <th key={c}>{c}</th>)}</tr></thead><tbody>{rows.map((r, i) => <tr key={i}>{r.map((cell, j) => <td key={j}>{cell}</td>)}</tr>)}</tbody></table></div>;
}

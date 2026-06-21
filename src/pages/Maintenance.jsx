import { useEffect, useState } from 'react';
import { api, apiError, mock } from '../services/api';
import { useAuth } from '../context/AuthContext.jsx';

export default function Maintenance() {
  const { user } = useAuth();
  const [issues, setIssues] = useState([]);
  const [form, setForm] = useState({ roomName: '', issue: '' });
  const [notice, setNotice] = useState('');
  const isAdmin = user?.role === 'ADMIN';

  function load() {
    api.get(isAdmin ? '/api/maintenance/all' : '/api/maintenance/my-issues')
      .then((response) => setIssues(response.data.issues || []))
      .catch((err) => {
        setIssues(mock.issues);
        setNotice(`Showing fallback issues: ${apiError(err)}`);
      });
  }

  useEffect(load, [isAdmin]);

  async function submitIssue(event) {
    event.preventDefault();
    try {
      await api.post('/api/maintenance/submit', form);
      setForm({ roomName: '', issue: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function updateStatus(id, status) {
    try {
      await api.put(`/api/maintenance/status/${id}`, { status });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  return (
    <section className="page-grid">
      <PageTitle title="Maintenance Issues" text="Submit maintenance issues and update progress." />
      {notice && <div className="alert warning">{notice}</div>}
      <form className="panel form-grid" onSubmit={submitIssue}>
        <label>Room name<input value={form.roomName} onChange={(e) => setForm({ ...form, roomName: e.target.value })} required /></label>
        <label className="full-span">Issue<textarea value={form.issue} onChange={(e) => setForm({ ...form, issue: e.target.value })} required /></label>
        <button className="primary-button full-span">Submit Issue</button>
      </form>
      <Table columns={['Room', 'Issue', 'Status', 'Created', '']} rows={issues.map((item) => [
        item.roomName, item.issue, item.status, item.createdAt,
        isAdmin ? <select value={item.status} onChange={(e) => updateStatus(item.id, e.target.value)}><option>pending</option><option>in progress</option><option>resolved</option></select> : 'View only',
      ])} />
    </section>
  );
}

function PageTitle({ title, text }) {
  return <div className="page-heading"><span>Operations</span><h2>{title}</h2><p>{text}</p></div>;
}

function Table({ columns, rows }) {
  return <div className="table-card"><table><thead><tr>{columns.map((c) => <th key={c}>{c}</th>)}</tr></thead><tbody>{rows.map((r, i) => <tr key={i}>{r.map((cell, j) => <td key={j}>{cell}</td>)}</tr>)}</tbody></table></div>;
}

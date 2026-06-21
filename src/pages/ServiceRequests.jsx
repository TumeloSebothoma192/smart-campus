import { useEffect, useState } from 'react';
import { api, apiError, mock } from '../services/api';
import { useAuth } from '../context/AuthContext.jsx';

export default function ServiceRequests() {
  const { user } = useAuth();
  const [lookups, setLookups] = useState({ categories: [], departments: [], statuses: [], priorities: [] });
  const [requests, setRequests] = useState([]);
  const [form, setForm] = useState({ title: '', description: '', category: 'IT Support', department: 'ICT Department', priority: 'MEDIUM', status: 'PENDING' });
  const [notice, setNotice] = useState('');
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'DEPARTMENT_MANAGER' || user?.role === 'LECTURER';

  function load() {
    Promise.all([
      api.get('/lookups'),
      api.get(isAdmin ? '/request/get/all' : '/request/get/mine'),
    ])
      .then(([lookupResponse, requestResponse]) => {
        setLookups(lookupResponse.data);
        setRequests(requestResponse.data.requests || []);
      })
      .catch((err) => {
        setRequests(mock.serviceRequests);
        setLookups({
          categories: [{ name: 'IT Support' }, { name: 'Maintenance' }],
          departments: [{ name: 'ICT Department' }, { name: 'Facilities & Maintenance' }],
          statuses: ['PENDING', 'IN_PROGRESS', 'RESOLVED'],
          priorities: ['LOW', 'MEDIUM', 'HIGH', 'URGENT'],
        });
        setNotice(`Showing fallback service requests: ${apiError(err)}`);
      });
  }

  useEffect(load, [isAdmin]);

  async function create(event) {
    event.preventDefault();
    try {
      await api.post('/request/store', form);
      setForm({ ...form, title: '', description: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function update(request, status) {
    try {
      await api.put('/request/update', { ...request, status });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function remove(id) {
    try {
      await api.delete(`/request/delete/${id}`);
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  return (
    <section className="page-grid">
      <PageTitle title="Service Requests" text="Original Java request API for campus service support." />
      {notice && <div className="alert warning">{notice}</div>}
      <form className="panel form-grid" onSubmit={create}>
        <label>Title<input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></label>
        <label>Priority<select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>{lookups.priorities.map((item) => <option key={item}>{item}</option>)}</select></label>
        <label>Category<select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>{lookups.categories.map((item) => <option key={item.name}>{item.name}</option>)}</select></label>
        <label>Department<select value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })}>{lookups.departments.map((item) => <option key={item.name}>{item.name}</option>)}</select></label>
        <label className="full-span">Description<textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} required /></label>
        <button className="primary-button full-span">Submit Request</button>
      </form>
      <Table columns={['Title', 'Department', 'Priority', 'Status', 'Created', '']} rows={requests.map((item) => [
        item.title, item.department, item.priority, item.status, item.createdAt,
        isAdmin ? <div className="row-actions"><select value={item.status} onChange={(e) => update(item, e.target.value)}>{lookups.statuses.map((status) => <option key={status}>{status}</option>)}</select><button className="danger-button" onClick={() => remove(item.requestId)}>Delete</button></div> : 'Submitted',
      ])} />
    </section>
  );
}

function PageTitle({ title, text }) {
  return <div className="page-heading"><span>Support Desk</span><h2>{title}</h2><p>{text}</p></div>;
}

function Table({ columns, rows }) {
  return <div className="table-card"><table><thead><tr>{columns.map((c) => <th key={c}>{c}</th>)}</tr></thead><tbody>{rows.map((r, i) => <tr key={i}>{r.map((cell, j) => <td key={j}>{cell}</td>)}</tr>)}</tbody></table></div>;
}

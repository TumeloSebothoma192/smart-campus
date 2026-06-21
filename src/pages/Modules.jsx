import { useEffect, useState } from 'react';
import { api, apiError, mock } from '../services/api';
import { useAuth } from '../context/AuthContext.jsx';
import { canAdmin, canTeach } from '../services/roles.js';

export default function Modules() {
  const { user } = useAuth();
  const isAdmin = canAdmin(user?.role);
  const teacher = canTeach(user?.role);
  const [modules, setModules] = useState([]);
  const [form, setForm] = useState({ moduleName: '', description: '', subjectCode: '' });
  const [content, setContent] = useState({ moduleId: '1', title: '', body: '' });
  const [notice, setNotice] = useState('');

  function load() {
    api.get('/api/modules/all-modules')
      .then((response) => setModules(response.data.modules || []))
      .catch((err) => {
        setModules(mock.modules);
        setNotice(`Showing fallback modules: ${apiError(err)}`);
      });
  }

  useEffect(load, []);

  async function createModule(event) {
    event.preventDefault();
    try {
      await api.post('/api/modules/create', form);
      setForm({ moduleName: '', description: '', subjectCode: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function registerModule(id) {
    try {
      await api.post('/api/modules/register', { moduleId: id });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function addContent(event) {
    event.preventDefault();
    try {
      await api.post('/api/modules/add/content', content);
      setContent({ moduleId: content.moduleId, title: '', body: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function deleteModule(id) {
    try {
      await api.delete(`/api/modules/${id}`);
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  return (
    <section className="page-grid">
      <PageTitle title="Modules" text={isAdmin ? 'Create modules and manage academic structure.' : teacher ? 'Add teaching content and view registered modules.' : 'Register for modules and view learning content.'} />
      {notice && <div className="alert warning">{notice}</div>}
      <div className="two-column">
        {isAdmin && (
          <form className="panel form-stack" onSubmit={createModule}>
            <h3>Create Module</h3>
            <label>Module name<input value={form.moduleName} onChange={(e) => setForm({ ...form, moduleName: e.target.value })} required /></label>
            <label>Subject code<input value={form.subjectCode} onChange={(e) => setForm({ ...form, subjectCode: e.target.value })} required /></label>
            <label>Description<textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} required /></label>
            <button className="primary-button">Create</button>
          </form>
        )}
        {teacher && (
          <form className="panel form-stack" onSubmit={addContent}>
            <h3>Add Content</h3>
            <label>Module ID<input value={content.moduleId} onChange={(e) => setContent({ ...content, moduleId: e.target.value })} required /></label>
            <label>Title<input value={content.title} onChange={(e) => setContent({ ...content, title: e.target.value })} required /></label>
            <label>Body<textarea value={content.body} onChange={(e) => setContent({ ...content, body: e.target.value })} required /></label>
            <button className="primary-button">Add Content</button>
          </form>
        )}
        {!isAdmin && !teacher && (
          <div className="panel">
            <h3>Student Module View</h3>
            <p>Register for available modules and use this page as your learning hub.</p>
          </div>
        )}
      </div>
      <Table columns={['ID', 'Module', 'Code', 'Description', 'Registered', '']} rows={modules.map((item) => [
        item.id, item.moduleName, item.subjectCode, item.description, item.registeredUsers || 'None',
        <div className="row-actions">
          {!isAdmin && <button className="secondary-button" onClick={() => registerModule(item.id)}>Register</button>}
          {isAdmin && <button className="danger-button" onClick={() => deleteModule(item.id)}>Delete</button>}
        </div>,
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

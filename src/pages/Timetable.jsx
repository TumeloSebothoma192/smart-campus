import { useEffect, useState } from 'react';
import { api, apiError, mock } from '../services/api';

export default function Timetable() {
  const [timetable, setTimetable] = useState([]);
  const [form, setForm] = useState({ moduleId: '1', day: 'Monday', startTime: '09:00', endTime: '11:00', roomName: '' });
  const [notice, setNotice] = useState('');

  function load() {
    api.get('/api/timetables/myTimetable')
      .then((response) => setTimetable(response.data.timetable || []))
      .catch((err) => {
        setTimetable(mock.timetable);
        setNotice(`Showing fallback timetable: ${apiError(err)}`);
      });
  }

  useEffect(load, []);

  async function create(event) {
    event.preventDefault();
    try {
      await api.post(`/api/timetables/modules/${form.moduleId}`, form);
      setForm({ moduleId: '1', day: 'Monday', startTime: '09:00', endTime: '11:00', roomName: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function remove(id) {
    try {
      await api.delete(`/api/timetables/${id}`);
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  return (
    <section className="page-grid">
      <PageTitle title="Timetable" text="Create and view module timetable sessions." />
      {notice && <div className="alert warning">{notice}</div>}
      <form className="panel form-grid" onSubmit={create}>
        <label>Module ID<input value={form.moduleId} onChange={(e) => setForm({ ...form, moduleId: e.target.value })} required /></label>
        <label>Day<select value={form.day} onChange={(e) => setForm({ ...form, day: e.target.value })}><option>Monday</option><option>Tuesday</option><option>Wednesday</option><option>Thursday</option><option>Friday</option><option>Saturday</option></select></label>
        <label>Start<input type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} /></label>
        <label>End<input type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} /></label>
        <label className="full-span">Room<input value={form.roomName} onChange={(e) => setForm({ ...form, roomName: e.target.value })} required /></label>
        <button className="primary-button full-span">Create Timetable</button>
      </form>
      <Table columns={['Module', 'Day', 'Time', 'Room', '']} rows={timetable.map((item) => [
        item.moduleId, item.day, `${item.startTime} - ${item.endTime}`, item.roomName,
        <button className="danger-button" onClick={() => remove(item.id)}>Delete</button>,
      ])} />
    </section>
  );
}

function PageTitle({ title, text }) {
  return <div className="page-heading"><span>Academic Schedule</span><h2>{title}</h2><p>{text}</p></div>;
}

function Table({ columns, rows }) {
  return <div className="table-card"><table><thead><tr>{columns.map((c) => <th key={c}>{c}</th>)}</tr></thead><tbody>{rows.map((r, i) => <tr key={i}>{r.map((cell, j) => <td key={j}>{cell}</td>)}</tr>)}</tbody></table></div>;
}

import { useEffect, useState } from 'react';
import { api, apiError, mock } from '../services/api';
import { useAuth } from '../context/AuthContext.jsx';
import { canAdmin } from '../services/roles.js';

export default function Rooms() {
  const { user } = useAuth();
  const isAdmin = canAdmin(user?.role);
  const [rooms, setRooms] = useState([]);
  const [form, setForm] = useState({ name: '', capacity: '', type: 'lab', building: '', equipment: '' });
  const [notice, setNotice] = useState('');

  function load() {
    api.get('/api/rooms')
      .then((response) => setRooms(response.data.rooms || []))
      .catch((err) => {
        setRooms(mock.rooms);
        setNotice(`Showing fallback rooms: ${apiError(err)}`);
      });
  }

  useEffect(load, []);

  async function createRoom(event) {
    event.preventDefault();
    try {
      await api.post('/api/rooms', form);
      setForm({ name: '', capacity: '', type: 'lab', building: '', equipment: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function deleteRoom(id) {
    try {
      await api.delete(`/api/rooms/${id}`);
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  return (
    <section className="page-grid">
      <PageTitle title="Rooms" text={isAdmin ? 'Create, view, and manage campus rooms.' : 'Browse available campus spaces for learning and bookings.'} />
      {notice && <div className="alert warning">{notice}</div>}
      {isAdmin && (
        <form className="panel form-grid" onSubmit={createRoom}>
          <label>Room name<input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></label>
          <label>Capacity<input value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} required /></label>
          <label>Type<select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}><option>lab</option><option>lecture-hall</option><option>meeting-room</option><option>study-room</option></select></label>
          <label>Building<input value={form.building} onChange={(e) => setForm({ ...form, building: e.target.value })} required /></label>
          <label className="full-span">Equipment<input value={form.equipment} onChange={(e) => setForm({ ...form, equipment: e.target.value })} /></label>
          <button className="primary-button full-span">Create Room</button>
        </form>
      )}
      <DataTable columns={isAdmin ? ['Name', 'Type', 'Building', 'Capacity', 'Available', ''] : ['Name', 'Type', 'Building', 'Capacity', 'Available']} rows={rooms.map((room) => isAdmin ? [
        room.name, room.type, room.building, room.capacity, room.isAvailable,
        <button className="danger-button" onClick={() => deleteRoom(room.id)}>Delete</button>,
      ] : [room.name, room.type, room.building, room.capacity, room.isAvailable])} />
    </section>
  );
}

function PageTitle({ title, text }) {
  return <div className="page-heading"><span>Campus Resource</span><h2>{title}</h2><p>{text}</p></div>;
}

function DataTable({ columns, rows }) {
  return (
    <div className="table-card">
      <table>
        <thead><tr>{columns.map((column) => <th key={column}>{column}</th>)}</tr></thead>
        <tbody>{rows.map((row, index) => <tr key={index}>{row.map((cell, i) => <td key={i}>{cell}</td>)}</tr>)}</tbody>
      </table>
    </div>
  );
}

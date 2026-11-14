import { useEffect, useState } from 'react';
import { api, apiError } from '../services/api';

export default function Bookings() {
  const [bookings, setBookings] = useState([]);
  const [roomId, setRoomId] = useState('');
  const [rooms, setRooms] = useState([]);
  const [form, setForm] = useState({ startTime: '', endTime: '', purpose: '' });
  const [notice, setNotice] = useState('');

  function load() {
    api.get('/api/bookings/my-bookings')
      .then((response) => setBookings(response.data.bookings || []))
      .catch((err) => {
        setBookings([]);
        setNotice(`Could not load bookings: ${apiError(err)}`);
      });
  }

  useEffect(load, []);
  // Pick a room from the list instead of typing its ID.
  useEffect(() => {
    api.get('/api/rooms')
      .then((response) => {
        const list = response.data.rooms || [];
        setRooms(list);
        if (list.length) setRoomId((current) => current || String(list[0].id));
      })
      .catch(() => {});
  }, []);

  async function createBooking(event) {
    event.preventDefault();
    try {
      await api.post(`/api/bookings/room/${roomId}`, form);
      setForm({ startTime: '', endTime: '', purpose: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function cancelBooking(id) {
    try {
      await api.put(`/api/bookings/${id}/cancel`);
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  return (
    <section className="page-grid">
      <PageTitle title="Room Bookings" text="Book rooms and manage your reservations." />
      {notice && <div className="alert warning">{notice}</div>}
      <form className="panel form-grid" onSubmit={createBooking}>
        <label>Room
          {rooms.length ? (
            <select value={roomId} onChange={(e) => setRoomId(e.target.value)} required>
              {rooms.map((room) => <option key={room.id} value={room.id}>{room.name}{room.building ? ` (${room.building})` : ''}</option>)}
            </select>
          ) : (
            <input value={roomId} onChange={(e) => setRoomId(e.target.value)} placeholder="Room ID" required />
          )}
        </label>
        <label>Start time<input type="datetime-local" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} required /></label>
        <label>End time<input type="datetime-local" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} required /></label>
        <label className="full-span">Purpose<input value={form.purpose} onChange={(e) => setForm({ ...form, purpose: e.target.value })} required /></label>
        <button className="primary-button full-span">Create Booking</button>
      </form>
      <Table columns={['Room', 'Start', 'End', 'Purpose', 'Status', '']} rows={bookings.map((item) => [
        item.roomName || item.roomId, item.startTime, item.endTime, item.purpose, item.status,
        <button className="secondary-button" onClick={() => cancelBooking(item.id)}>Cancel</button>,
      ])} />
    </section>
  );
}

function PageTitle({ title, text }) {
  return <div className="page-heading"><span>Campus Resource</span><h2>{title}</h2><p>{text}</p></div>;
}

function Table({ columns, rows }) {
  return <div className="table-card"><table><thead><tr>{columns.map((c) => <th key={c}>{c}</th>)}</tr></thead><tbody>{rows.map((r, i) => <tr key={i}>{r.map((cell, j) => <td key={j}>{cell}</td>)}</tr>)}</tbody></table></div>;
}

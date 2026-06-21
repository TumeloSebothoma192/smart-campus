import { useEffect, useState } from 'react';
import { api, apiError, mock } from '../services/api';

export default function Bookings() {
  const [bookings, setBookings] = useState([]);
  const [roomId, setRoomId] = useState('1');
  const [form, setForm] = useState({ startTime: '', endTime: '', purpose: '' });
  const [notice, setNotice] = useState('');

  function load() {
    api.get('/api/bookings/my-bookings')
      .then((response) => setBookings(response.data.bookings || []))
      .catch((err) => {
        setBookings(mock.bookings);
        setNotice(`Showing fallback bookings: ${apiError(err)}`);
      });
  }

  useEffect(load, []);

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
        <label>Room ID<input value={roomId} onChange={(e) => setRoomId(e.target.value)} required /></label>
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

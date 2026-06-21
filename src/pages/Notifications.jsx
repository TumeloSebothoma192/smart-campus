import { useEffect, useState } from 'react';
import { api, apiError, mock } from '../services/api';

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [form, setForm] = useState({ userId: '', content: '', module: '' });
  const [edit, setEdit] = useState({ notificationId: '', newContent: '' });
  const [notice, setNotice] = useState('');

  function load() {
    api.get('/api/notifications/getNotifications')
      .then((response) => setNotifications(response.data.notifications || []))
      .catch((err) => {
        setNotifications(mock.notifications);
        setNotice(`Showing fallback notifications: ${apiError(err)}`);
      });
  }

  useEffect(load, []);

  async function send(event) {
    event.preventDefault();
    try {
      await api.post('/api/notifications/sendNotification', form);
      setForm({ userId: '', content: '', module: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function markRead(id) {
    try {
      await api.patch(`/api/notifications/markAsRead/${id}`);
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  async function update(event) {
    event.preventDefault();
    try {
      await api.put('/api/notifications/updateNotification', edit);
      setEdit({ notificationId: '', newContent: '' });
      load();
    } catch (err) {
      setNotice(apiError(err));
    }
  }

  return (
    <section className="page-grid">
      <PageTitle title="Notifications" text="Send, edit, and mark campus notifications as read." />
      {notice && <div className="alert warning">{notice}</div>}
      <div className="two-column">
        <form className="panel form-stack" onSubmit={send}>
          <h3>Send Notification</h3>
          <label>User ID<input value={form.userId} onChange={(e) => setForm({ ...form, userId: e.target.value })} /></label>
          <label>Module<input value={form.module} onChange={(e) => setForm({ ...form, module: e.target.value })} /></label>
          <label>Content<textarea value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} required /></label>
          <button className="primary-button">Send</button>
        </form>
        <form className="panel form-stack" onSubmit={update}>
          <h3>Edit Notification</h3>
          <label>Notification ID<input value={edit.notificationId} onChange={(e) => setEdit({ ...edit, notificationId: e.target.value })} required /></label>
          <label>New content<textarea value={edit.newContent} onChange={(e) => setEdit({ ...edit, newContent: e.target.value })} required /></label>
          <button className="secondary-button">Update</button>
        </form>
      </div>
      <div className="card-grid">
        {notifications.map((item) => (
          <article className="mini-card" key={item.id}>
            <span>{item.read === 'true' ? 'Read' : 'Unread'}</span>
            <h3>{item.content}</h3>
            <p>{item.module || 'General campus notice'}</p>
            <button className="secondary-button" onClick={() => markRead(item.id)}>Mark as Read</button>
          </article>
        ))}
      </div>
    </section>
  );
}

function PageTitle({ title, text }) {
  return <div className="page-heading"><span>Communications</span><h2>{title}</h2><p>{text}</p></div>;
}

import { useEffect, useState } from 'react';
import { BarChart3, BookOpen, CalendarCheck, DoorOpen, Wrench, Bell, UsersRound, Clock } from 'lucide-react';
import { api, apiError, mock } from '../services/api';
import StatCard from '../components/StatCard.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { profileFor } from '../services/roles.js';

export default function Dashboard({ adminOnly = false }) {
  const { user } = useAuth();
  const profile = profileFor(user?.role);
  const [stats, setStats] = useState(mock.stats);
  const [notice, setNotice] = useState('');

  useEffect(() => {
    api.get('/api/admin/stats')
      .then((response) => {
        setStats(response.data);
        setNotice('');
      })
      .catch((err) => setNotice(`Showing fallback stats because backend is offline: ${apiError(err)}`));
  }, []);

  const allCards = {
    Users: ['Users', stats.users, 'registered campus profiles', <UsersRound />],
    Rooms: ['Rooms', stats.rooms, 'managed learning spaces', <DoorOpen />],
    Bookings: ['Bookings', stats.bookings, 'room reservations', <CalendarCheck />],
    Modules: ['Modules', stats.modules, 'academic modules', <BookOpen />],
    Maintenance: ['Maintenance', stats.maintenance, 'reported issues', <Wrench />],
    Appointments: ['Appointments', stats.appointments, 'student lecturer meetings', <Clock />],
    Notifications: ['Notifications', stats.notifications, 'campus messages', <Bell />],
    Timetables: ['Timetables', stats.timetables, 'scheduled sessions', <BarChart3 />],
  };
  const cards = profile.cards.map((name) => allCards[name]).filter(Boolean);

  return (
    <section className={`page-grid role-dashboard role-${profile.tone}`}>
      <div className="role-hero">
        <div>
          <span>{adminOnly ? 'Admin Stats' : profile.label}</span>
          <h2>{adminOnly ? 'System Administration Overview' : profile.headline}</h2>
          <p>{profile.intro}</p>
        </div>
        <div className="focus-list">
          {profile.focus.map((item) => <b key={item}>{item}</b>)}
        </div>
      </div>
      {notice && <div className="alert warning">{notice}</div>}
      <div className="stat-grid">
        {cards.map(([title, value, detail, icon]) => (
          <StatCard key={title} title={title} value={value ?? 0} detail={detail} icon={icon} />
        ))}
      </div>
      <div className="role-workspace">
        <article className="dashboard-panel">
          <h3>{workspaceTitle(user?.role)}</h3>
          <p>{workspaceCopy(user?.role)}</p>
        </article>
        <article className="dashboard-panel">
          <h3>Allowed Workflows</h3>
          <ul className="workflow-list">
            {workflows(user?.role).map((item) => <li key={item}>{item}</li>)}
          </ul>
        </article>
      </div>
    </section>
  );
}

function workspaceTitle(role) {
  if (role === 'ADMIN') return 'Full System Control';
  if (role === 'LECTURER') return 'Academic Delivery';
  if (role === 'DEPARTMENT_MANAGER') return 'Department Queue';
  if (role === 'STAFF') return 'Staff Operations';
  return 'Student Self-Service';
}

function workspaceCopy(role) {
  if (role === 'ADMIN') return 'This dashboard emphasizes platform-wide visibility and management actions.';
  if (role === 'LECTURER') return 'This workspace emphasizes modules, appointments, teaching notices, and timetable access.';
  if (role === 'DEPARTMENT_MANAGER') return 'This view is designed for resolving requests and tracking operational pressure.';
  if (role === 'STAFF') return 'This portal keeps daily campus work simple: bookings, requests, notices, and schedules.';
  return 'This dashboard focuses on student actions: modules, study room bookings, appointments, support, and timetable.';
}

function workflows(role) {
  if (role === 'ADMIN') return ['Create rooms and modules', 'View admin statistics', 'Resolve maintenance and service requests', 'Manage timetables'];
  if (role === 'LECTURER') return ['Add module content', 'Respond to appointments', 'Send notifications', 'View academic timetable'];
  if (role === 'DEPARTMENT_MANAGER') return ['Review service requests', 'Update maintenance status', 'Track room and booking pressure', 'Communicate updates'];
  if (role === 'STAFF') return ['Book rooms', 'Submit service requests', 'Report maintenance issues', 'Read campus notifications'];
  return ['Register modules', 'Book study rooms', 'Book lecturer appointments', 'Track service and maintenance requests'];
}

import { NavLink } from 'react-router-dom';
import {
  Bell,
  BookOpen,
  CalendarCheck,
  DoorOpen,
  LayoutDashboard,
  ListChecks,
  MessageSquareWarning,
  School,
  UserRound,
  Wrench,
  BarChart3,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { profileFor } from '../services/roles.js';

const links = [
  { id: 'dashboard', to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { id: 'rooms', to: '/rooms', label: 'Rooms', icon: DoorOpen },
  { id: 'bookings', to: '/bookings', label: 'Room Bookings', icon: CalendarCheck },
  { id: 'modules', to: '/modules', label: 'Modules', icon: BookOpen },
  { id: 'maintenance', to: '/maintenance', label: 'Maintenance', icon: Wrench },
  { id: 'appointments', to: '/appointments', label: 'Appointments', icon: School },
  { id: 'notifications', to: '/notifications', label: 'Notifications', icon: Bell },
  { id: 'timetable', to: '/timetable', label: 'Timetable', icon: ListChecks },
  { id: 'requests', to: '/requests', label: 'Service Requests', icon: MessageSquareWarning },
  { id: 'admin-stats', to: '/admin-stats', label: 'Admin Stats', icon: BarChart3 },
  { id: 'profile', to: '/profile', label: 'Profile', icon: UserRound },
];

export default function Sidebar() {
  const { user } = useAuth();
  const profile = profileFor(user?.role);
  const visibleLinks = links.filter((link) => profile.links.includes(link.id));

  return (
    <aside className={`sidebar role-${profile.tone}`}>
      <div className="brand">
        <div className="brand-mark">SC</div>
        <div>
          <strong>Smart Campus</strong>
          <span>{profile.label} Portal</span>
        </div>
      </div>

      <div className="role-card">
        <span>{profile.label}</span>
        <strong>{profile.headline}</strong>
      </div>

      <nav className="sidebar-nav">
        {visibleLinks.map(({ to, label, icon: Icon }) => (
          <NavLink key={to} to={to} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            <Icon size={18} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

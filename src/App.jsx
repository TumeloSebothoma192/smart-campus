import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Layout from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Rooms from './pages/Rooms.jsx';
import Bookings from './pages/Bookings.jsx';
import Modules from './pages/Modules.jsx';
import Maintenance from './pages/Maintenance.jsx';
import Appointments from './pages/Appointments.jsx';
import Notifications from './pages/Notifications.jsx';
import Timetable from './pages/Timetable.jsx';
import ServiceRequests from './pages/ServiceRequests.jsx';
import Profile from './pages/Profile.jsx';
import NotFound from './pages/NotFound.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/rooms" element={<Rooms />} />
          <Route path="/bookings" element={<Bookings />} />
          <Route path="/modules" element={<Modules />} />
          <Route path="/maintenance" element={<Maintenance />} />
          <Route path="/appointments" element={<Appointments />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/timetable" element={<Timetable />} />
          <Route path="/requests" element={<ServiceRequests />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/admin-stats" element={<Dashboard adminOnly />} />
        </Route>
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

import { LogOut, Search, Server } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { profileFor } from '../services/roles.js';

export default function Navbar() {
  const { user, logout } = useAuth();
  const profile = profileFor(user?.role);

  return (
    <header className={`navbar role-${profile.tone}`}>
      <div>
        <p>{profile.label} workspace</p>
        <h1>{user?.fullName || 'Campus User'}</h1>
      </div>
      <div className="navbar-actions">
        <div className="search-box">
          <Search size={16} />
          <input placeholder="Search campus data" />
        </div>
        <span className="api-pill"><Server size={15} /> Tomcat API</span>
        <span className="role-pill">{user?.role || 'USER'}</span>
        <button className="icon-button" type="button" onClick={logout} aria-label="Sign out">
          <LogOut size={18} />
        </button>
      </div>
    </header>
  );
}

import { useEffect, useState } from 'react';
import { api, apiError } from '../services/api';
import { useAuth } from '../context/AuthContext.jsx';

export default function Profile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(user);
  const [notice, setNotice] = useState('');

  useEffect(() => {
    api.get('/api/users/profile')
      .then((response) => setProfile(response.data))
      .catch((err) => {
        setProfile(user);
        setNotice(`Showing local profile: ${apiError(err)}`);
      });
  }, [user]);

  return (
    <section className="page-grid">
      <div className="page-heading">
        <span>Account</span>
        <h2>Profile</h2>
        <p>Your authenticated campus profile from the Java backend.</p>
      </div>
      {notice && <div className="alert warning">{notice}</div>}
      <div className="profile-card">
        <div className="avatar">{profile?.fullName?.slice(0, 2).toUpperCase() || 'SC'}</div>
        <div>
          <h3>{profile?.fullName}</h3>
          <p>{profile?.email}</p>
          <div className="profile-grid">
            <span>Role <b>{profile?.role}</b></span>
            <span>Number <b>{profile?.number}</b></span>
            <span>Phone <b>{profile?.phone || 'Not provided'}</b></span>
          </div>
        </div>
      </div>
    </section>
  );
}

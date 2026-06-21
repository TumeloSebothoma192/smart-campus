import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <main className="not-found">
      <h1>404</h1>
      <p>The campus page you requested does not exist.</p>
      <Link className="primary-button" to="/dashboard">Back to Dashboard</Link>
    </main>
  );
}

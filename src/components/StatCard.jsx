export default function StatCard({ title, value, detail, icon }) {
  return (
    <article className="stat-card">
      <div>
        <p>{title}</p>
        <strong>{value}</strong>
        <span>{detail}</span>
      </div>
      <div className="stat-icon">{icon}</div>
    </article>
  );
}

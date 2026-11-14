# Smart Campus Management System

A campus service management system built in an Agile team at Tshwane University of Technology. Students, lecturers, staff and managers each get their own dashboard for logging and tracking campus service requests.

**Live demo:** [portfolio-six-bice-5lh3it3mor.vercel.app](https://portfolio-six-bice-5lh3it3mor.vercel.app) · **Case study:** [tumelosebothoma.vercel.app/projects/smart-campus](https://tumelosebothoma.vercel.app/projects/smart-campus)

This repository is the **React + Vite frontend**. It talks to a Java EE backend (REST APIs over a SQL database).

## What I worked on

- Requirements analysis and system design with the team
- Frontend: role-based dashboards and request flows
- Integration with the Java EE REST backend
- Iterative delivery in Agile sprints with Git

## Stack

- **Frontend:** React, Vite, JavaScript
- **Backend (separate service):** Java EE, REST APIs, SQL
- **Deployment:** Vercel

## Demo accounts

These are seeded demo users for trying the app:

| Role | Email | Password |
|---|---|---|
| Admin | admin@campus.local | admin123 |
| Lecturer | lecturer@campus.local | lecturer123 |
| Manager | manager@campus.local | manager123 |
| Staff | staff@campus.local | staff123 |
| Student | student@campus.local | student123 |

## Run locally

```bash
npm install
npm run dev
```

Then open http://127.0.0.1:5173/. Point the app at the backend with:

```text
VITE_API_URL=http://localhost:8081/CampusServiceManagementSystemBackend/web
```

## Deploy

On Vercel: framework **Vite**, build command `npm run build`, output directory `dist`, and set `VITE_API_URL` to the hosted backend's `/web` URL.

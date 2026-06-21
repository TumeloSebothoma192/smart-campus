# Smart Campus Java Backend

Java REST backend for the Smart Campus system. It is based on your original Java backend, improved with the main API ideas from `09Jeanette/smart-campus-api`, but kept as a Java WAR project for NetBeans/Tomcat.

## Open In NetBeans

Open this folder:

```text
B:\Java\CampusServiceManagementSystem_ANT\CampusServiceManagementSystem_Backend
```

NetBeans can open it as a Maven WAR project because it has `pom.xml`.

Recommended setup:

```text
JDK: 17 or 21
Server: Apache Tomcat 10.1+
Context path: /CampusServiceManagementSystemBackend
```

Avoid Java 8. This project uses Jakarta/Tomcat 10 APIs.

## Run Locally

```powershell
powershell -ExecutionPolicy Bypass -File .\run-backend.ps1 -Port 8081
```

Backend API base URL:

```text
http://localhost:8081/CampusServiceManagementSystemBackend/web
```

Browser check URL:

```text
http://localhost:8081/CampusServiceManagementSystemBackend/web/health
http://localhost:8081/CampusServiceManagementSystemBackend/web/api/health
```

## Auth For Frontend

Login:

```http
POST /api/auth/login
```

Body:

```json
{
  "username": "admin@campus.local",
  "password": "admin123"
}
```

Use the returned token on protected frontend requests:

```http
Authorization: Bearer <token>
```

Demo accounts:

```text
admin@campus.local / admin123
lecturer@campus.local / lecturer123
manager@campus.local / manager123
staff@campus.local / staff123
student@campus.local / student123
```

## Smart Campus API

Frontend base URL:

```text
http://localhost:8081/CampusServiceManagementSystemBackend/web
```

Routes added from the reference API design:

```text
POST   /api/auth/register
POST   /api/auth/login
GET    /api/auth/me
GET    /api/users/profile

GET    /api/admin/stats

GET    /api/rooms
GET    /api/rooms/available
POST   /api/rooms
PUT    /api/rooms/{id}
DELETE /api/rooms/{id}

POST   /api/bookings/room/{roomId}
GET    /api/bookings/my-bookings
GET    /api/bookings/{bookingId}
PUT    /api/bookings/{bookingId}/cancel

POST   /api/modules/create
POST   /api/modules/register
POST   /api/modules/add/content
GET    /api/modules/view
GET    /api/modules/all-modules
DELETE /api/modules/{moduleId}

POST   /api/maintenance/submit
GET    /api/maintenance/my-issues
GET    /api/maintenance/all
PUT    /api/maintenance/status/{id}

POST   /api/appointments/book
POST   /api/appointments/respond
GET    /api/appointments/myAppointments
GET    /api/appointments/lecturer/appointments

POST   /api/notifications/sendNotification
GET    /api/notifications/getNotifications
PATCH  /api/notifications/markAsRead/{id}
PUT    /api/notifications/updateNotification

POST   /api/timetables/create
POST   /api/timetables/modules/{moduleId}
GET    /api/timetables/myTimetable
GET    /api/timetables/modules/{moduleId}
PUT    /api/timetables/{timetableId}
DELETE /api/timetables/{timetableId}
```

## Original Request API

These routes still work for the previous campus service request frontend:

```text
GET    /health
GET    /auth/me
GET    /lookups
GET    /request/get/all
GET    /request/get/mine
POST   /request/store
PUT    /request/update
DELETE /request/delete/{requestID}
```

Only `/health`, `/api/health`, `/api/auth/login`, and `/api/auth/register` are public. Other routes require Basic Auth or Bearer token auth.

## PostgreSQL

The backend now uses PostgreSQL when `DATABASE_URL` or `JDBC_DATABASE_URL` is set.

Local example:

```text
DATABASE_URL=postgres://user:password@localhost:5432/smart_campus
```

The app creates these tables automatically:

```text
campus_users
service_requests
smart_records
```

Without a database URL, it still runs with local in-memory seed data for development.

## Deploy Backend

Recommended hosting shape:

```text
Backend: Docker Java web service
Database: Hosted PostgreSQL
Required env var: DATABASE_URL
```

This project includes:

```text
Dockerfile
docker-entrypoint.sh
render.yaml
```

Expected hosted API URL format:

```text
https://YOUR_BACKEND_HOST/CampusServiceManagementSystemBackend/web
```

Set the frontend `VITE_API_URL` to that hosted `/web` URL.

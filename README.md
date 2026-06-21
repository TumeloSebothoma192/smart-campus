# Smart Campus Frontend

Fresh React + Vite frontend for the Smart Campus Java backend.

## Run

```powershell
npm install
npm run dev
```

Open:

```text
http://127.0.0.1:5173/
```

Backend API base:

```text
VITE_API_URL=http://localhost:8081/CampusServiceManagementSystemBackend/web
```

For Vercel, set `VITE_API_URL` to your hosted Java backend URL, for example:

```text
https://smart-campus-api.onrender.com/CampusServiceManagementSystemBackend/web
```

## Deploy To GitHub + Vercel

```powershell
git init
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_FRONTEND_REPO.git
git add .
git commit -m "Deploy Smart Campus frontend"
git push -u origin main
```

Then import the GitHub repo in Vercel.

Vercel settings:

```text
Framework: Vite
Build command: npm run build
Output directory: dist
Environment variable: VITE_API_URL=<hosted backend /web URL>
```

Demo login:

```text
admin@campus.local / admin123
lecturer@campus.local / lecturer123
manager@campus.local / manager123
staff@campus.local / staff123
student@campus.local / student123
```

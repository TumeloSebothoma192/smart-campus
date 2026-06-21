import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8081/CampusServiceManagementSystemBackend/web',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('smartCampusToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export function unwrap(response) {
  return response.data;
}

export function apiError(error) {
  const data = error?.response?.data;
  if (typeof data === 'string') {
    return data.replace(/<[^>]*>/g, '').replace(/\s+/g, ' ').trim();
  }
  return data?.message || error?.message || 'Backend request failed';
}

export const mock = {
  stats: {
    users: 5,
    rooms: 3,
    bookings: 1,
    modules: 2,
    maintenance: 2,
    appointments: 1,
    notifications: 2,
    timetables: 1,
  },
  rooms: [
    { id: '1', name: 'ICT Lab A1', capacity: '45', type: 'lab', building: 'Engineering Block', isAvailable: 'true', equipment: 'Projector, Computers, Wi-Fi' },
    { id: '2', name: 'Study Room B2', capacity: '12', type: 'study-room', building: 'Library', isAvailable: 'true', equipment: 'Whiteboard, Smart Screen' },
  ],
  bookings: [
    { id: '1', roomName: 'ICT Lab A1', startTime: '2026-06-25T09:00', endTime: '2026-06-25T11:00', purpose: 'Studying', status: 'active' },
  ],
  modules: [
    { id: '1', moduleName: 'Web Technologies', subjectCode: 'WEB202', description: 'Learn frontend, backend, and API integration.', registeredUsers: 'Student Demo' },
    { id: '2', moduleName: 'Network Security', subjectCode: 'NET301', description: 'Campus network defence fundamentals.', registeredUsers: '' },
  ],
  issues: [
    { id: '1', roomName: 'Study Room B2', issue: 'Lights not working', status: 'pending', createdAt: '2026-06-20 21:00' },
  ],
  appointments: [
    { id: '1', moduleId: '1', lecturerId: '3', date: '2026-06-26T13:00', status: 'pending' },
  ],
  notifications: [
    { id: '1', content: 'Welcome to Smart Campus. Your dashboard is ready.', read: 'false', module: '' },
  ],
  timetable: [
    { id: '1', moduleId: '1', day: 'Monday', startTime: '09:00', endTime: '11:00', roomName: 'ICT Lab A1' },
  ],
  serviceRequests: [
    { requestId: 1, title: 'Wi-Fi not working in lab', department: 'ICT Department', category: 'IT Support', priority: 'MEDIUM', status: 'PENDING', createdAt: '2026-06-20 21:56' },
  ],
};

export const roleProfiles = {
  ADMIN: {
    label: 'Administrator',
    tone: 'admin',
    headline: 'Campus Command Center',
    intro: 'Control users, rooms, modules, maintenance, timetables, and system-wide reporting.',
    focus: ['System governance', 'Resource control', 'Operational reporting'],
    links: ['dashboard', 'rooms', 'bookings', 'modules', 'maintenance', 'appointments', 'notifications', 'timetable', 'requests', 'admin-stats', 'profile'],
    cards: ['Users', 'Rooms', 'Bookings', 'Modules', 'Maintenance', 'Appointments', 'Notifications', 'Timetables'],
  },
  LECTURER: {
    label: 'Lecturer',
    tone: 'lecturer',
    headline: 'Teaching Workspace',
    intro: 'Manage module content, appointments, notifications, schedules, and learner support.',
    focus: ['Module content', 'Student appointments', 'Academic communication'],
    links: ['dashboard', 'rooms', 'bookings', 'modules', 'appointments', 'notifications', 'timetable', 'requests', 'profile'],
    cards: ['Modules', 'Appointments', 'Notifications', 'Timetables'],
  },
  DEPARTMENT_MANAGER: {
    label: 'Department Manager',
    tone: 'manager',
    headline: 'Department Operations Desk',
    intro: 'Track service queues, resolve campus issues, and keep department work moving.',
    focus: ['Request triage', 'Maintenance status', 'Department queue'],
    links: ['dashboard', 'rooms', 'bookings', 'maintenance', 'requests', 'notifications', 'profile'],
    cards: ['Maintenance', 'Bookings', 'Rooms', 'Notifications'],
  },
  STAFF: {
    label: 'Staff',
    tone: 'staff',
    headline: 'Staff Service Hub',
    intro: 'Book rooms, submit support requests, view notices, and follow campus schedules.',
    focus: ['Room access', 'Service requests', 'Campus notices'],
    links: ['dashboard', 'rooms', 'bookings', 'maintenance', 'notifications', 'timetable', 'requests', 'profile'],
    cards: ['Rooms', 'Bookings', 'Maintenance', 'Notifications'],
  },
  STUDENT: {
    label: 'Student',
    tone: 'student',
    headline: 'Student Campus Portal',
    intro: 'Book study spaces, register modules, request help, view appointments, and track your timetable.',
    focus: ['Learning access', 'Bookings', 'Support tracking'],
    links: ['dashboard', 'rooms', 'bookings', 'modules', 'maintenance', 'appointments', 'notifications', 'timetable', 'requests', 'profile'],
    cards: ['Rooms', 'Bookings', 'Modules', 'Appointments', 'Notifications', 'Timetables'],
  },
};

export function profileFor(role) {
  return roleProfiles[role] || roleProfiles.STUDENT;
}

export function canAdmin(role) {
  return role === 'ADMIN';
}

export function canManageRequests(role) {
  return role === 'ADMIN' || role === 'DEPARTMENT_MANAGER' || role === 'LECTURER';
}

export function canTeach(role) {
  return role === 'LECTURER' || role === 'ADMIN' || role === 'DEPARTMENT_MANAGER';
}

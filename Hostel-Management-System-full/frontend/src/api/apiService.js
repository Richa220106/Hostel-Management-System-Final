const API_BASE_URL = 'http://localhost:8081/api';

let currentUser = null;

export const setCurrentAuthUser = (user) => {
  currentUser = user;
  if (user) {
    localStorage.setItem('hostelhub_user', JSON.stringify(user));
  } else {
    localStorage.removeItem('hostelhub_user');
  }
};

export const getStoredAuthUser = () => {
  try {
    const stored = localStorage.getItem('hostelhub_user');
    if (stored) {
      currentUser = JSON.parse(stored);
      return currentUser;
    }
  } catch (e) {}
  return currentUser;
};

const getHeaders = () => {
  const headers = {
    'Content-Type': 'application/json'
  };
  if (currentUser) {
    headers['X-User-Id'] = String(currentUser.id);
    headers['X-User-Role'] = currentUser.role;
  }
  return headers;
};

export const api = {
  // Auth
  login: async (username, password) => {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await res.json();
    if (data.success && data.user) {
      setCurrentAuthUser(data.user);
    }
    return data;
  },

  getUsers: async () => {
    const res = await fetch(`${API_BASE_URL}/auth/users`, {
      headers: getHeaders()
    });
    return res.json();
  },

  // Complaints
  getComplaints: async () => {
    const res = await fetch(`${API_BASE_URL}/complaints`, {
      headers: getHeaders()
    });
    return res.json();
  },

  getComplaintById: async (id) => {
    const res = await fetch(`${API_BASE_URL}/complaints/${id}`, {
      headers: getHeaders()
    });
    return res.json();
  },

  createComplaint: async (complaintData) => {
    const res = await fetch(`${API_BASE_URL}/complaints`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({
        ...complaintData,
        studentId: currentUser?.id,
        roomNo: complaintData.roomNo || currentUser?.roomNo || 'A-203'
      })
    });
    return res.json();
  },

  updateComplaintStatus: async (id, status) => {
    const res = await fetch(`${API_BASE_URL}/complaints/${id}/status`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify({ status })
    });
    return res.json();
  },

  assignTechnician: async (id, technicianId) => {
    const res = await fetch(`${API_BASE_URL}/complaints/${id}/assign`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify({ technicianId })
    });
    return res.json();
  },

  // Rooms
  getRooms: async () => {
    const res = await fetch(`${API_BASE_URL}/rooms`, {
      headers: getHeaders()
    });
    return res.json();
  },

  addRoom: async (roomData) => {
    const res = await fetch(`${API_BASE_URL}/rooms`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(roomData)
    });
    return res.json();
  },

  allocateRoom: async (studentId, roomNo) => {
    const res = await fetch(`${API_BASE_URL}/rooms/allocate`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify({ studentId, roomNo })
    });
    return res.json();
  },

  // Visitors
  getVisitors: async () => {
    const res = await fetch(`${API_BASE_URL}/visitors`, {
      headers: getHeaders()
    });
    return res.json();
  },

  registerVisitor: async (visitorData) => {
    const res = await fetch(`${API_BASE_URL}/visitors`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({
        ...visitorData,
        studentId: currentUser?.id,
        roomNo: visitorData.roomNo || currentUser?.roomNo || 'A-203'
      })
    });
    return res.json();
  },

  updateVisitorStatus: async (visitorId, status) => {
    const res = await fetch(`${API_BASE_URL}/visitors/${visitorId}/status`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify({ status })
    });
    return res.json();
  },

  // Technicians
  getTechnicians: async () => {
    const res = await fetch(`${API_BASE_URL}/technicians`, {
      headers: getHeaders()
    });
    return res.json();
  },

  addTechnician: async (techData) => {
    const res = await fetch(`${API_BASE_URL}/technicians`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(techData)
    });
    return res.json();
  },

  updateTechnicianStatus: async (techId, status) => {
    const res = await fetch(`${API_BASE_URL}/technicians/${techId}/status`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify({ status })
    });
    return res.json();
  },

  // Notifications
  getNotifications: async () => {
    const res = await fetch(`${API_BASE_URL}/notifications`, {
      headers: getHeaders()
    });
    return res.json();
  },

  markNotificationRead: async (notificationId) => {
    const res = await fetch(`${API_BASE_URL}/notifications/${notificationId}/read`, {
      method: 'PUT',
      headers: getHeaders()
    });
    return res.json();
  },

  // Warden Stats
  getWardenStats: async () => {
    const res = await fetch(`${API_BASE_URL}/warden/stats`, {
      headers: getHeaders()
    });
    return res.json();
  },

  // Emergency
  triggerEmergency: async (emergencyData) => {
    const res = await fetch(`${API_BASE_URL}/emergency`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({
        ...emergencyData,
        roomNo: emergencyData?.roomNo || currentUser?.roomNo || 'A-203'
      })
    });
    return res.json();
  }
};

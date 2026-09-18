import React, { createContext, useContext, useState, useEffect } from 'react';
import { api, getStoredAuthUser, setCurrentAuthUser } from '../api/apiService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(getStoredAuthUser());
  const [unreadCount, setUnreadCount] = useState(0);
  const [activeTab, setActiveTab] = useState('dashboard'); // Navigation state

  useEffect(() => {
    refreshNotifications();
  }, [user]);

  const refreshNotifications = async () => {
    try {
      if (user) {
        const notifs = await api.getNotifications();
        if (Array.isArray(notifs)) {
          const unread = notifs.filter(n => !n.isRead).length;
          setUnreadCount(unread);
        }
      }
    } catch (e) {
      console.error('Failed to fetch notifications', e);
    }
  };

  const login = async (username, password) => {
    const res = await api.login(username, password);
    if (res.success && res.user) {
      setUser(res.user);
      setActiveTab('dashboard');
      return true;
    }
    return false;
  };

  const logout = () => {
    setCurrentAuthUser(null);
    setUser(null);
    setActiveTab('login');
  };

  // Switch demo persona instantly
  const switchRole = async (targetRole) => {
    if (targetRole === 'WARDEN') {
      const wardenUser = {
        id: 1,
        username: 'warden',
        role: 'WARDEN',
        name: 'Dr. K. Sharma (Chief Warden)',
        roomNo: 'Admin-01',
        contact: '+91 98765 43210',
        email: 'warden@hostelhub.edu'
      };
      setCurrentAuthUser(wardenUser);
      setUser(wardenUser);
      setActiveTab('dashboard');
    } else {
      const studentUser = {
        id: 2,
        username: 'student1',
        role: 'STUDENT',
        name: 'Aarav Patel',
        roomNo: 'A-203',
        contact: '+91 90118 48854',
        email: 'aarav.patel@hostelhub.edu'
      };
      setCurrentAuthUser(studentUser);
      setUser(studentUser);
      setActiveTab('dashboard');
    }
  };

  return (
    <AuthContext.Provider value={{
      user,
      activeTab,
      setActiveTab,
      unreadCount,
      refreshNotifications,
      login,
      logout,
      switchRole
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

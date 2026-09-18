import React, { useState } from 'react';
import { AuthProvider, useAuth } from './auth/AuthContext';
import { Sidebar } from './components/Sidebar';
import { Header } from './components/Header';
import { LoginScreen } from './auth/LoginScreen';

// Student Screens
import { StudentDashboard } from './student/StudentDashboard';
import { RaiseComplaintScreen } from './student/RaiseComplaintScreen';
import { MyComplaintsScreen } from './student/MyComplaintsScreen';
import { VisitorEntryScreen } from './student/VisitorEntryScreen';
import { EmergencyScreen } from './student/EmergencyScreen';
import { StudentProfileScreen } from './student/StudentProfileScreen';

// Warden Screens
import { WardenDashboard } from './warden/WardenDashboard';
import { RoomManagementScreen } from './warden/RoomManagementScreen';
import { TechnicianManagementScreen } from './warden/TechnicianManagementScreen';
import { VisitorManagementScreen } from './warden/VisitorManagementScreen';
import { WardenReportsScreen } from './warden/WardenReportsScreen';

// Common Screens
import { NotificationsScreen } from './components/NotificationsScreen';

import './styles/theme.css';

const MainLayout = () => {
  const { user, activeTab } = useAuth();
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  if (!user) {
    return <LoginScreen />;
  }

  const isWarden = user.role === 'WARDEN';

  const renderActiveScreen = () => {
    switch (activeTab) {
      // Common
      case 'notifications':
        return <NotificationsScreen />;

      // Student Tabs
      case 'dashboard':
        return isWarden ? <WardenDashboard /> : <StudentDashboard />;
      case 'my-room':
        return <StudentProfileScreen />;
      case 'raise-complaint':
        return <RaiseComplaintScreen />;
      case 'my-complaints':
        return <MyComplaintsScreen />;
      case 'visitor-entry':
        return <VisitorEntryScreen />;
      case 'emergency':
        return <EmergencyScreen />;
      case 'profile':
        return <StudentProfileScreen />;

      // Warden Tabs
      case 'warden-rooms':
        return <RoomManagementScreen />;
      case 'warden-complaints':
        return <WardenDashboard />;
      case 'warden-technicians':
        return <TechnicianManagementScreen />;
      case 'warden-visitors':
        return <VisitorManagementScreen />;
      case 'warden-emergencies':
        return <WardenDashboard />;
      case 'warden-reports':
        return <WardenReportsScreen />;

      default:
        return isWarden ? <WardenDashboard /> : <StudentDashboard />;
    }
  };

  return (
    <div className="app-container">
      <Sidebar 
        isMobileOpen={mobileSidebarOpen} 
        closeMobileSidebar={() => setMobileSidebarOpen(false)} 
      />

      <div className="main-content">
        <Header 
          onToggleMobileSidebar={() => setMobileSidebarOpen(!mobileSidebarOpen)} 
        />

        <main className="page-body">
          {renderActiveScreen()}
        </main>
      </div>
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <MainLayout />
    </AuthProvider>
  );
}

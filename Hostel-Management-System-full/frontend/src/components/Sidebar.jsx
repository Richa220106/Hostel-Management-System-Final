import React from 'react';
import { useAuth } from '../auth/AuthContext';
import { 
  Building2, 
  Home, 
  PlusCircle, 
  FileText, 
  Users, 
  AlertTriangle, 
  Bell, 
  User, 
  Wrench, 
  BarChart3, 
  DoorClosed, 
  LogOut,
  ShieldCheck,
  CheckSquare
} from 'lucide-react';

export const Sidebar = ({ isMobileOpen, closeMobileSidebar }) => {
  const { user, activeTab, setActiveTab, unreadCount, logout } = useAuth();
  const isWarden = user?.role === 'WARDEN';

  const handleNav = (tabKey) => {
    setActiveTab(tabKey);
    if (closeMobileSidebar) closeMobileSidebar();
  };

  return (
    <>
      {/* Backdrop for Mobile */}
      {isMobileOpen && (
        <div 
          onClick={closeMobileSidebar}
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(15, 23, 42, 0.6)',
            zIndex: 40,
            backdropFilter: 'blur(2px)'
          }}
        />
      )}

      <aside className={`hostelhub-sidebar ${isMobileOpen ? 'open' : ''}`}>
        {/* Brand Header */}
        <div className="sidebar-brand">
          <div className="brand-logo-icon">
            <Building2 size={22} color="#ffffff" />
          </div>
          <div className="brand-text">
            <div className="brand-title">HostelHub</div>
            <div className="brand-tagline">Stay Safe • Stay Connected</div>
          </div>
        </div>

        {/* Navigation Section */}
        <div className="sidebar-nav">
          <div className="nav-group-title">MAIN</div>
          <button 
            className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}
            onClick={() => handleNav('dashboard')}
          >
            <Home size={18} />
            <span>Dashboard</span>
          </button>

          {!isWarden ? (
            /* STUDENT NAVIGATION */
            <>
              <div className="nav-group-title">STUDENT SERVICES</div>
              
              <button 
                className={`nav-item ${activeTab === 'my-room' ? 'active' : ''}`}
                onClick={() => handleNav('my-room')}
              >
                <DoorClosed size={18} />
                <span>My Room</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'raise-complaint' ? 'active' : ''}`}
                onClick={() => handleNav('raise-complaint')}
              >
                <PlusCircle size={18} />
                <span>Raise Complaint</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'my-complaints' ? 'active' : ''}`}
                onClick={() => handleNav('my-complaints')}
              >
                <FileText size={18} />
                <span>My Complaints</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'visitor-entry' ? 'active' : ''}`}
                onClick={() => handleNav('visitor-entry')}
              >
                <Users size={18} />
                <span>Visitor Entry</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'emergency' ? 'active' : ''}`}
                onClick={() => handleNav('emergency')}
              >
                <AlertTriangle size={18} color="#f87171" />
                <span style={{ color: '#f87171', fontWeight: 600 }}>Emergency</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'notifications' ? 'active' : ''}`}
                onClick={() => handleNav('notifications')}
              >
                <Bell size={18} />
                <span>Notifications</span>
                {unreadCount > 0 && (
                  <span className="nav-badge">{unreadCount}</span>
                )}
              </button>

              <button 
                className={`nav-item ${activeTab === 'profile' ? 'active' : ''}`}
                onClick={() => handleNav('profile')}
              >
                <User size={18} />
                <span>Profile</span>
              </button>
            </>
          ) : (
            /* WARDEN NAVIGATION */
            <>
              <div className="nav-group-title">WARDEN MANAGEMENT</div>
              
              <button 
                className={`nav-item ${activeTab === 'warden-rooms' ? 'active' : ''}`}
                onClick={() => handleNav('warden-rooms')}
              >
                <DoorClosed size={18} />
                <span>Rooms</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'warden-complaints' ? 'active' : ''}`}
                onClick={() => handleNav('warden-complaints')}
              >
                <CheckSquare size={18} />
                <span>Complaints</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'warden-technicians' ? 'active' : ''}`}
                onClick={() => handleNav('warden-technicians')}
              >
                <Wrench size={18} />
                <span>Technicians</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'warden-visitors' ? 'active' : ''}`}
                onClick={() => handleNav('warden-visitors')}
              >
                <Users size={18} />
                <span>Visitors</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'warden-emergencies' ? 'active' : ''}`}
                onClick={() => handleNav('warden-emergencies')}
              >
                <AlertTriangle size={18} color="#f87171" />
                <span>Emergencies</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'warden-reports' ? 'active' : ''}`}
                onClick={() => handleNav('warden-reports')}
              >
                <BarChart3 size={18} />
                <span>Reports</span>
              </button>

              <button 
                className={`nav-item ${activeTab === 'notifications' ? 'active' : ''}`}
                onClick={() => handleNav('notifications')}
              >
                <Bell size={18} />
                <span>Notifications</span>
                {unreadCount > 0 && (
                  <span className="nav-badge">{unreadCount}</span>
                )}
              </button>
            </>
          )}
        </div>

        {/* User Card in Footer */}
        <div className="sidebar-footer">
          <div className="user-profile-summary">
            <div className="user-avatar-circle">
              {user?.name?.charAt(0) || 'U'}
            </div>
            <div className="user-info-text">
              <div className="user-name">{user?.name}</div>
              <div className="user-role-label">
                {isWarden ? 'Chief Warden' : `Room ${user?.roomNo || 'A-203'}`}
              </div>
            </div>
          </div>
          <button className="logout-btn" onClick={logout} title="Sign Out">
            <LogOut size={16} />
          </button>
        </div>
      </aside>

      <style>{`
        .hostelhub-sidebar {
          width: var(--sidebar-width);
          min-width: var(--sidebar-width);
          flex-shrink: 0;
          background-color: var(--sidebar-bg);
          height: 100vh;
          position: sticky;
          top: 0;
          left: 0;
          display: flex;
          flex-direction: column;
          z-index: 50;
          border-right: 1px solid var(--sidebar-border);
          transition: transform 0.25s ease;
        }

        .sidebar-brand {
          padding: 16px 14px;
          display: flex;
          align-items: center;
          gap: 10px;
          border-bottom: 1px solid var(--sidebar-border);
        }

        .brand-logo-icon {
          width: 34px;
          height: 34px;
          background: linear-gradient(135deg, #2563eb, #1d4ed8);
          border-radius: var(--radius-sm);
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 10px rgba(37, 99, 235, 0.3);
        }

        .brand-title {
          color: #ffffff;
          font-size: 1.05rem;
          font-weight: 800;
          letter-spacing: -0.02em;
        }

        .brand-tagline {
          color: var(--sidebar-header);
          font-size: 0.68rem;
          font-weight: 500;
          letter-spacing: 0.02em;
        }

        .sidebar-nav {
          flex: 1;
          padding: 12px 8px;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 3px;
        }

        .nav-group-title {
          font-size: 0.64rem;
          font-weight: 700;
          color: var(--sidebar-header);
          letter-spacing: 0.08em;
          padding: 10px 10px 4px;
          text-transform: uppercase;
        }

        .nav-item {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 8px 10px;
          border-radius: var(--radius-sm);
          color: var(--sidebar-text);
          background: transparent;
          border: none;
          font-size: 0.82rem; /* ~13px */
          font-weight: 500;
          cursor: pointer;
          transition: all 0.16s ease;
          width: 100%;
          text-align: left;
        }

        .nav-item:hover {
          color: #ffffff;
          background: var(--sidebar-hover);
        }

        .nav-item.active {
          background: var(--accent-blue);
          color: #ffffff;
          font-weight: 600;
          box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
        }

        .nav-badge {
          margin-left: auto;
          background: #ef4444;
          color: #ffffff;
          font-size: 0.68rem;
          font-weight: 700;
          padding: 1px 6px;
          border-radius: var(--radius-full);
        }

        .sidebar-footer {
          padding: 12px 14px;
          border-top: 1px solid var(--sidebar-border);
          display: flex;
          align-items: center;
          justify-content: space-between;
          background: rgba(15, 23, 42, 0.6);
        }

        .user-profile-summary {
          display: flex;
          align-items: center;
          gap: 8px;
          overflow: hidden;
        }

        .user-avatar-circle {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: #2563eb;
          color: #ffffff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 0.85rem;
          flex-shrink: 0;
        }

        .user-info-text {
          overflow: hidden;
        }

        .user-name {
          color: #ffffff;
          font-size: 0.8rem;
          font-weight: 600;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .user-role-label {
          color: var(--sidebar-header);
          font-size: 0.68rem;
          font-weight: 500;
        }

        .logout-btn {
          background: transparent;
          border: none;
          color: var(--sidebar-header);
          padding: 8px;
          border-radius: var(--radius-sm);
          cursor: pointer;
          transition: all 0.15s ease;
        }

        .logout-btn:hover {
          color: #f87171;
          background: rgba(239, 68, 68, 0.1);
        }

        @media (max-width: 768px) {
          .hostelhub-sidebar {
            position: fixed;
            top: 0;
            left: 0;
            height: 100vh;
            transform: translateX(-100%);
          }
          .hostelhub-sidebar.open {
            transform: translateX(0);
          }
        }
      `}</style>
    </>
  );
};

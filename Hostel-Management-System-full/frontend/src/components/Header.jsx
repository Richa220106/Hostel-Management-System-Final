import React, { useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { Search, Bell, Menu, Shield, UserCircle, CheckCircle2, AlertCircle } from 'lucide-react';

export const Header = ({ onToggleMobileSidebar }) => {
  const { user, switchRole, unreadCount, setActiveTab } = useAuth();
  const [showRoleMenu, setShowRoleMenu] = useState(false);
  const isWarden = user?.role === 'WARDEN';

  return (
    <header className="hostelhub-header">
      {/* Left: Mobile Toggle & Search */}
      <div className="header-left">
        <button 
          className="mobile-menu-btn" 
          onClick={onToggleMobileSidebar}
          aria-label="Toggle navigation"
        >
          <Menu size={20} />
        </button>

        <div className="search-bar-wrapper">
          <Search size={18} className="search-icon" />
          <input 
            type="text" 
            placeholder="Search anything (complaints, rooms, visitors)..." 
            className="search-input"
          />
        </div>
      </div>

      {/* Right: Quick Role Switcher, Notifications, User Profile */}
      <div className="header-right">
        {/* Role Demo Switcher */}
        <div className="role-switch-container">
          <button 
            className={`role-switch-pill ${isWarden ? 'warden-mode' : 'student-mode'}`}
            onClick={() => switchRole(isWarden ? 'STUDENT' : 'WARDEN')}
            title="Quick switch between Student and Warden views for demo"
          >
            <Shield size={14} />
            <span>Viewing as: <strong>{isWarden ? 'Warden' : 'Student (Aarav)'}</strong></span>
            <span className="switch-hint">Switch</span>
          </button>
        </div>

        {/* Notifications Icon */}
        <button 
          className="icon-circle-btn" 
          onClick={() => setActiveTab('notifications')}
          title="Notifications"
        >
          <Bell size={19} color="#0f172a" />
          {unreadCount > 0 && (
            <span className="notification-ping">
              {unreadCount}
            </span>
          )}
        </button>

        {/* User Card */}
        <div className="header-user-badge">
          <div className="avatar-small">
            {user?.name?.charAt(0) || 'U'}
          </div>
          <div className="header-user-meta">
            <div className="user-name-title">{user?.name}</div>
            <div className="user-role-subtitle">
              {isWarden ? 'Hostel Warden' : `Student • ${user?.roomNo || 'A-203'}`}
            </div>
          </div>
        </div>
      </div>

      <style>{`
        .hostelhub-header {
          height: var(--header-height);
          background: #ffffff;
          border-bottom: 1px solid var(--border-light);
          padding: 0 22px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          position: sticky;
          top: 0;
          z-index: 30;
        }

        .header-left {
          display: flex;
          align-items: center;
          gap: 12px;
          flex: 1;
          max-width: 380px;
        }

        .mobile-menu-btn {
          display: none;
          background: transparent;
          border: 1px solid var(--border-light);
          padding: 6px;
          border-radius: var(--radius-sm);
          cursor: pointer;
          color: var(--text-main);
        }

        .search-bar-wrapper {
          display: flex;
          align-items: center;
          gap: 8px;
          background: var(--bg-main);
          border: 1px solid var(--border-light);
          border-radius: var(--radius-full);
          padding: 6px 14px;
          width: 100%;
          transition: border-color 0.15s ease, box-shadow 0.15s ease;
        }

        .search-bar-wrapper:focus-within {
          border-color: var(--accent-blue);
          box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.12);
          background: #ffffff;
        }

        .search-icon {
          color: var(--text-muted);
          flex-shrink: 0;
        }

        .search-input {
          border: none;
          background: transparent;
          outline: none;
          font-size: 0.8125rem; /* 13px */
          color: var(--text-main);
          width: 100%;
        }

        .search-input::placeholder {
          color: var(--text-light);
        }

        .header-right {
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .role-switch-pill {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 4px 10px;
          border-radius: var(--radius-full);
          font-size: 0.74rem;
          cursor: pointer;
          border: 1px solid transparent;
          transition: all 0.15s ease;
        }

        .role-switch-pill.student-mode {
          background: #eff6ff;
          color: #1e40af;
          border-color: #bfdbfe;
        }

        .role-switch-pill.student-mode:hover {
          background: #dbeafe;
        }

        .role-switch-pill.warden-mode {
          background: #fdf4ff;
          color: #86198f;
          border-color: #f5d0fe;
        }

        .role-switch-pill.warden-mode:hover {
          background: #fae8ff;
        }

        .switch-hint {
          font-size: 0.68rem;
          text-decoration: underline;
          opacity: 0.8;
          margin-left: 3px;
        }

        .icon-circle-btn {
          width: 34px;
          height: 34px;
          border-radius: 50%;
          background: var(--bg-main);
          border: 1px solid var(--border-light);
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          position: relative;
          transition: all 0.15s ease;
        }

        .icon-circle-btn:hover {
          background: var(--border-light);
        }

        .notification-ping {
          position: absolute;
          top: -2px;
          right: -2px;
          background: #ef4444;
          color: #ffffff;
          font-size: 0.64rem;
          font-weight: 700;
          padding: 1px 5px;
          border-radius: var(--radius-full);
          border: 2px solid #ffffff;
        }

        .header-user-badge {
          display: flex;
          align-items: center;
          gap: 8px;
          padding-left: 8px;
          border-left: 1px solid var(--border-light);
        }

        .avatar-small {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: linear-gradient(135deg, #3b82f6, #1d4ed8);
          color: #ffffff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 0.82rem;
        }

        .header-user-meta {
          line-height: 1.2;
        }

        .user-name-title {
          font-size: 0.8rem; /* 13px */
          font-weight: 700;
          color: var(--text-main);
        }

        .user-role-subtitle {
          font-size: 0.68rem; /* 11px */
          color: var(--text-muted);
        }

        @media (max-width: 768px) {
          .hostelhub-header {
            padding: 0 16px;
          }
          .mobile-menu-btn {
            display: flex;
          }
          .search-bar-wrapper {
            display: none;
          }
          .role-switch-pill span {
            display: none;
          }
          .header-user-meta {
            display: none;
          }
        }
      `}</style>
    </header>
  );
};

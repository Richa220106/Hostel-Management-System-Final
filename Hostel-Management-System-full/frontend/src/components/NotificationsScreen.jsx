import React, { useState, useEffect, useMemo } from 'react';
import { api } from '../api/apiService';
import { useAuth } from '../auth/AuthContext';
import { 
  Bell, 
  Check, 
  CheckCheck, 
  Clock, 
  Wrench, 
  Users, 
  AlertTriangle, 
  CheckCircle2, 
  Sparkles,
  Filter,
  ArrowUpRight,
  ShieldAlert
} from 'lucide-react';

export const NotificationsScreen = () => {
  const { user, refreshNotifications, setActiveTab } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState('ALL'); // ALL, UNREAD, EMERGENCY, COMPLAINT, VISITOR

  useEffect(() => {
    loadNotifications();
  }, [user]);

  const loadNotifications = async () => {
    setLoading(true);
    try {
      const data = await api.getNotifications();
      if (Array.isArray(data)) setNotifications(data);
    } catch (e) {
      console.error('Failed to load notifications', e);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkRead = async (id, e) => {
    if (e) e.stopPropagation();
    try {
      await api.markNotificationRead(id);
      loadNotifications();
      refreshNotifications();
    } catch (e) {
      console.error(e);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      for (const n of notifications.filter(item => !item.isRead)) {
        await api.markNotificationRead(n.id);
      }
      loadNotifications();
      refreshNotifications();
    } catch (e) {
      console.error(e);
    }
  };

  // Helper to format timestamps to relative or clean format
  const formatTime = (dateStr) => {
    if (!dateStr) return 'Just now';
    try {
      const date = new Date(dateStr);
      const now = new Date();
      const diffMs = now - date;
      const diffMins = Math.floor(diffMs / (1000 * 60));
      const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays === 1) return 'Yesterday';
      if (diffDays < 7) return `${diffDays}d ago`;
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch {
      return dateStr;
    }
  };

  // Parse raw message into structured content with high-clarity title, badge & subtitle
  const parseNotification = (n) => {
    const raw = n.message || '';
    const lower = raw.toLowerCase();
    
    let category = 'COMPLAINT';
    let badgeLabel = 'Update';
    let badgeClass = 'badge-blue';
    let icon = <Wrench size={16} />;
    let title = raw;
    let subtitle = '';

    // Remove repetitive prefix if present
    const cleanMsg = raw.replace(/^Hostel Update:\s*/i, '');

    if (lower.includes('emergency')) {
      category = 'EMERGENCY';
      badgeLabel = 'Emergency';
      badgeClass = 'badge-red';
      icon = <ShieldAlert size={16} />;

      if (cleanMsg.includes('ASSIGNED')) {
        const matchTech = cleanMsg.match(/Technician\s+([^,]+?)\s+assigned/i);
        const matchId = cleanMsg.match(/Complaint\s+#(\d+)/i);
        title = matchTech ? `Emergency: Technician ${matchTech[1]} Dispatched` : 'Emergency Response Dispatched';
        subtitle = matchId ? `Complaint #${matchId[1]} • Priority: Immediate Attention` : cleanMsg;
      } else if (cleanMsg.includes('RESOLVED')) {
        const matchId = cleanMsg.match(/Complaint\s+#(\d+)/i);
        title = matchId ? `Emergency Complaint #${matchId[1]} Resolved` : 'Emergency Issue Resolved';
        subtitle = 'Situation has been verified and safety protocol concluded.';
      } else {
        title = cleanMsg;
        subtitle = 'Critical hostel alert';
      }
    } else if (lower.includes('resolved')) {
      category = 'RESOLVED';
      badgeLabel = 'Resolved';
      badgeClass = 'badge-green';
      icon = <CheckCircle2 size={16} />;

      const matchId = cleanMsg.match(/Complaint\s+#(\d+)/i);
      title = matchId ? `Complaint #${matchId[1]} Resolved & Closed` : 'Maintenance Complaint Resolved';
      subtitle = 'Inspection completed and signed off by hostel operations.';
    } else if (cleanMsg.includes('ASSIGNED')) {
      category = 'ASSIGNED';
      badgeLabel = 'Assigned';
      badgeClass = 'badge-blue';
      icon = <Wrench size={16} />;

      const matchTech = cleanMsg.match(/Technician\s+([^,]+?)\s+assigned/i);
      const matchDept = cleanMsg.match(/\((.*?)\)/);
      const matchId = cleanMsg.match(/Complaint\s+#(\d+)/i);

      if (matchTech && matchId) {
        title = `Technician ${matchTech[1]} Assigned`;
        subtitle = `Complaint #${matchId[1]}${matchDept ? ` • ${matchDept[1]}` : ''}`;
      } else {
        title = cleanMsg;
        subtitle = 'Technician dispatched for maintenance task';
      }
    } else if (lower.includes('visitor')) {
      category = 'VISITOR';
      badgeLabel = 'Visitor';
      badgeClass = 'badge-purple';
      icon = <Users size={16} />;
      title = cleanMsg;
      subtitle = 'Hostel Gate Entry Pass';
    } else {
      category = 'GENERAL';
      badgeLabel = 'HostelHub';
      badgeClass = 'badge-slate';
      icon = <Sparkles size={16} />;
      title = cleanMsg;
      subtitle = 'Central System Notification';
    }

    return { category, badgeLabel, badgeClass, icon, title, subtitle };
  };

  // Filtered notifications
  const unreadCount = notifications.filter(n => !n.isRead).length;

  const filteredNotifications = useMemo(() => {
    return notifications.filter(n => {
      if (activeFilter === 'UNREAD') return !n.isRead;
      if (activeFilter === 'EMERGENCY') {
        const lower = (n.message || '').toLowerCase();
        return n.type === 'EMERGENCY' || lower.includes('emergency');
      }
      if (activeFilter === 'COMPLAINT') {
        const lower = (n.message || '').toLowerCase();
        return lower.includes('complaint') || lower.includes('technician');
      }
      if (activeFilter === 'VISITOR') {
        const lower = (n.message || '').toLowerCase();
        return n.type === 'VISITOR' || lower.includes('visitor');
      }
      return true;
    });
  }, [notifications, activeFilter]);

  return (
    <div className="notifs-container">
      {/* Top Header Card */}
      <div className="notifs-header-card">
        <div className="notifs-header-left">
          <div className="header-icon-wrap">
            <Bell size={22} color="#2563eb" />
          </div>
          <div>
            <div className="header-title-flex">
              <h1 className="notifs-title">Notifications Center</h1>
              {unreadCount > 0 && (
                <span className="unread-counter-pill">{unreadCount} Unread</span>
              )}
            </div>
            <p className="notifs-subtitle">
              Live automated system events delivered via Observer Pattern for hostel complaints, technicians, emergencies, and visitors.
            </p>
          </div>
        </div>

        <div className="notifs-header-actions">
          <button 
            className="btn btn-secondary mark-all-btn"
            onClick={handleMarkAllRead}
            disabled={unreadCount === 0}
            title={unreadCount === 0 ? 'All caught up' : 'Mark all notifications as read'}
          >
            <CheckCheck size={16} />
            <span>Mark all as read</span>
          </button>
        </div>
      </div>

      {/* Filter Navigation Tabs */}
      <div className="notifs-filter-bar">
        <button 
          className={`filter-tab ${activeFilter === 'ALL' ? 'active' : ''}`}
          onClick={() => setActiveFilter('ALL')}
        >
          All
          <span className="filter-count">{notifications.length}</span>
        </button>

        <button 
          className={`filter-tab ${activeFilter === 'UNREAD' ? 'active' : ''}`}
          onClick={() => setActiveFilter('UNREAD')}
        >
          Unread
          {unreadCount > 0 && <span className="filter-count unread">{unreadCount}</span>}
        </button>

        <button 
          className={`filter-tab ${activeFilter === 'EMERGENCY' ? 'active' : ''}`}
          onClick={() => setActiveFilter('EMERGENCY')}
        >
          🚨 Emergencies
        </button>

        <button 
          className={`filter-tab ${activeFilter === 'COMPLAINT' ? 'active' : ''}`}
          onClick={() => setActiveFilter('COMPLAINT')}
        >
          🔧 Complaints & Techs
        </button>

        <button 
          className={`filter-tab ${activeFilter === 'VISITOR' ? 'active' : ''}`}
          onClick={() => setActiveFilter('VISITOR')}
        >
          👥 Visitors
        </button>
      </div>

      {/* Notifications List Card */}
      <div className="card notifs-card-list">
        {loading ? (
          <div className="notifs-empty-state">
            <div className="loading-spinner" />
            <p>Loading notification feed...</p>
          </div>
        ) : filteredNotifications.length === 0 ? (
          <div className="notifs-empty-state">
            <div className="empty-icon-circle">
              <CheckCircle2 size={32} color="#16a34a" />
            </div>
            <h3 className="empty-title">All caught up!</h3>
            <p className="empty-desc">
              {activeFilter === 'ALL' 
                ? 'No notifications found in your hostel activity history.' 
                : `No notifications match the "${activeFilter.toLowerCase()}" filter.`}
            </p>
          </div>
        ) : (
          <div className="notifications-feed">
            {filteredNotifications.map(n => {
              const { category, badgeLabel, badgeClass, icon, title, subtitle } = parseNotification(n);

              return (
                <div 
                  key={n.id} 
                  className={`notif-item-row ${n.isRead ? 'is-read' : 'is-unread'}`}
                  onClick={() => !n.isRead && handleMarkRead(n.id)}
                >
                  {/* Category Icon */}
                  <div className={`notif-category-icon ${badgeClass}`}>
                    {icon}
                  </div>

                  {/* Body Text */}
                  <div className="notif-body">
                    <div className="notif-meta-line">
                      <span className={`notif-badge-chip ${badgeClass}`}>{badgeLabel}</span>
                      <span className="notif-time">
                        <Clock size={12} />
                        {formatTime(n.createdAt)}
                      </span>
                      {!n.isRead && <span className="unread-dot" title="Unread notification" />}
                    </div>

                    <h4 className="notif-main-title">{title}</h4>
                    {subtitle && <p className="notif-sub-desc">{subtitle}</p>}
                  </div>

                  {/* Right Action */}
                  <div className="notif-action-wrap">
                    {!n.isRead ? (
                      <button 
                        className="mark-single-read-btn"
                        onClick={(e) => handleMarkRead(n.id, e)}
                        title="Mark as Read"
                      >
                        <Check size={14} />
                        <span>Mark read</span>
                      </button>
                    ) : (
                      <span className="read-status-badge">
                        <CheckCheck size={14} />
                        <span>Read</span>
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <style>{`
        .notifs-container {
          display: flex;
          flex-direction: column;
          gap: 20px;
          max-width: 100%;
          margin: 0 auto;
        }

        /* Top Header Card */
        .notifs-header-card {
          background: #ffffff;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-lg);
          padding: 24px 28px;
          box-shadow: var(--shadow-card);
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 24px;
        }

        .notifs-header-left {
          display: flex;
          align-items: center;
          gap: 18px;
        }

        .header-icon-wrap {
          width: 48px;
          height: 48px;
          border-radius: var(--radius-md);
          background: #eff6ff;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }

        .header-title-flex {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 4px;
        }

        .notifs-title {
          font-size: 1.5rem;
          font-weight: 800;
          color: var(--text-main);
          letter-spacing: -0.02em;
        }

        .unread-counter-pill {
          background: #eff6ff;
          color: #2563eb;
          border: 1px solid #bfdbfe;
          font-size: 0.76rem;
          font-weight: 700;
          padding: 2px 10px;
          border-radius: var(--radius-full);
        }

        .notifs-subtitle {
          font-size: 0.88rem;
          color: var(--text-muted);
          max-width: 680px;
          line-height: 1.4;
        }

        .mark-all-btn {
          white-space: nowrap;
          font-size: 0.84rem;
        }

        /* Filter Tab Bar */
        .notifs-filter-bar {
          display: flex;
          align-items: center;
          gap: 8px;
          overflow-x: auto;
          padding-bottom: 2px;
        }

        .filter-tab {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          padding: 8px 16px;
          border-radius: var(--radius-full);
          font-size: 0.84rem;
          font-weight: 600;
          color: var(--text-muted);
          background: #ffffff;
          border: 1px solid var(--border-light);
          cursor: pointer;
          transition: all 0.15s ease;
          white-space: nowrap;
        }

        .filter-tab:hover {
          color: var(--text-main);
          border-color: #cbd5e1;
          background: #f8fafc;
        }

        .filter-tab.active {
          background: var(--text-main);
          color: #ffffff;
          border-color: var(--text-main);
        }

        .filter-count {
          background: #f1f5f9;
          color: var(--text-main);
          font-size: 0.72rem;
          padding: 1px 7px;
          border-radius: var(--radius-full);
          font-weight: 700;
        }

        .filter-tab.active .filter-count {
          background: rgba(255, 255, 255, 0.2);
          color: #ffffff;
        }

        .filter-count.unread {
          background: #ef4444;
          color: #ffffff;
        }

        /* Notifications Feed Card */
        .notifs-card-list {
          padding: 0;
          overflow: hidden;
          background: #ffffff;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-lg);
          box-shadow: var(--shadow-card);
        }

        .notifications-feed {
          display: flex;
          flex-direction: column;
        }

        .notif-item-row {
          display: flex;
          align-items: flex-start;
          gap: 16px;
          padding: 18px 24px;
          border-bottom: 1px solid #f1f5f9;
          transition: background-color 0.15s ease;
          position: relative;
          cursor: pointer;
        }

        .notif-item-row:last-child {
          border-bottom: none;
        }

        .notif-item-row:hover {
          background-color: #f8fafc;
        }

        /* Unread vs Read visual styling */
        .notif-item-row.is-unread {
          background-color: #fbfdff;
          border-left: 3.5px solid var(--accent-blue);
        }

        .notif-item-row.is-unread:hover {
          background-color: #f1f7ff;
        }

        .notif-item-row.is-read {
          opacity: 0.82;
        }

        .notif-item-row.is-read .notif-main-title {
          font-weight: 600;
          color: #334155;
        }

        /* Category Icon */
        .notif-category-icon {
          width: 40px;
          height: 40px;
          border-radius: var(--radius-md);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          margin-top: 2px;
        }

        .notif-category-icon.badge-red {
          background: #fee2e2;
          color: #dc2626;
        }

        .notif-category-icon.badge-green {
          background: #dcfce7;
          color: #16a34a;
        }

        .notif-category-icon.badge-blue {
          background: #eff6ff;
          color: #2563eb;
        }

        .notif-category-icon.badge-purple {
          background: #f3e8ff;
          color: #9333ea;
        }

        .notif-category-icon.badge-slate {
          background: #f1f5f9;
          color: #475569;
        }

        /* Body Details */
        .notif-body {
          flex: 1;
          min-width: 0;
        }

        .notif-meta-line {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 5px;
        }

        .notif-badge-chip {
          font-size: 0.72rem;
          font-weight: 700;
          padding: 2px 8px;
          border-radius: var(--radius-full);
          text-transform: uppercase;
          letter-spacing: 0.03em;
        }

        .notif-badge-chip.badge-red {
          background: #fee2e2;
          color: #991b1b;
        }

        .notif-badge-chip.badge-green {
          background: #dcfce7;
          color: #166534;
        }

        .notif-badge-chip.badge-blue {
          background: #eff6ff;
          color: #1e40af;
        }

        .notif-badge-chip.badge-purple {
          background: #f3e8ff;
          color: #6b21a8;
        }

        .notif-badge-chip.badge-slate {
          background: #f1f5f9;
          color: #334155;
        }

        .notif-time {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          font-size: 0.74rem;
          color: var(--text-muted);
          font-weight: 500;
        }

        .unread-dot {
          width: 7px;
          height: 7px;
          border-radius: 50%;
          background: var(--accent-blue);
          display: inline-block;
        }

        .notif-main-title {
          font-size: 0.94rem;
          font-weight: 700;
          color: var(--text-main);
          line-height: 1.35;
          margin-bottom: 2px;
        }

        .notif-sub-desc {
          font-size: 0.8rem;
          color: var(--text-muted);
          line-height: 1.4;
        }

        /* Right Action Column */
        .notif-action-wrap {
          display: flex;
          align-items: center;
          flex-shrink: 0;
          margin-top: 4px;
        }

        .mark-single-read-btn {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          background: #ffffff;
          border: 1px solid var(--border-light);
          padding: 6px 12px;
          border-radius: var(--radius-sm);
          font-size: 0.78rem;
          font-weight: 600;
          color: var(--text-main);
          cursor: pointer;
          transition: all 0.15s ease;
        }

        .mark-single-read-btn:hover {
          background: var(--accent-blue);
          color: #ffffff;
          border-color: var(--accent-blue);
        }

        .read-status-badge {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          font-size: 0.76rem;
          color: #94a3b8;
          font-weight: 600;
        }

        /* Empty State */
        .notifs-empty-state {
          padding: 72px 24px;
          text-align: center;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
        }

        .empty-icon-circle {
          width: 64px;
          height: 64px;
          border-radius: 50%;
          background: #f0fdf4;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 16px;
        }

        .empty-title {
          font-size: 1.15rem;
          font-weight: 800;
          color: var(--text-main);
          margin-bottom: 6px;
        }

        .empty-desc {
          font-size: 0.86rem;
          color: var(--text-muted);
          max-width: 360px;
        }

        .loading-spinner {
          width: 32px;
          height: 32px;
          border: 3px solid #e2e8f0;
          border-top-color: var(--accent-blue);
          border-radius: 50%;
          animation: spin 0.8s linear infinite;
          margin-bottom: 16px;
        }

        @keyframes spin {
          to { transform: rotate(360deg); }
        }

        @media (max-width: 768px) {
          .notifs-header-card {
            flex-direction: column;
            align-items: flex-start;
            padding: 20px;
          }
          .notif-item-row {
            padding: 16px;
          }
          .mark-single-read-btn span {
            display: none;
          }
        }
      `}</style>
    </div>
  );
};

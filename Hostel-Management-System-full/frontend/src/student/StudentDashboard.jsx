import React, { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/apiService';
import { StatusBadge } from '../components/StatusBadge';
import { 
  PlusCircle, 
  Users, 
  AlertTriangle, 
  DoorClosed, 
  Wrench, 
  Bell, 
  CheckCircle, 
  Clock, 
  ArrowRight,
  Sparkles,
  Zap,
  Droplets,
  UtensilsCrossed
} from 'lucide-react';

export const StudentDashboard = () => {
  const { user, setActiveTab } = useAuth();
  const [complaints, setComplaints] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStudentData();
  }, [user]);

  const loadStudentData = async () => {
    setLoading(true);
    try {
      const [cData, nData] = await Promise.all([
        api.getComplaints(),
        api.getNotifications()
      ]);
      if (Array.isArray(cData)) setComplaints(cData);
      if (Array.isArray(nData)) setNotifications(nData);
    } catch (err) {
      console.error('Failed to load student dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  const activeComplaints = complaints.filter(c => c.status !== 'RESOLVED');
  const latestComplaint = complaints.length > 0 ? complaints[0] : null;

  return (
    <div className="student-dashboard">
      {/* Top Welcome Banner with Hostel Illustration */}
      <div className="welcome-banner-card">
        <div className="banner-content">
          <div className="greeting-badge">
            <Sparkles size={14} color="#2563eb" />
            <span>HostelHub Student Portal</span>
          </div>
          <h1 className="greeting-heading">Good Morning, {user?.name || 'Aarav'}! 👋</h1>
          <p className="greeting-subtitle">Here’s what’s happening in your hostel today. Stay updated with your room status and requests.</p>

          {/* Quick Action Buttons */}
          <div className="quick-actions-row">
            <button 
              className="quick-action-btn primary"
              onClick={() => setActiveTab('raise-complaint')}
            >
              <PlusCircle size={18} />
              <span>Report an Issue</span>
            </button>

            <button 
              className="quick-action-btn secondary"
              onClick={() => setActiveTab('visitor-entry')}
            >
              <Users size={18} />
              <span>Register Visitor</span>
            </button>

            <button 
              className="quick-action-btn emergency"
              onClick={() => setActiveTab('emergency')}
            >
              <AlertTriangle size={18} />
              <span>Emergency</span>
            </button>
          </div>
        </div>

        <div className="banner-image-wrapper">
          <img 
            src="/hostel_banner.jpg" 
            alt="Hostel Building Campus" 
            className="hostel-illustration-img"
          />
        </div>
      </div>

      {/* Grid Layout: 2 Columns */}
      <div className="dashboard-grid">
        {/* Left Column: Room Card & Active Complaints */}
        <div className="grid-col-main">
          {/* MY ROOM CARD */}
          <div className="card room-summary-card">
            <div className="card-header-flex">
              <div className="card-title-group">
                <div className="icon-badge blue">
                  <DoorClosed size={20} color="#2563eb" />
                </div>
                <div>
                  <h3 className="section-title">MY ROOM</h3>
                  <p className="section-sub">Assigned residential quarters</p>
                </div>
              </div>
              <span className="room-pill">Active Residence</span>
            </div>

            <div className="room-details-grid">
              <div className="room-stat-box highlight">
                <div className="stat-label">Room Number</div>
                <div className="stat-val large">{user?.roomNo || 'A-203'}</div>
                <div className="stat-sub">Block A • Floor 2</div>
              </div>

              <div className="room-stat-box">
                <div className="stat-label">Capacity</div>
                <div className="stat-val">2 Beds</div>
                <div className="stat-sub">Double Sharing</div>
              </div>

              <div className="room-stat-box">
                <div className="stat-label">Current Occupancy</div>
                <div className="stat-val">2 Students</div>
                <div className="stat-sub text-green">Fully Occupied</div>
              </div>
            </div>

            <div className="roommates-section">
              <div className="roommates-label">Roommates:</div>
              <div className="roommates-pills">
                <span className="roommate-chip current-user">
                  {user?.name} (You)
                </span>
                <span className="roommate-chip">
                  Ria Sharma
                </span>
              </div>
            </div>
          </div>

          {/* MY ACTIVE COMPLAINTS */}
          <div className="card complaints-card">
            <div className="card-header-flex">
              <div className="card-title-group">
                <div className="icon-badge orange">
                  <Wrench size={20} color="#ea580c" />
                </div>
                <div>
                  <h3 className="section-title">MY ACTIVE COMPLAINTS</h3>
                  <p className="section-sub">{activeComplaints.length} issues in resolution flow</p>
                </div>
              </div>
              <button 
                className="view-all-link"
                onClick={() => setActiveTab('my-complaints')}
              >
                <span>View All</span>
                <ArrowRight size={15} />
              </button>
            </div>

            {activeComplaints.length === 0 ? (
              <div className="empty-state">
                <CheckCircle size={36} color="#16a34a" />
                <p>No open complaints! Everything in Room {user?.roomNo || 'A-203'} is running smoothly.</p>
              </div>
            ) : (
              <div className="complaint-items-list">
                {activeComplaints.map(c => (
                  <div key={c.id} className="complaint-item-card">
                    <div className="complaint-left">
                      <div className="complaint-type-icon">
                        {c.department === 'Electrical' ? <Zap size={18} color="#2563eb" /> :
                         c.department === 'Plumbing' ? <Droplets size={18} color="#0d9488" /> :
                         c.type === 'Mess' ? <UtensilsCrossed size={18} color="#d97706" /> :
                         <Wrench size={18} color="#ea580c" />}
                      </div>
                      <div>
                        <div className="complaint-title-row">
                          <span className="complaint-name">{c.description}</span>
                          <span className="complaint-id">#{c.id}</span>
                        </div>
                        <div className="complaint-meta-row">
                          <span>Room {c.roomNo}</span>
                          <span>•</span>
                          <span>{c.type} ({c.department || 'Processing'})</span>
                          {c.technicianName && (
                            <>
                              <span>•</span>
                              <span className="tech-assigned-tag">Assigned: {c.technicianName} ({c.department})</span>
                            </>
                          )}
                        </div>
                      </div>
                    </div>
                    <div className="complaint-right">
                      <StatusBadge status={c.status} />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Quick Timeline & Recent Notifications */}
        <div className="grid-col-side">
          {/* QUICK COMPLAINT TIMELINE */}
          {latestComplaint && (
            <div className="card timeline-mini-card">
              <div className="card-header-flex">
                <h3 className="section-title">LATEST ISSUE TIMELINE</h3>
                <span className="mini-id-pill">#{latestComplaint.id}</span>
              </div>
              <p className="timeline-issue-title">{latestComplaint.description}</p>
              
              <div className="horizontal-timeline-bar">
                <div className={`step-dot ${latestComplaint.id ? 'active' : ''}`}>
                  <div className="dot-circle" />
                  <span className="dot-label">Submitted</span>
                </div>
                <div className={`timeline-connector ${latestComplaint.status !== 'PENDING' ? 'active' : ''}`} />
                <div className={`step-dot ${latestComplaint.status !== 'PENDING' ? 'active' : ''}`}>
                  <div className="dot-circle" />
                  <span className="dot-label">Assigned</span>
                </div>
                <div className={`timeline-connector ${latestComplaint.status === 'IN_PROGRESS' || latestComplaint.status === 'RESOLVED' ? 'active' : ''}`} />
                <div className={`step-dot ${latestComplaint.status === 'IN_PROGRESS' || latestComplaint.status === 'RESOLVED' ? 'active' : ''}`}>
                  <div className="dot-circle" />
                  <span className="dot-label">In Progress</span>
                </div>
                <div className={`timeline-connector ${latestComplaint.status === 'RESOLVED' ? 'active' : ''}`} />
                <div className={`step-dot ${latestComplaint.status === 'RESOLVED' ? 'active' : ''}`}>
                  <div className="dot-circle" />
                  <span className="dot-label">Resolved</span>
                </div>
              </div>

              {latestComplaint.technicianName && (
                <div className="timeline-tech-box">
                  <Wrench size={16} color="#2563eb" />
                  <span>Assigned Technician: <strong>{latestComplaint.technicianName} ({latestComplaint.department})</strong></span>
                </div>
              )}
            </div>
          )}

          {/* RECENT NOTIFICATIONS */}
          <div className="card notifications-card">
            <div className="card-header-flex">
              <div className="card-title-group">
                <div className="icon-badge teal">
                  <Bell size={18} color="#0d9488" />
                </div>
                <div>
                  <h3 className="section-title">RECENT NOTIFICATIONS</h3>
                  <p className="section-sub">Real-time status alerts</p>
                </div>
              </div>
              <button 
                className="view-all-link"
                onClick={() => setActiveTab('notifications')}
              >
                View
              </button>
            </div>

            <div className="notifications-stream">
              {notifications.slice(0, 4).map(n => (
                <div key={n.id} className="notification-tile">
                  <div className="notif-indicator" />
                  <div className="notif-body">
                    <div className="notif-text">{n.message}</div>
                    <div className="notif-time">{n.createdAt ? new Date(n.createdAt).toLocaleDateString() : 'Just now'}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <style>{`
        .student-dashboard {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        /* Welcome Banner Card */
        .welcome-banner-card {
          background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%);
          border-radius: var(--radius-xl);
          border: 1px solid rgba(219, 234, 254, 0.8);
          box-shadow: var(--shadow-card);
          padding: 32px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          overflow: hidden;
          position: relative;
        }

        .banner-content {
          max-width: 620px;
          z-index: 2;
        }

        .greeting-badge {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          background: #eff6ff;
          color: var(--accent-blue);
          border: 1px solid #dbeafe;
          padding: 4px 12px;
          border-radius: var(--radius-full);
          font-size: 0.76rem;
          font-weight: 700;
          margin-bottom: 12px;
          text-transform: uppercase;
          letter-spacing: 0.04em;
        }

        .greeting-heading {
          font-size: 1.625rem; /* 26px standard */
          color: var(--text-main);
          margin-bottom: 6px;
          font-weight: 700;
          letter-spacing: -0.02em;
        }

        .greeting-subtitle {
          color: var(--text-muted);
          font-size: 0.84rem; /* 13.5px */
          line-height: 1.45;
          margin-bottom: 18px;
        }

        .quick-actions-row {
          display: flex;
          align-items: center;
          gap: 10px;
          flex-wrap: wrap;
        }

        .quick-action-btn {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 8px 14px;
          border-radius: var(--radius-sm);
          font-size: 0.8125rem; /* 13px */
          font-weight: 600;
          cursor: pointer;
          transition: all 0.16s ease;
          border: none;
        }

        .quick-action-btn.primary {
          background: var(--accent-blue);
          color: #ffffff;
          box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
        }

        .quick-action-btn.primary:hover {
          background: #1d4ed8;
          transform: translateY(-1px);
        }

        .quick-action-btn.secondary {
          background: #ffffff;
          color: var(--text-main);
          border: 1px solid var(--border-light);
          box-shadow: var(--shadow-sm);
        }

        .quick-action-btn.secondary:hover {
          background: #f8fafc;
          border-color: #cbd5e1;
        }

        .quick-action-btn.emergency {
          background: #fee2e2;
          color: #b91c1c;
          border: 1px solid #fecaca;
        }

        .quick-action-btn.emergency:hover {
          background: #fecaca;
        }

        .banner-image-wrapper {
          width: 300px;
          height: 155px;
          border-radius: var(--radius-md);
          overflow: hidden;
          box-shadow: 0 4px 16px -2px rgba(15, 23, 42, 0.08);
          flex-shrink: 0;
          margin-left: 20px;
        }

        .hostel-illustration-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }

        /* Dashboard Grid: 68% / 32% */
        .dashboard-grid {
          display: grid;
          grid-template-columns: minmax(0, 68fr) minmax(0, 32fr);
          gap: 18px;
          width: 100%;
        }

        .grid-col-main, .grid-col-side {
          display: flex;
          flex-direction: column;
          gap: 18px;
          min-width: 0;
        }

        .card-header-flex {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 14px;
        }

        .card-title-group {
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .icon-badge {
          width: 40px;
          height: 40px;
          border-radius: var(--radius-md);
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .icon-badge.blue { background: #eff6ff; }
        .icon-badge.orange { background: #fff7ed; }
        .icon-badge.teal { background: #f0fdfa; }

        .section-title {
          font-size: 1.05rem;
          color: var(--text-main);
          font-weight: 700;
        }

        .section-sub {
          font-size: 0.78rem;
          color: var(--text-muted);
        }

        .room-pill {
          background: #dcfce7;
          color: #166534;
          font-size: 0.74rem;
          font-weight: 700;
          padding: 4px 10px;
          border-radius: var(--radius-full);
        }

        .room-details-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 14px;
          margin-bottom: 20px;
        }

        .room-stat-box {
          background: var(--bg-main);
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
          padding: 14px;
        }

        .room-stat-box.highlight {
          background: #eff6ff;
          border-color: #bfdbfe;
        }

        .stat-label {
          font-size: 0.74rem;
          font-weight: 600;
          color: var(--text-muted);
          text-transform: uppercase;
          letter-spacing: 0.04em;
          margin-bottom: 6px;
        }

        .stat-val {
          font-size: 1.25rem;
          font-weight: 800;
          color: var(--text-main);
        }

        .stat-val.large {
          font-size: 1.5rem;
          color: var(--accent-blue);
        }

        .stat-sub {
          font-size: 0.74rem;
          color: var(--text-muted);
          margin-top: 4px;
        }

        .stat-sub.text-green {
          color: #16a34a;
          font-weight: 600;
        }

        .roommates-section {
          display: flex;
          align-items: center;
          gap: 12px;
          padding-top: 14px;
          border-top: 1px solid var(--border-light);
        }

        .roommates-label {
          font-size: 0.82rem;
          font-weight: 600;
          color: var(--text-muted);
        }

        .roommates-pills {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }

        .roommate-chip {
          font-size: 0.8rem;
          padding: 4px 10px;
          border-radius: var(--radius-sm);
          background: #f1f5f9;
          color: var(--text-main);
          font-weight: 500;
        }

        .roommate-chip.current-user {
          background: #dbeafe;
          color: #1e40af;
          font-weight: 600;
        }

        .view-all-link {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          color: var(--accent-blue);
          font-size: 0.82rem;
          font-weight: 600;
          background: transparent;
          border: none;
          cursor: pointer;
        }

        .view-all-link:hover {
          text-decoration: underline;
        }

        .complaint-items-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .complaint-item-card {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 14px 16px;
          background: #f8fafc;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
          transition: background 0.15s ease;
        }

        .complaint-item-card:hover {
          background: #f1f5f9;
        }

        .complaint-left {
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .complaint-type-icon {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          background: #ffffff;
          border: 1px solid var(--border-light);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }

        .complaint-title-row {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .complaint-name {
          font-size: 0.92rem;
          font-weight: 700;
          color: var(--text-main);
        }

        .complaint-id {
          font-size: 0.74rem;
          color: var(--text-muted);
          font-weight: 600;
        }

        .complaint-meta-row {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 0.78rem;
          color: var(--text-muted);
          margin-top: 2px;
        }

        .tech-assigned-tag {
          color: var(--accent-blue);
          font-weight: 600;
        }

        /* Timeline Mini Card */
        .timeline-mini-card {
          padding: 22px;
        }

        .mini-id-pill {
          background: #eff6ff;
          color: var(--accent-blue);
          font-size: 0.74rem;
          font-weight: 700;
          padding: 2px 8px;
          border-radius: var(--radius-sm);
        }

        .timeline-issue-title {
          font-size: 0.95rem;
          font-weight: 700;
          color: var(--text-main);
          margin-bottom: 20px;
        }

        .horizontal-timeline-bar {
          display: flex;
          align-items: center;
          justify-content: space-between;
          position: relative;
          margin-bottom: 20px;
        }

        .step-dot {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 6px;
          z-index: 2;
        }

        .dot-circle {
          width: 14px;
          height: 14px;
          border-radius: 50%;
          background: #e2e8f0;
          border: 2px solid #cbd5e1;
        }

        .step-dot.active .dot-circle {
          background: var(--accent-blue);
          border-color: var(--accent-blue);
          box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.2);
        }

        .dot-label {
          font-size: 0.7rem;
          color: var(--text-muted);
          font-weight: 600;
        }

        .step-dot.active .dot-label {
          color: var(--accent-blue);
        }

        .timeline-connector {
          flex: 1;
          height: 2px;
          background: #e2e8f0;
          margin: 0 4px 18px 4px;
        }

        .timeline-connector.active {
          background: var(--accent-blue);
        }

        .timeline-tech-box {
          display: flex;
          align-items: center;
          gap: 8px;
          background: #f0fdf4;
          border: 1px solid #bbf7d0;
          border-radius: var(--radius-sm);
          padding: 10px 12px;
          font-size: 0.8rem;
          color: #166534;
        }

        /* Notifications Stream */
        .notifications-stream {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .notification-tile {
          display: flex;
          gap: 10px;
          padding: 10px;
          border-radius: var(--radius-sm);
          background: #f8fafc;
          border-left: 3px solid var(--accent-blue);
        }

        .notif-text {
          font-size: 0.82rem;
          color: var(--text-main);
          line-height: 1.4;
        }

        .notif-time {
          font-size: 0.7rem;
          color: var(--text-muted);
          margin-top: 4px;
        }

        .empty-state {
          text-align: center;
          padding: 30px;
          color: var(--text-muted);
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 10px;
        }

        @media (max-width: 1024px) {
          .dashboard-grid {
            grid-template-columns: 1fr;
          }
          .welcome-banner-card {
            flex-direction: column;
            align-items: flex-start;
          }
          .banner-image-wrapper {
            margin-left: 0;
            margin-top: 20px;
            width: 100%;
            height: 180px;
          }
        }

        @media (max-width: 640px) {
          .room-details-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
};

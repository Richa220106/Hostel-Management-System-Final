import React, { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/apiService';
import { StatusBadge } from '../components/StatusBadge';
import { 
  Building2, 
  DoorClosed, 
  CheckCircle2, 
  Clock, 
  AlertTriangle, 
  Wrench, 
  Users, 
  RefreshCw, 
  Filter, 
  Search,
  Check,
  UserPlus,
  ArrowUpRight
} from 'lucide-react';

export const WardenDashboard = () => {
  const { user, setActiveTab } = useAuth();
  const [stats, setStats] = useState({
    totalRooms: 6,
    occupiedRooms: 3,
    availableRooms: 3,
    activeComplaints: 0,
    pendingComplaints: 0,
    activeEmergencies: 0
  });
  const [complaints, setComplaints] = useState([]);
  const [technicians, setTechnicians] = useState([]);
  const [visitors, setVisitors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);

  useEffect(() => {
    loadWardenData();
  }, []);

  const loadWardenData = async () => {
    setLoading(true);
    try {
      const [sData, cData, tData, vData] = await Promise.all([
        api.getWardenStats(),
        api.getComplaints(),
        api.getTechnicians(),
        api.getVisitors()
      ]);
      if (sData) setStats(sData);
      if (Array.isArray(cData)) setComplaints(cData);
      if (Array.isArray(tData)) setTechnicians(tData);
      if (Array.isArray(vData)) setVisitors(vData);
    } catch (e) {
      console.error('Failed to load warden data', e);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (complaintId, newStatus) => {
    setUpdatingId(complaintId);
    try {
      await api.updateComplaintStatus(complaintId, newStatus);
      await loadWardenData();
    } catch (e) {
      alert('Error updating status: ' + e.message);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleAssignTechnician = async (complaintId, technicianId) => {
    setUpdatingId(complaintId);
    try {
      await api.assignTechnician(complaintId, technicianId);
      await loadWardenData();
    } catch (e) {
      alert('Error assigning technician: ' + e.message);
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div className="warden-dashboard">
      {/* Welcome Header Bar */}
      <div className="warden-welcome-header">
        <div>
          <h1 className="warden-title">Good Morning, Warden! 👋</h1>
          <p className="warden-subtitle">Here’s the overall real-time status of your hostel facilities and student requests.</p>
        </div>
        <button className="btn btn-secondary" onClick={loadWardenData} title="Refresh Data">
          <RefreshCw size={16} />
          <span>Refresh Live Data</span>
        </button>
      </div>

      {/* 6 Statistics Cards Grid */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon-wrap blue">
            <DoorClosed size={20} color="#2563eb" />
          </div>
          <div className="stat-info">
            <div className="stat-metric-label">Total Rooms</div>
            <div className="stat-metric-value">{stats.totalRooms || 6}</div>
            <div className="stat-metric-sub">Across 3 Residential Blocks</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrap teal">
            <Building2 size={20} color="#0d9488" />
          </div>
          <div className="stat-info">
            <div className="stat-metric-label">Occupied Rooms</div>
            <div className="stat-metric-value">{stats.occupiedRooms || 3}</div>
            <div className="stat-metric-sub text-teal">50% Occupancy Rate</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrap green">
            <CheckCircle2 size={20} color="#16a34a" />
          </div>
          <div className="stat-info">
            <div className="stat-metric-label">Available Rooms</div>
            <div className="stat-metric-value">{stats.availableRooms || 3}</div>
            <div className="stat-metric-sub text-green">Ready for Allocation</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrap orange">
            <Wrench size={20} color="#ea580c" />
          </div>
          <div className="stat-info">
            <div className="stat-metric-label">Active Complaints</div>
            <div className="stat-metric-value">{stats.activeComplaints || complaints.filter(c => c.status !== 'RESOLVED').length}</div>
            <div className="stat-metric-sub text-orange">In Assignment / Repair</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrap amber">
            <Clock size={20} color="#d97706" />
          </div>
          <div className="stat-info">
            <div className="stat-metric-label">Pending Complaints</div>
            <div className="stat-metric-value">{stats.pendingComplaints || complaints.filter(c => c.status === 'PENDING').length}</div>
            <div className="stat-metric-sub text-amber">Needs Technician Dispatch</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrap red">
            <AlertTriangle size={20} color="#dc2626" />
          </div>
          <div className="stat-info">
            <div className="stat-metric-label">Active Emergencies</div>
            <div className="stat-metric-value">{stats.activeEmergencies || complaints.filter(c => c.type === 'Emergency' && c.status !== 'RESOLVED').length}</div>
            <div className="stat-metric-sub text-red">Immediate Attention</div>
          </div>
        </div>
      </div>

      {/* Main Content Layout: Live Hostel Issues + Technicians Workload */}
      <div className="warden-layout-grid">
        {/* Left Column: Live Hostel Issues Table */}
        <div className="live-issues-column">
          <div className="card live-issues-card">
            <div className="issues-header-flex">
              <div>
                <h3 className="card-heading">LIVE HOSTEL ISSUES</h3>
                <p className="card-subheading">Real-time complaint tracking routed through Chain of Responsibility</p>
              </div>
              <span className="total-pill">{complaints.length} Total Complaints</span>
            </div>

            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Room</th>
                    <th>Issue Description</th>
                    <th>Department</th>
                    <th>Technician</th>
                    <th>Status</th>
                    <th>Manage Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {complaints.map(c => (
                    <tr key={c.id}>
                      <td>
                        <span className="complaint-id-badge">#{c.id}</span>
                      </td>
                      <td>
                        <strong className="room-highlight">{c.roomNo}</strong>
                      </td>
                      <td>
                        <div className="issue-text-wrap">
                          <span className="issue-desc">{c.description}</span>
                          <span className="issue-sub">Student: {c.studentName || 'Student'} • Priority: {c.priority}</span>
                        </div>
                      </td>
                      <td>
                        <span className="dept-tag">{c.department || c.type}</span>
                      </td>
                      <td>
                        {c.technicianName ? (
                          <div className="tech-assigned-pill">
                            <Wrench size={13} />
                            <span>{c.technicianName}</span>
                          </div>
                        ) : (
                          <select 
                            className="assign-quick-select"
                            defaultValue=""
                            onChange={(e) => {
                              if (e.target.value) handleAssignTechnician(c.id, Number(e.target.value));
                            }}
                          >
                            <option value="" disabled>Assign Tech</option>
                            {technicians.map(t => (
                              <option key={t.id} value={t.id}>{t.name} ({t.department})</option>
                            ))}
                          </select>
                        )}
                      </td>
                      <td>
                        <StatusBadge status={c.status} />
                      </td>
                      <td>
                        <div className="action-buttons-group">
                          {c.status !== 'IN_PROGRESS' && c.status !== 'RESOLVED' && (
                            <button 
                              className="action-btn-sm in-progress"
                              disabled={updatingId === c.id}
                              onClick={() => handleStatusUpdate(c.id, 'IN_PROGRESS')}
                              title="Mark as In Progress"
                            >
                              In Progress
                            </button>
                          )}
                          {c.status !== 'RESOLVED' && (
                            <button 
                              className="action-btn-sm resolve"
                              disabled={updatingId === c.id}
                              onClick={() => handleStatusUpdate(c.id, 'RESOLVED')}
                              title="Mark as Resolved"
                            >
                              Resolve
                            </button>
                          )}
                          {c.status === 'RESOLVED' && (
                            <span className="resolved-check">
                              <Check size={14} /> Closed
                            </span>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Right Column: Technician Workload & Quick Widgets */}
        <div className="warden-side-column">
          {/* TECHNICIAN WORKLOAD */}
          <div className="card tech-workload-card">
            <div className="card-header-flex">
              <div>
                <h3 className="card-heading">TECHNICIAN WORKLOAD</h3>
                <p className="card-subheading">Staff allocation & active repair tasks</p>
              </div>
              <button 
                className="view-link"
                onClick={() => setActiveTab('warden-technicians')}
              >
                Manage
              </button>
            </div>

            <div className="technicians-workload-list">
              {technicians.map(tech => {
                const activeCount = tech.activeTasks || 0;
                const maxTasks = 5;
                const percent = Math.min(100, Math.round((activeCount / maxTasks) * 100));

                return (
                  <div key={tech.id} className="tech-workload-row">
                    <div className="tech-row-header">
                      <div className="tech-name-col">
                        <strong className="tech-name">{tech.name}</strong>
                        <span className="tech-dept">({tech.department})</span>
                      </div>
                      <span className="tech-task-count">{activeCount} active task{activeCount !== 1 ? 's' : ''}</span>
                    </div>

                    <div className="workload-progress-bar">
                      <div 
                        className={`progress-fill ${activeCount > 2 ? 'high' : activeCount > 0 ? 'medium' : 'low'}`}
                        style={{ width: `${percent || 8}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* VISITOR ACTIVITY OVERVIEW */}
          <div className="card visitor-activity-card">
            <div className="card-header-flex">
              <div>
                <h3 className="card-heading">VISITOR ACTIVITY</h3>
                <p className="card-subheading">Registered campus entries</p>
              </div>
              <button 
                className="view-link"
                onClick={() => setActiveTab('warden-visitors')}
              >
                Log
              </button>
            </div>

            <div className="visitors-preview-list">
              {visitors.slice(0, 3).map(v => (
                <div key={v.id} className="visitor-item">
                  <div className="visitor-avatar">{v.visitorName.charAt(0)}</div>
                  <div className="visitor-meta">
                    <div className="visitor-name-line">
                      <strong>{v.visitorName}</strong>
                      <span className="visit-room-tag">Room {v.roomNo}</span>
                    </div>
                    <div className="visit-detail-line">
                      Visiting {v.studentName || 'Student'} • {v.entryTime} - {v.exitTime}
                    </div>
                  </div>
                  <span className={`status-dot-pill ${v.status === 'APPROVED' ? 'approved' : 'exited'}`}>
                    {v.status}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <style>{`
        .warden-dashboard {
          display: flex;
          flex-direction: column;
          gap: 18px;
        }

        .warden-welcome-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding-bottom: 2px;
        }

        .warden-title {
          font-size: 1.625rem; /* 26px heading standard */
          font-weight: 700;
          color: var(--text-main);
          letter-spacing: -0.02em;
          margin-bottom: 3px;
        }

        .warden-subtitle {
          font-size: 0.84rem; /* 13.5px standard */
          color: var(--text-muted);
        }

        /* 6 Statistics Cards Grid - Compact and responsive */
        .stats-grid {
          display: grid;
          grid-template-columns: repeat(6, minmax(0, 1fr));
          gap: 12px;
          width: 100%;
        }

        @media (max-width: 1360px) {
          .stats-grid {
            grid-template-columns: repeat(3, minmax(0, 1fr));
          }
        }

        @media (max-width: 860px) {
          .stats-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
          }
        }

        @media (max-width: 520px) {
          .stats-grid {
            grid-template-columns: 1fr;
          }
        }

        .stat-card {
          background: #ffffff;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-sm);
          padding: 12px 14px;
          box-shadow: var(--shadow-sm);
          display: flex;
          align-items: center;
          gap: 12px;
          transition: transform 0.15s ease, box-shadow 0.15s ease;
          min-width: 0;
        }

        .stat-card:hover {
          transform: translateY(-1px);
          box-shadow: var(--shadow-card);
        }

        .stat-icon-wrap {
          width: 36px;
          height: 36px;
          border-radius: var(--radius-sm);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }

        .stat-info {
          display: flex;
          flex-direction: column;
          min-width: 0;
          overflow: hidden;
        }

        .stat-icon-wrap.blue { background: #eff6ff; }
        .stat-icon-wrap.teal { background: #f0fdfa; }
        .stat-icon-wrap.green { background: #f0fdf4; }
        .stat-icon-wrap.orange { background: #fff7ed; }
        .stat-icon-wrap.amber { background: #fffbeb; }
        .stat-icon-wrap.red { background: #fef2f2; }

        .stat-metric-label {
          font-size: 0.68rem; /* 11px uppercase */
          font-weight: 700;
          color: var(--text-muted);
          text-transform: uppercase;
          letter-spacing: 0.04em;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .stat-metric-value {
          font-size: 1.35rem; /* 21.5px */
          font-weight: 800;
          color: var(--text-main);
          line-height: 1.15;
          margin: 1px 0;
        }

        .stat-metric-sub {
          font-size: 0.68rem; /* 11px */
          color: var(--text-muted);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .text-teal { color: #0d9488; font-weight: 600; }
        .text-green { color: #16a34a; font-weight: 600; }
        .text-orange { color: #ea580c; font-weight: 600; }
        .text-amber { color: #d97706; font-weight: 600; }
        .text-red { color: #dc2626; font-weight: 600; }

        /* Main Content Layout Grid (68% / 32% proportion) */
        .warden-layout-grid {
          display: grid;
          grid-template-columns: minmax(0, 68fr) minmax(0, 32fr);
          gap: 18px;
          width: 100%;
        }

        .live-issues-column, .warden-side-column {
          display: flex;
          flex-direction: column;
          gap: 18px;
          min-width: 0;
        }

        .issues-header-flex, .card-header-flex {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 14px;
        }

        .card-heading {
          font-size: 1.05rem; /* 17px section header */
          font-weight: 700;
          color: var(--text-main);
          letter-spacing: -0.01em;
        }

        .card-subheading {
          font-size: 0.78rem; /* 12.5px */
          color: var(--text-muted);
          margin-top: 1px;
        }

        .total-pill {
          background: #f1f5f9;
          color: var(--text-main);
          font-size: 0.72rem;
          font-weight: 700;
          padding: 3px 8px;
          border-radius: var(--radius-full);
        }

        .complaint-id-badge {
          font-weight: 700;
          color: var(--accent-blue);
          font-size: 0.8125rem; /* 13px */
        }

        .room-highlight {
          color: var(--text-main);
          font-weight: 700;
          font-size: 0.8125rem; /* 13px */
        }

        .issue-text-wrap {
          display: flex;
          flex-direction: column;
          max-width: 300px;
        }

        .issue-desc {
          font-weight: 600;
          font-size: 0.8125rem; /* 13px */
          color: var(--text-main);
          line-height: 1.35;
        }

        .issue-sub {
          font-size: 0.7rem; /* 11px */
          color: var(--text-muted);
          margin-top: 2px;
        }

        .dept-tag {
          background: #f8fafc;
          border: 1px solid var(--border-light);
          padding: 2px 7px;
          border-radius: var(--radius-sm);
          font-size: 0.72rem; /* 11.5px */
          font-weight: 600;
          color: var(--text-main);
          white-space: nowrap;
        }

        .tech-assigned-pill {
          display: inline-flex;
          align-items: center;
          gap: 5px;
          background: #eff6ff;
          color: #1e40af;
          border: 1px solid #bfdbfe;
          padding: 3px 8px;
          border-radius: var(--radius-full);
          font-size: 0.74rem; /* 12px */
          font-weight: 600;
          white-space: nowrap;
        }

        .assign-quick-select {
          padding: 4px 6px;
          border-radius: var(--radius-sm);
          border: 1px solid var(--border-light);
          font-size: 0.75rem;
          color: var(--text-main);
          background: #ffffff;
          outline: none;
        }

        .action-buttons-group {
          display: flex;
          align-items: center;
          gap: 5px;
        }

        .action-btn-sm {
          padding: 4px 8px;
          border-radius: var(--radius-sm);
          font-size: 0.72rem; /* 11.5px */
          font-weight: 700;
          border: none;
          cursor: pointer;
          transition: all 0.15s ease;
          white-space: nowrap;
        }

        .action-btn-sm.in-progress {
          background: #ffedd5;
          color: #c2410c;
        }

        .action-btn-sm.in-progress:hover {
          background: #fed7aa;
        }

        .action-btn-sm.resolve {
          background: #dcfce7;
          color: #15803d;
        }

        .action-btn-sm.resolve:hover {
          background: #bbf7d0;
        }

        .resolved-check {
          display: inline-flex;
          align-items: center;
          gap: 3px;
          color: #16a34a;
          font-size: 0.74rem;
          font-weight: 700;
        }

        /* Tech Workload */
        .technicians-workload-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .tech-workload-row {
          display: flex;
          flex-direction: column;
          gap: 4px;
        }

        .tech-row-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
        }

        .tech-name {
          font-size: 0.8125rem; /* 13px */
          color: var(--text-main);
        }

        .tech-dept {
          font-size: 0.72rem; /* 11.5px */
          color: var(--text-muted);
          margin-left: 3px;
        }

        .tech-task-count {
          font-size: 0.72rem;
          font-weight: 700;
          color: var(--accent-blue);
        }

        .workload-progress-bar {
          width: 100%;
          height: 5px;
          background: #f1f5f9;
          border-radius: var(--radius-full);
          overflow: hidden;
        }

        .progress-fill {
          height: 100%;
          border-radius: var(--radius-full);
          transition: width 0.3s ease;
        }

        .progress-fill.low { background: #10b981; }
        .progress-fill.medium { background: #3b82f6; }
        .progress-fill.high { background: #f97316; }

        .view-link {
          background: transparent;
          border: none;
          color: var(--accent-blue);
          font-size: 0.76rem;
          font-weight: 600;
          cursor: pointer;
        }

        /* Visitor Preview List */
        .visitors-preview-list {
          display: flex;
          flex-direction: column;
          gap: 10px;
        }

        .visitor-item {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 8px 10px;
          background: #f8fafc;
          border-radius: var(--radius-sm);
        }

        .visitor-avatar {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          background: #e2e8f0;
          color: var(--text-main);
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 0.78rem;
          flex-shrink: 0;
        }

        .visitor-meta {
          flex: 1;
        }

        .visitor-name-line {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 0.8rem; /* 13px */
        }

        .visit-room-tag {
          font-size: 0.68rem;
          color: var(--accent-blue);
          background: #eff6ff;
          padding: 1px 5px;
          border-radius: var(--radius-sm);
        }

        .visit-detail-line {
          font-size: 0.7rem;
          color: var(--text-muted);
          margin-top: 1px;
        }

        .status-dot-pill {
          font-size: 0.66rem;
          font-weight: 700;
          padding: 2px 6px;
          border-radius: var(--radius-full);
        }

        .status-dot-pill.approved {
          background: #dcfce7;
          color: #15803d;
        }

        .status-dot-pill.exited {
          background: #f1f5f9;
          color: var(--text-muted);
        }

        @media (max-width: 1024px) {
          .warden-layout-grid {
            grid-template-columns: 1fr;
          }
        }

        @media (max-width: 640px) {
          .warden-welcome-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 10px;
          }
        }
      `}</style>
    </div>
  );
};

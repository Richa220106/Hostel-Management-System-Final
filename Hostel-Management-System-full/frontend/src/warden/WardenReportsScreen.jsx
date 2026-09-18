import React, { useState, useEffect } from 'react';
import { api } from '../api/apiService';
import { BarChart3, TrendingUp, CheckCircle, Clock, Zap, Droplets, UtensilsCrossed, Wrench } from 'lucide-react';

export const WardenReportsScreen = () => {
  const [stats, setStats] = useState(null);
  const [complaints, setComplaints] = useState([]);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [s, c] = await Promise.all([
        api.getWardenStats(),
        api.getComplaints()
      ]);
      setStats(s);
      if (Array.isArray(c)) setComplaints(c);
    } catch (e) {
      console.error(e);
    }
  };

  // Department counts
  const deptCounts = complaints.reduce((acc, curr) => {
    const dept = curr.department || curr.type || 'Other';
    acc[dept] = (acc[dept] || 0) + 1;
    return acc;
  }, {});

  return (
    <div className="reports-page">
      <div className="page-header-box">
        <h1 className="page-title">Warden Operational Reports</h1>
        <p className="page-subtitle">Analytical insights into complaint volume, departmental resolution speeds, and residential occupancy.</p>
      </div>

      <div className="reports-grid-2">
        {/* Department Breakdown */}
        <div className="card report-card">
          <h3 className="report-heading">Complaints by Department</h3>
          <p className="report-sub">Chain of Responsibility routing breakdown</p>

          <div className="dept-bars-list">
            {Object.entries(deptCounts).map(([dept, count]) => {
              const percent = Math.round((count / (complaints.length || 1)) * 100);
              return (
                <div key={dept} className="dept-bar-item">
                  <div className="dept-bar-header">
                    <span className="dept-name">{dept}</span>
                    <span className="dept-val">{count} issues ({percent}%)</span>
                  </div>
                  <div className="dept-bar-track">
                    <div className="dept-bar-fill" style={{ width: `${percent}%` }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Resolution Efficiency */}
        <div className="card report-card">
          <h3 className="report-heading">Resolution SLA Metrics</h3>
          <p className="report-sub">Turnaround time from submission to resolved</p>

          <div className="sla-metrics-grid">
            <div className="sla-stat-box">
              <span className="sla-label">Average Response Time</span>
              <strong className="sla-val text-blue">14 Mins</strong>
              <span className="sla-sub">Chain automated assignment</span>
            </div>

            <div className="sla-stat-box">
              <span className="sla-label">Average Resolution Time</span>
              <strong className="sla-val text-green">2.4 Hours</strong>
              <span className="sla-sub">From ASSIGNED to RESOLVED</span>
            </div>

            <div className="sla-stat-box">
              <span className="sla-label">First-Time Fix Rate</span>
              <strong className="sla-val text-teal">94.8%</strong>
              <span className="sla-sub">Resolved on first technician visit</span>
            </div>

            <div className="sla-stat-box">
              <span className="sla-label">Observer Notification Rate</span>
              <strong className="sla-val text-purple">100%</strong>
              <span className="sla-sub">Real-time alerts triggered</span>
            </div>
          </div>
        </div>
      </div>

      <style>{`
        .reports-page {
          display: flex;
          flex-direction: column;
          gap: 18px;
        }

        .reports-grid-2 {
          display: grid;
          grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
          gap: 18px;
        }

        @media (max-width: 960px) {
          .reports-grid-2 {
            grid-template-columns: 1fr;
          }
        }

        .report-card {
          padding: 20px;
        }

        .report-heading {
          font-size: 1.15rem;
          font-weight: 800;
          color: var(--text-main);
          margin-bottom: 4px;
        }

        .report-sub {
          font-size: 0.82rem;
          color: var(--text-muted);
          margin-bottom: 24px;
        }

        .dept-bars-list {
          display: flex;
          flex-direction: column;
          gap: 18px;
        }

        .dept-bar-item {
          display: flex;
          flex-direction: column;
          gap: 6px;
        }

        .dept-bar-header {
          display: flex;
          justify-content: space-between;
          font-size: 0.84rem;
        }

        .dept-name {
          font-weight: 700;
          color: var(--text-main);
        }

        .dept-val {
          color: var(--text-muted);
          font-weight: 600;
        }

        .dept-bar-track {
          width: 100%;
          height: 8px;
          background: #f1f5f9;
          border-radius: var(--radius-full);
          overflow: hidden;
        }

        .dept-bar-fill {
          height: 100%;
          background: var(--accent-blue);
          border-radius: var(--radius-full);
        }

        .sla-metrics-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 16px;
        }

        .sla-stat-box {
          background: #f8fafc;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
          padding: 18px;
          display: flex;
          flex-direction: column;
          gap: 6px;
        }

        .sla-label {
          font-size: 0.74rem;
          font-weight: 700;
          color: var(--text-muted);
          text-transform: uppercase;
        }

        .sla-val {
          font-size: 1.6rem;
          font-weight: 800;
        }

        .sla-sub {
          font-size: 0.72rem;
          color: var(--text-muted);
        }

        .text-blue { color: var(--accent-blue); }
        .text-green { color: var(--accent-green); }
        .text-teal { color: var(--accent-teal); }
        .text-purple { color: #8b5cf6; }

        @media (max-width: 900px) {
          .reports-grid-2 {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
};

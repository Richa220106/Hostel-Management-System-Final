import React, { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/apiService';
import { Users, UserPlus, Clock, Calendar, CheckCircle2, ShieldCheck } from 'lucide-react';

export const VisitorEntryScreen = () => {
  const { user } = useAuth();
  const [visitors, setVisitors] = useState([]);
  const [visitorName, setVisitorName] = useState('');
  const [contact, setContact] = useState('');
  const [visitDate, setVisitDate] = useState('2026-09-16');
  const [entryTime, setEntryTime] = useState('11:00 AM');
  const [exitTime, setExitTime] = useState('04:00 PM');
  const [reason, setReason] = useState('Parent Visit');
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    loadVisitors();
  }, []);

  const loadVisitors = async () => {
    try {
      const data = await api.getVisitors();
      if (Array.isArray(data)) setVisitors(data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!visitorName.trim()) {
      alert('Please enter visitor name');
      return;
    }

    setSubmitting(true);
    try {
      await api.registerVisitor({
        visitorName,
        contact,
        visitDate,
        entryTime,
        exitTime,
        reason,
        roomNo: user?.roomNo || 'A-203'
      });

      setSuccessMsg(`Visitor entry pass generated for ${visitorName}`);
      setVisitorName('');
      setContact('');
      loadVisitors();
    } catch (e) {
      alert('Failed to register visitor: ' + e.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="visitor-entry-page">
      <div className="page-header-box">
        <h1 className="page-title">Visitor Registration</h1>
        <p className="page-subtitle">Register visiting guests or parents for gate check-in and room access passes.</p>
      </div>

      <div className="visitor-layout-grid">
        {/* Registration Form */}
        <div className="card visitor-form-card">
          <div className="card-header-flex">
            <div className="card-title-group">
              <div className="icon-badge blue">
                <UserPlus size={20} color="#2563eb" />
              </div>
              <div>
                <h3 className="section-title">New Visitor Entry Pass</h3>
                <p className="section-sub">Room {user?.roomNo || 'A-203'} Host Pass</p>
              </div>
            </div>
          </div>

          {successMsg && (
            <div className="success-pill-box">
              <CheckCircle2 size={18} color="#16a34a" />
              <span>{successMsg}</span>
            </div>
          )}

          <form onSubmit={handleRegister} className="v-form">
            <div className="form-group">
              <label className="form-label">Visitor Full Name</label>
              <input 
                type="text" 
                className="form-input" 
                placeholder="e.g. Karan Patel"
                value={visitorName}
                onChange={(e) => setVisitorName(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Contact Number</label>
              <input 
                type="tel" 
                className="form-input" 
                placeholder="+91 98765 43210"
                value={contact}
                onChange={(e) => setContact(e.target.value)}
                required
              />
            </div>

            <div className="form-row-2">
              <div className="form-group">
                <label className="form-label">Visit Date</label>
                <input 
                  type="date" 
                  className="form-input" 
                  value={visitDate}
                  onChange={(e) => setVisitDate(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Visit Reason</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="e.g. Family / Luggage"
                  required
                />
              </div>
            </div>

            <div className="form-row-2">
              <div className="form-group">
                <label className="form-label">Entry Time</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={entryTime}
                  onChange={(e) => setEntryTime(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Expected Exit Time</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={exitTime}
                  onChange={(e) => setExitTime(e.target.value)}
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-submit-v" disabled={submitting}>
              <ShieldCheck size={18} />
              <span>{submitting ? 'Registering...' : 'Register Visitor Pass'}</span>
            </button>
          </form>
        </div>

        {/* Visitor History */}
        <div className="card visitor-history-card">
          <div className="card-header-flex">
            <h3 className="section-title">My Visitor History</h3>
            <span className="count-tag">{visitors.length} Passes</span>
          </div>

          <div className="visitors-list">
            {visitors.map(v => (
              <div key={v.id} className="v-history-item">
                <div className="v-avatar">{v.visitorName.charAt(0)}</div>
                <div className="v-details">
                  <div className="v-top-row">
                    <strong>{v.visitorName}</strong>
                    <span className="v-status-badge">{v.status}</span>
                  </div>
                  <div className="v-sub-row">
                    <span>Date: {v.visitDate}</span>
                    <span>•</span>
                    <span>{v.entryTime} - {v.exitTime}</span>
                  </div>
                  <div className="v-reason-row">
                    Reason: {v.reason} • Contact: {v.contact}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <style>{`
        .visitor-entry-page {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .visitor-layout-grid {
          display: grid;
          grid-template-columns: 1.1fr 1fr;
          gap: 24px;
        }

        .v-form {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .btn-submit-v {
          padding: 12px;
          margin-top: 8px;
        }

        .success-pill-box {
          display: flex;
          align-items: center;
          gap: 8px;
          background: #f0fdf4;
          border: 1px solid #bbf7d0;
          color: #166534;
          padding: 10px 14px;
          border-radius: var(--radius-md);
          margin-bottom: 16px;
          font-size: 0.85rem;
          font-weight: 600;
        }

        .visitors-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .v-history-item {
          display: flex;
          gap: 12px;
          padding: 12px;
          background: #f8fafc;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
        }

        .v-avatar {
          width: 38px;
          height: 38px;
          border-radius: 50%;
          background: #eff6ff;
          color: var(--accent-blue);
          font-weight: 800;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }

        .v-details {
          flex: 1;
        }

        .v-top-row {
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 0.9rem;
        }

        .v-status-badge {
          font-size: 0.7rem;
          font-weight: 700;
          padding: 2px 8px;
          border-radius: var(--radius-full);
          background: #dcfce7;
          color: #166534;
        }

        .v-sub-row {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 0.78rem;
          color: var(--text-muted);
          margin-top: 3px;
        }

        .v-reason-row {
          font-size: 0.75rem;
          color: var(--text-muted);
          margin-top: 4px;
        }

        .count-tag {
          font-size: 0.75rem;
          background: #f1f5f9;
          padding: 3px 8px;
          border-radius: var(--radius-full);
          font-weight: 700;
        }

        @media (max-width: 1024px) {
          .visitor-layout-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
};

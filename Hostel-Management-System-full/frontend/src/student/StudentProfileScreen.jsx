import React from 'react';
import { useAuth } from '../auth/AuthContext';
import { User, Mail, Phone, DoorClosed, Shield, Building2, Calendar, Award } from 'lucide-react';

export const StudentProfileScreen = () => {
  const { user } = useAuth();

  return (
    <div className="student-profile-page">
      <div className="page-header-box">
        <h1 className="page-title">Student Resident Profile</h1>
        <p className="page-subtitle">Your registered hostel identification, room allocation, and emergency details.</p>
      </div>

      <div className="profile-layout-grid">
        {/* Left: ID Card */}
        <div className="card id-card">
          <div className="id-card-header">
            <div className="id-brand">
              <Building2 size={20} color="#ffffff" />
              <span>HostelHub Resident Card</span>
            </div>
            <span className="id-active-tag">Active 2026</span>
          </div>

          <div className="id-card-body">
            <div className="avatar-large">
              {user?.name?.charAt(0) || 'S'}
            </div>
            <h2 className="id-student-name">{user?.name}</h2>
            <span className="id-roll-number">Roll: 24CS029 • Computer Science</span>

            <div className="id-chips-row">
              <span className="id-chip">Room {user?.roomNo || 'A-203'}</span>
              <span className="id-chip">Block A</span>
              <span className="id-chip">Double Sharing</span>
            </div>
          </div>

          <div className="id-card-footer">
            <div className="id-barcode-mock" />
            <span className="id-hash">HH-RES-2026-90118</span>
          </div>
        </div>

        {/* Right: Residential Details & Roommate */}
        <div className="profile-details-col">
          <div className="card details-card">
            <h3 className="card-section-title">Personal & Contact Info</h3>
            
            <div className="details-list">
              <div className="detail-item">
                <User size={16} color="#64748b" />
                <div className="detail-meta">
                  <span className="d-label">Full Name</span>
                  <span className="d-val">{user?.name}</span>
                </div>
              </div>

              <div className="detail-item">
                <Mail size={16} color="#64748b" />
                <div className="detail-meta">
                  <span className="d-label">Hostel Email</span>
                  <span className="d-val">{user?.email || 'aarav.patel@hostelhub.edu'}</span>
                </div>
              </div>

              <div className="detail-item">
                <Phone size={16} color="#64748b" />
                <div className="detail-meta">
                  <span className="d-label">Phone Contact</span>
                  <span className="d-val">{user?.contact || '+91 90118 48854'}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="card details-card">
            <h3 className="card-section-title">Room & Roommates</h3>
            
            <div className="room-profile-box">
              <div className="r-badge-room">Room {user?.roomNo || 'A-203'}</div>
              <div className="r-desc-text">
                Assigned to <strong>Block A, 2nd Floor</strong>. Facility includes split air-conditioning, attached bath, and study desk units.
              </div>

              <div className="roommate-sub-card">
                <div className="roommate-avatar">R</div>
                <div>
                  <strong>Ria Sharma</strong>
                  <div className="text-muted text-sm">Roommate • Year 2 CS</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <style>{`
        .student-profile-page {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .profile-layout-grid {
          display: grid;
          grid-template-columns: 1fr 1.3fr;
          gap: 24px;
        }

        .id-card {
          background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
          color: #ffffff;
          padding: 28px;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          border-radius: var(--radius-xl);
          min-height: 420px;
          box-shadow: 0 16px 36px -6px rgba(15, 23, 42, 0.35);
        }

        .id-card-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
        }

        .id-brand {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 0.85rem;
          font-weight: 700;
          color: #93c5fd;
        }

        .id-active-tag {
          font-size: 0.7rem;
          font-weight: 700;
          background: rgba(34, 197, 94, 0.2);
          color: #4ade80;
          padding: 3px 8px;
          border-radius: var(--radius-full);
          border: 1px solid rgba(74, 222, 128, 0.4);
        }

        .id-card-body {
          display: flex;
          flex-direction: column;
          align-items: center;
          text-align: center;
          margin: 20px 0;
        }

        .avatar-large {
          width: 84px;
          height: 84px;
          border-radius: 50%;
          background: linear-gradient(135deg, #3b82f6, #1d4ed8);
          color: #ffffff;
          font-size: 2.2rem;
          font-weight: 800;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 4px solid rgba(255, 255, 255, 0.15);
          margin-bottom: 14px;
        }

        .id-student-name {
          color: #ffffff;
          font-size: 1.45rem;
          font-weight: 800;
          margin-bottom: 4px;
        }

        .id-roll-number {
          font-size: 0.82rem;
          color: #94a3b8;
          margin-bottom: 16px;
        }

        .id-chips-row {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
          justify-content: center;
        }

        .id-chip {
          font-size: 0.74rem;
          font-weight: 600;
          background: rgba(255, 255, 255, 0.1);
          padding: 4px 10px;
          border-radius: var(--radius-full);
          border: 1px solid rgba(255, 255, 255, 0.15);
        }

        .id-card-footer {
          display: flex;
          align-items: center;
          justify-content: space-between;
          border-top: 1px solid rgba(255, 255, 255, 0.1);
          padding-top: 16px;
        }

        .id-barcode-mock {
          width: 120px;
          height: 24px;
          background: repeating-linear-gradient(
            90deg,
            #ffffff,
            #ffffff 2px,
            transparent 2px,
            transparent 5px
          );
        }

        .id-hash {
          font-family: monospace;
          font-size: 0.72rem;
          color: #94a3b8;
        }

        .profile-details-col {
          display: flex;
          flex-direction: column;
          gap: 20px;
        }

        .details-card {
          padding: 24px;
        }

        .card-section-title {
          font-size: 1.05rem;
          font-weight: 800;
          color: var(--text-main);
          margin-bottom: 16px;
          padding-bottom: 10px;
          border-bottom: 1px solid var(--border-light);
        }

        .details-list {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .detail-item {
          display: flex;
          align-items: center;
          gap: 14px;
        }

        .detail-meta {
          display: flex;
          flex-direction: column;
        }

        .d-label {
          font-size: 0.74rem;
          color: var(--text-muted);
          text-transform: uppercase;
        }

        .d-val {
          font-size: 0.92rem;
          font-weight: 600;
          color: var(--text-main);
        }

        .room-profile-box {
          display: flex;
          flex-direction: column;
          gap: 14px;
        }

        .r-badge-room {
          display: inline-block;
          font-size: 1.1rem;
          font-weight: 800;
          color: var(--accent-blue);
        }

        .r-desc-text {
          font-size: 0.88rem;
          color: var(--text-muted);
          line-height: 1.5;
        }

        .roommate-sub-card {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          background: #f8fafc;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
        }

        .roommate-avatar {
          width: 38px;
          height: 38px;
          border-radius: 50%;
          background: #eff6ff;
          color: var(--accent-blue);
          font-weight: 800;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .text-sm {
          font-size: 0.78rem;
        }

        @media (max-width: 960px) {
          .profile-layout-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
};

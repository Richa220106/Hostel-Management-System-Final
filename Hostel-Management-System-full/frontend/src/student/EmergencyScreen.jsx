import React, { useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/apiService';
import { AlertTriangle, PhoneCall, ShieldAlert, CheckCircle2, Siren, Building2, UserCheck } from 'lucide-react';

export const EmergencyScreen = () => {
  const { user } = useAuth();
  const [triggered, setTriggered] = useState(false);
  const [loading, setLoading] = useState(false);
  const [emergencyDesc, setEmergencyDesc] = useState('Medical / Security emergency in Room ' + (user?.roomNo || 'A-203'));

  const handleRaiseEmergency = async () => {
    setLoading(true);
    try {
      // Direct call to backend emergency API: triggers EmergencyRequest Factory -> Singleton -> EmergencyHandler -> Security Technician assigned -> Observer notifies
      await api.triggerEmergency({
        roomNo: user?.roomNo || 'A-203',
        description: emergencyDesc
      });
      setTriggered(true);
    } catch (e) {
      alert('Failed to trigger emergency: ' + e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="emergency-page">
      <div className="page-header-box">
        <h1 className="page-title">Emergency Assistance</h1>
        <p className="page-subtitle">Immediate response protocol for medical, security, or critical residential emergencies.</p>
      </div>

      <div className="emergency-layout-grid">
        {/* Main Action Card */}
        <div className="card emergency-main-card">
          <div className="emergency-badge-banner">
            <Siren size={20} color="#dc2626" />
            <span>High Priority Security Protocol</span>
          </div>

          <h2 className="emergency-headline">EMERGENCY REQUEST</h2>
          <p className="emergency-lead-text">Need immediate help? Raise an emergency request to dispatch hostel security and notify the warden instantly.</p>

          <div className="emergency-room-box">
            <span className="room-pin-label">Current Emergency Location:</span>
            <strong className="room-pin-val">Room {user?.roomNo || 'A-203'} (Block A)</strong>
          </div>

          {!triggered ? (
            <div className="trigger-action-area">
              <button 
                className="raise-emergency-btn"
                onClick={handleRaiseEmergency}
                disabled={loading}
              >
                <AlertTriangle size={24} />
                <span>{loading ? 'Dispatching Protocol...' : 'RAISE EMERGENCY'}</span>
              </button>
              <p className="emergency-disclaimer">
                Pressing this button activates the emergency observer and alerts campus security and the warden immediately.
              </p>
            </div>
          ) : (
            <div className="emergency-active-alert">
              <CheckCircle2 size={32} color="#16a34a" />
              <div>
                <h3 className="alert-sent-title">EMERGENCY ALERT BROADCASTED</h3>
                <p className="alert-sent-desc">
                  Warden Dr. K. Sharma and Campus Security team have been alerted. Emergency complaint created with high priority. Stay calm; assistance is on the way.
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Emergency Contacts Card */}
        <div className="card emergency-contacts-card">
          <div className="card-header-flex">
            <h3 className="section-title">Emergency Contacts</h3>
            <span className="live-status-pill">24/7 Available</span>
          </div>

          <div className="contacts-list">
            <div className="contact-tile">
              <div className="contact-icon-box warden-c">
                <UserCheck size={20} color="#2563eb" />
              </div>
              <div className="contact-info">
                <div className="contact-role">Chief Warden</div>
                <div className="contact-name">Dr. K. Sharma</div>
                <div className="contact-number">+91 98765 43210</div>
              </div>
              <a href="tel:+919876543210" className="call-action-btn">
                <PhoneCall size={16} />
                <span>Call</span>
              </a>
            </div>

            <div className="contact-tile">
              <div className="contact-icon-box security-c">
                <ShieldAlert size={20} color="#dc2626" />
              </div>
              <div className="contact-info">
                <div className="contact-role">Campus Security Post</div>
                <div className="contact-name">Main Gate / Patrol</div>
                <div className="contact-number">+91 94005 55667</div>
              </div>
              <a href="tel:+919400555667" className="call-action-btn red">
                <PhoneCall size={16} />
                <span>Call</span>
              </a>
            </div>

            <div className="contact-tile">
              <div className="contact-icon-box office-c">
                <Building2 size={20} color="#0d9488" />
              </div>
              <div className="contact-info">
                <div className="contact-role">Hostel Administration Office</div>
                <div className="contact-name">Block A Ground Floor</div>
                <div className="contact-number">+91 98000 12345</div>
              </div>
              <a href="tel:+919800012345" className="call-action-btn">
                <PhoneCall size={16} />
                <span>Call</span>
              </a>
            </div>
          </div>
        </div>
      </div>

      <style>{`
        .emergency-page {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .emergency-layout-grid {
          display: grid;
          grid-template-columns: 1.3fr 1fr;
          gap: 24px;
        }

        .emergency-main-card {
          padding: 32px;
          border-top: 4px solid #ef4444;
        }

        .emergency-badge-banner {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          background: #fef2f2;
          border: 1px solid #fecaca;
          color: #b91c1c;
          padding: 4px 12px;
          border-radius: var(--radius-full);
          font-size: 0.76rem;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 0.04em;
          margin-bottom: 16px;
        }

        .emergency-headline {
          font-size: 1.65rem;
          font-weight: 800;
          color: #0f172a;
          margin-bottom: 8px;
        }

        .emergency-lead-text {
          font-size: 0.95rem;
          color: var(--text-muted);
          line-height: 1.5;
          margin-bottom: 24px;
        }

        .emergency-room-box {
          display: flex;
          align-items: center;
          justify-content: space-between;
          background: #f8fafc;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
          padding: 14px 18px;
          margin-bottom: 28px;
        }

        .room-pin-label {
          font-size: 0.82rem;
          color: var(--text-muted);
        }

        .room-pin-val {
          font-size: 0.95rem;
          color: var(--accent-blue);
        }

        .trigger-action-area {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .raise-emergency-btn {
          width: 100%;
          padding: 18px;
          border-radius: var(--radius-lg);
          background: #dc2626;
          color: #ffffff;
          font-size: 1.15rem;
          font-weight: 800;
          letter-spacing: 0.04em;
          border: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 12px;
          box-shadow: 0 8px 24px -4px rgba(220, 38, 38, 0.4);
          transition: all 0.2s ease;
        }

        .raise-emergency-btn:hover {
          background: #b91c1c;
          transform: translateY(-2px);
          box-shadow: 0 12px 28px -4px rgba(220, 38, 38, 0.5);
        }

        .emergency-disclaimer {
          font-size: 0.78rem;
          color: var(--text-muted);
          text-align: center;
        }

        .emergency-active-alert {
          display: flex;
          gap: 14px;
          background: #f0fdf4;
          border: 1px solid #bbf7d0;
          border-radius: var(--radius-md);
          padding: 20px;
        }

        .alert-sent-title {
          font-size: 1.05rem;
          font-weight: 800;
          color: #15803d;
          margin-bottom: 4px;
        }

        .alert-sent-desc {
          font-size: 0.85rem;
          color: #166534;
          line-height: 1.5;
        }

        /* Contacts List */
        .contacts-list {
          display: flex;
          flex-direction: column;
          gap: 14px;
          margin-top: 14px;
        }

        .contact-tile {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 14px 16px;
          background: #f8fafc;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
        }

        .contact-icon-box {
          width: 40px;
          height: 40px;
          border-radius: var(--radius-md);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }

        .contact-icon-box.warden-c { background: #eff6ff; }
        .contact-icon-box.security-c { background: #fef2f2; }
        .contact-icon-box.office-c { background: #f0fdfa; }

        .contact-info {
          flex: 1;
          margin-left: 12px;
        }

        .contact-role {
          font-size: 0.72rem;
          font-weight: 700;
          color: var(--text-muted);
          text-transform: uppercase;
        }

        .contact-name {
          font-size: 0.9rem;
          font-weight: 700;
          color: var(--text-main);
        }

        .contact-number {
          font-size: 0.78rem;
          color: var(--accent-blue);
          font-weight: 600;
        }

        .call-action-btn {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 6px 12px;
          border-radius: var(--radius-sm);
          font-size: 0.78rem;
          font-weight: 700;
          background: #eff6ff;
          color: var(--accent-blue);
          text-decoration: none;
          transition: background 0.15s ease;
        }

        .call-action-btn.red {
          background: #fef2f2;
          color: #dc2626;
        }

        .live-status-pill {
          font-size: 0.72rem;
          font-weight: 700;
          background: #dcfce7;
          color: #166534;
          padding: 3px 8px;
          border-radius: var(--radius-full);
        }

        @media (max-width: 1024px) {
          .emergency-layout-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
};

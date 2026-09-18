import React, { useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/apiService';
import { ComplaintTimeline } from '../components/ComplaintTimeline';
import { 
  Wrench, 
  Zap, 
  Droplets, 
  UtensilsCrossed, 
  AlertTriangle, 
  Send, 
  CheckCircle2, 
  ArrowRight,
  ShieldCheck,
  Cpu
} from 'lucide-react';

export const RaiseComplaintScreen = () => {
  const { user, setActiveTab } = useAuth();
  const [complaintType, setComplaintType] = useState('Maintenance');
  const [roomNo, setRoomNo] = useState(user?.roomNo || 'A-203');
  const [description, setDescription] = useState('AC is not working in Room A-203');
  const [priority, setPriority] = useState('HIGH');
  const [submitting, setSubmitting] = useState(false);
  const [createdComplaint, setCreatedComplaint] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!description.trim()) {
      alert('Please provide a description of the issue.');
      return;
    }

    setSubmitting(true);
    try {
      // Calls backend REST API: Student -> Factory creates complaint -> Singleton central system -> Chain routes complaint -> Technician Raj assigned -> DB stored -> Observer notifies
      const res = await api.createComplaint({
        type: complaintType,
        roomNo: roomNo,
        description: description,
        priority: priority
      });

      setCreatedComplaint(res);
    } catch (err) {
      alert('Failed to submit complaint: ' + err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const types = [
    { label: 'Maintenance', value: 'Maintenance', icon: <Wrench size={18} />, color: '#ea580c' },
    { label: 'Electricity', value: 'Electricity', icon: <Zap size={18} />, color: '#2563eb' },
    { label: 'Water / Plumbing', value: 'Water', icon: <Droplets size={18} />, color: '#0d9488' },
    { label: 'Mess / Food', value: 'Mess', icon: <UtensilsCrossed size={18} />, color: '#d97706' },
    { label: 'Emergency', value: 'Emergency', icon: <AlertTriangle size={18} />, color: '#dc2626' }
  ];

  const priorities = [
    { label: 'Low', value: 'LOW', color: '#10b981' },
    { label: 'Medium', value: 'MEDIUM', color: '#f59e0b' },
    { label: 'High', value: 'HIGH', color: '#f97316' },
    { label: 'Critical', value: 'CRITICAL', color: '#ef4444' }
  ];

  return (
    <div className="raise-complaint-container">
      {/* Title & Subtitle */}
      <div className="page-header-box">
        <h1 className="page-title">Raise a Complaint</h1>
        <p className="page-subtitle">Tell us what’s the issue. We’ll automatically route it and assign the right specialist.</p>
      </div>

      <div className="complaint-layout-row">
        {/* Left: Form */}
        <div className="card form-card">
          <form onSubmit={handleSubmit} className="complaint-form">
            {/* Complaint Type Selection */}
            <div className="form-group">
              <label className="form-label">Select Complaint Type</label>
              <div className="types-grid">
                {types.map(t => (
                  <button
                    key={t.value}
                    type="button"
                    className={`type-select-card ${complaintType === t.value ? 'selected' : ''}`}
                    onClick={() => setComplaintType(t.value)}
                  >
                    <div className="type-icon-box" style={{ color: t.color }}>
                      {t.icon}
                    </div>
                    <span className="type-card-name">{t.label}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Room Number & Priority in Row */}
            <div className="form-row-2">
              <div className="form-group">
                <label className="form-label">Room Number</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={roomNo} 
                  onChange={(e) => setRoomNo(e.target.value)}
                  placeholder="e.g. A-203"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Priority Level</label>
                <div className="priority-pills">
                  {priorities.map(p => (
                    <button
                      key={p.value}
                      type="button"
                      className={`priority-btn ${priority === p.value ? 'active' : ''}`}
                      onClick={() => setPriority(p.value)}
                    >
                      {p.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Description */}
            <div className="form-group">
              <label className="form-label">Issue Description</label>
              <textarea 
                className="form-textarea"
                rows="4"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Describe the issue clearly (e.g. AC is not cooling properly in room A-203)..."
                required
              />
              <div className="form-helper">
                💡 <em>Example: "AC is not working in Room A-203" automatically activates Electrical department assignment.</em>
              </div>
            </div>

            {/* Quick Demonstration Preset Buttons */}
            <div className="presets-box">
              <span className="preset-label">Quick Test Scenarios:</span>
              <button 
                type="button" 
                className="preset-pill"
                onClick={() => {
                  setComplaintType('Maintenance');
                  setDescription('AC is not working in Room A-203');
                  setPriority('HIGH');
                }}
              >
                ❄️ AC Issue in Room A-203
              </button>
              <button 
                type="button" 
                className="preset-pill"
                onClick={() => {
                  setComplaintType('Water');
                  setDescription('Water leakage under bathroom sink');
                  setPriority('MEDIUM');
                }}
              >
                💧 Plumbing Leakage
              </button>
              <button 
                type="button" 
                className="preset-pill"
                onClick={() => {
                  setComplaintType('Mess');
                  setDescription('Mess lunch was served late');
                  setPriority('LOW');
                }}
              >
                🍲 Mess Complaint
              </button>
            </div>

            {/* Submit Button */}
            <button 
              type="submit" 
              className="btn btn-primary btn-submit-large"
              disabled={submitting}
            >
              <Send size={18} />
              <span>{submitting ? 'Processing via Chain of Responsibility...' : 'Submit Complaint'}</span>
            </button>
          </form>
        </div>

        {/* Right: Submission Live Feedback Card */}
        <div className="result-column">
          {createdComplaint ? (
            <div className="card live-feedback-card">
              <div className="success-banner">
                <CheckCircle2 size={28} color="#16a34a" />
                <div>
                  <h3 className="success-title">Complaint Successfully Dispatched!</h3>
                  <p className="success-sub">Processed by HostelHub Design Pattern Pipeline</p>
                </div>
              </div>

              <div className="feedback-details-table">
                <div className="feedback-row">
                  <span className="f-label">Complaint ID:</span>
                  <span className="f-val bold text-blue">#{createdComplaint.id}</span>
                </div>
                <div className="feedback-row">
                  <span className="f-label">Room Number:</span>
                  <span className="f-val bold">{createdComplaint.roomNo}</span>
                </div>
                <div className="feedback-row">
                  <span className="f-label">Identified Department:</span>
                  <span className="f-val bold text-blue">{createdComplaint.department}</span>
                </div>
                <div className="feedback-row">
                  <span className="f-label">Assigned Technician:</span>
                  <span className="f-val bold text-green">
                    {createdComplaint.technicianName || 'Raj (Electrical)'}
                  </span>
                </div>
                <div className="feedback-row">
                  <span className="f-label">Current Status:</span>
                  <span className="status-chip assigned">{createdComplaint.status}</span>
                </div>
                <div className="feedback-row">
                  <span className="f-label">Observer Notification:</span>
                  <span className="f-val text-muted">Sent to Student & Warden Dashboard</span>
                </div>
              </div>

              <div className="timeline-embed-wrap">
                <ComplaintTimeline complaint={createdComplaint} />
              </div>

              <button 
                className="btn btn-secondary btn-full"
                onClick={() => setActiveTab('my-complaints')}
              >
                <span>View in My Complaints</span>
                <ArrowRight size={16} />
              </button>
            </div>
          ) : (
            <div className="card pattern-explanation-card">
              <div className="pattern-header">
                <Cpu size={22} color="#2563eb" />
                <h3>Design Pattern Workflow</h3>
              </div>
              <p className="pattern-sub">How your complaint travels through the backend:</p>

              <div className="pattern-steps-list">
                <div className="pattern-step">
                  <div className="p-num">1</div>
                  <div className="p-info">
                    <strong>Proxy Pattern</strong>
                    <span>Validates student authentication and enforces role boundaries.</span>
                  </div>
                </div>

                <div className="pattern-step">
                  <div className="p-num">2</div>
                  <div className="p-info">
                    <strong>Factory Method</strong>
                    <span>Instantiates specific subclass (MaintenanceComplaint, etc.).</span>
                  </div>
                </div>

                <div className="pattern-step">
                  <div className="p-num">3</div>
                  <div className="p-info">
                    <strong>Singleton System</strong>
                    <span>One central HostelHub controller coordinates the lifecycle.</span>
                  </div>
                </div>

                <div className="pattern-step">
                  <div className="p-num">4</div>
                  <div className="p-info">
                    <strong>Chain of Responsibility</strong>
                    <span>Routes from MaintenanceHandler to ElectricalHandler for AC.</span>
                  </div>
                </div>

                <div className="pattern-step">
                  <div className="p-num">5</div>
                  <div className="p-info">
                    <strong>Observer Pattern</strong>
                    <span>Dispatches notifications and live updates to dashboards.</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      <style>{`
        .raise-complaint-container {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .page-header-box {
          margin-bottom: 4px;
        }

        .page-title {
          font-size: 1.85rem;
          font-weight: 800;
          color: var(--text-main);
          margin-bottom: 6px;
        }

        .page-subtitle {
          color: var(--text-muted);
          font-size: 0.95rem;
        }

        .complaint-layout-row {
          display: grid;
          grid-template-columns: 1.3fr 1fr;
          gap: 24px;
          align-items: start;
        }

        .form-card {
          padding: 28px;
        }

        .complaint-form {
          display: flex;
          flex-direction: column;
          gap: 22px;
        }

        .form-group {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .form-label {
          font-size: 0.85rem;
          font-weight: 700;
          color: var(--text-main);
        }

        .types-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 10px;
        }

        .type-select-card {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 8px;
          padding: 14px 10px;
          background: #f8fafc;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
          cursor: pointer;
          transition: all 0.15s ease;
        }

        .type-select-card:hover {
          background: #ffffff;
          border-color: #cbd5e1;
        }

        .type-select-card.selected {
          background: #eff6ff;
          border-color: var(--accent-blue);
          box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
        }

        .type-card-name {
          font-size: 0.78rem;
          font-weight: 700;
          color: var(--text-main);
        }

        .form-row-2 {
          display: grid;
          grid-template-columns: 1fr 1.4fr;
          gap: 16px;
        }

        .form-input, .form-textarea {
          width: 100%;
          padding: 12px 14px;
          border-radius: var(--radius-md);
          border: 1px solid var(--border-light);
          background: #f8fafc;
          color: var(--text-main);
          font-family: inherit;
          font-size: 0.9rem;
          outline: none;
          transition: all 0.15s ease;
        }

        .form-input:focus, .form-textarea:focus {
          border-color: var(--accent-blue);
          background: #ffffff;
          box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
        }

        .priority-pills {
          display: flex;
          gap: 6px;
        }

        .priority-btn {
          flex: 1;
          padding: 11px 8px;
          border-radius: var(--radius-sm);
          font-size: 0.8rem;
          font-weight: 700;
          border: 1px solid var(--border-light);
          background: #f8fafc;
          cursor: pointer;
          transition: all 0.15s ease;
        }

        .priority-btn.active {
          background: #eff6ff;
          color: var(--accent-blue);
          border-color: var(--accent-blue);
        }

        .form-helper {
          font-size: 0.78rem;
          color: var(--text-muted);
        }

        .presets-box {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-wrap: wrap;
          padding: 12px;
          background: #f8fafc;
          border-radius: var(--radius-md);
        }

        .preset-label {
          font-size: 0.75rem;
          font-weight: 700;
          color: var(--text-muted);
        }

        .preset-pill {
          background: #ffffff;
          border: 1px solid var(--border-light);
          padding: 5px 10px;
          border-radius: var(--radius-full);
          font-size: 0.75rem;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.15s ease;
        }

        .preset-pill:hover {
          border-color: var(--accent-blue);
          color: var(--accent-blue);
        }

        .btn-submit-large {
          padding: 14px;
          font-size: 0.96rem;
        }

        /* Live Feedback Card */
        .live-feedback-card {
          display: flex;
          flex-direction: column;
          gap: 20px;
          background: #ffffff;
        }

        .success-banner {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px;
          background: #f0fdf4;
          border: 1px solid #bbf7d0;
          border-radius: var(--radius-md);
        }

        .success-title {
          font-size: 1rem;
          font-weight: 800;
          color: #15803d;
        }

        .success-sub {
          font-size: 0.76rem;
          color: #166534;
        }

        .feedback-details-table {
          display: flex;
          flex-direction: column;
          gap: 10px;
          background: #f8fafc;
          border-radius: var(--radius-md);
          padding: 16px;
        }

        .feedback-row {
          display: flex;
          justify-content: space-between;
          font-size: 0.85rem;
          padding-bottom: 8px;
          border-bottom: 1px solid var(--border-light);
        }

        .feedback-row:last-child {
          border-bottom: none;
          padding-bottom: 0;
        }

        .f-label {
          color: var(--text-muted);
        }

        .f-val {
          color: var(--text-main);
        }

        .bold { font-weight: 700; }
        .text-blue { color: var(--accent-blue); }
        .text-green { color: var(--accent-green); }

        .status-chip {
          padding: 2px 8px;
          border-radius: var(--radius-full);
          font-size: 0.72rem;
          font-weight: 700;
          text-transform: uppercase;
        }

        .status-chip.assigned {
          background: #dbeafe;
          color: #1e40af;
        }

        .btn-full {
          width: 100%;
        }

        /* Pattern Explanation Card */
        .pattern-explanation-card {
          padding: 24px;
        }

        .pattern-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 6px;
        }

        .pattern-sub {
          font-size: 0.82rem;
          color: var(--text-muted);
          margin-bottom: 18px;
        }

        .pattern-steps-list {
          display: flex;
          flex-direction: column;
          gap: 14px;
        }

        .pattern-step {
          display: flex;
          gap: 12px;
          align-items: flex-start;
        }

        .p-num {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #eff6ff;
          color: var(--accent-blue);
          font-size: 0.75rem;
          font-weight: 800;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          border: 1px solid #bfdbfe;
        }

        .p-info {
          display: flex;
          flex-direction: column;
          font-size: 0.8rem;
          line-height: 1.4;
        }

        .p-info strong {
          color: var(--text-main);
        }

        .p-info span {
          color: var(--text-muted);
        }

        @media (max-width: 1024px) {
          .complaint-layout-row {
            grid-template-columns: 1fr;
          }
          .types-grid {
            grid-template-columns: repeat(2, 1fr);
          }
        }
      `}</style>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { api } from '../api/apiService';
import { Wrench, Plus, Phone, CheckCircle2, Clock, X } from 'lucide-react';

export const TechnicianManagementScreen = () => {
  const [technicians, setTechnicians] = useState([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [name, setName] = useState('');
  const [department, setDepartment] = useState('Electrical');
  const [contact, setContact] = useState('');

  useEffect(() => {
    loadTechnicians();
  }, []);

  const loadTechnicians = async () => {
    try {
      const data = await api.getTechnicians();
      if (Array.isArray(data)) setTechnicians(data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      await api.addTechnician({ name, department, contact, status: 'AVAILABLE' });
      setShowAddModal(false);
      setName('');
      setContact('');
      loadTechnicians();
    } catch (e) {
      alert('Failed to add technician: ' + e.message);
    }
  };

  const handleToggleStatus = async (techId, currentStatus) => {
    const nextStatus = currentStatus === 'AVAILABLE' ? 'ASSIGNED' : 'AVAILABLE';
    try {
      await api.updateTechnicianStatus(techId, nextStatus);
      loadTechnicians();
    } catch (e) {
      alert('Failed to update status: ' + e.message);
    }
  };

  return (
    <div className="tech-mgmt-page">
      <div className="page-header-flex">
        <div>
          <h1 className="page-title">Technician Management</h1>
          <p className="page-subtitle">Configure maintenance personnel across Electrical, Plumbing, Mess, and Security departments.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowAddModal(true)}>
          <Plus size={16} />
          <span>Add Technician</span>
        </button>
      </div>

      <div className="tech-cards-grid">
        {technicians.map(t => (
          <div key={t.id} className="card tech-card">
            <div className="tech-header">
              <div className="tech-avatar">{t.name.charAt(0)}</div>
              <div className="tech-meta">
                <h3 className="tech-title">{t.name}</h3>
                <span className="tech-badge">{t.department} Department</span>
              </div>
            </div>

            <div className="tech-stats-row">
              <div className="t-stat">
                <span className="t-label">Status</span>
                <span className={`t-pill ${t.status === 'AVAILABLE' ? 'avail' : 'busy'}`}>
                  {t.status}
                </span>
              </div>
              <div className="t-stat">
                <span className="t-label">Active Workload</span>
                <strong className="t-count">{t.activeTasks || 0} Tasks</strong>
              </div>
            </div>

            <div className="tech-contact-row">
              <Phone size={14} color="#64748b" />
              <span>{t.contact || '+91 94000 00000'}</span>
            </div>

            <div className="tech-actions">
              <button 
                className="btn btn-secondary btn-sm full"
                onClick={() => handleToggleStatus(t.id, t.status)}
              >
                Toggle {t.status === 'AVAILABLE' ? 'to Assigned' : 'to Available'}
              </button>
            </div>
          </div>
        ))}
      </div>

      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add New Technician</h3>
              <button className="close-btn" onClick={() => setShowAddModal(false)}><X size={18} /></button>
            </div>
            <form onSubmit={handleAdd} className="modal-form">
              <div className="form-group">
                <label className="form-label">Full Name</label>
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="e.g. Raj" 
                  value={name} 
                  onChange={e => setName(e.target.value)} 
                  required 
                />
              </div>
              <div className="form-group">
                <label className="form-label">Department</label>
                <select className="form-input" value={department} onChange={e => setDepartment(e.target.value)}>
                  <option value="Electrical">Electrical</option>
                  <option value="Plumbing">Plumbing</option>
                  <option value="Mess">Mess</option>
                  <option value="Maintenance">Maintenance</option>
                  <option value="Security">Security</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Contact Phone</label>
                <input 
                  type="tel" 
                  className="form-input" 
                  placeholder="+91 94000 12345" 
                  value={contact} 
                  onChange={e => setContact(e.target.value)} 
                  required 
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowAddModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Technician</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <style>{`
        .tech-mgmt-page {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .page-header-flex {
          display: flex;
          align-items: center;
          justify-content: space-between;
        }

        .tech-cards-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(min(100%, 250px), 1fr));
          gap: 16px;
        }

        .tech-card {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .tech-header {
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .tech-avatar {
          width: 44px;
          height: 44px;
          border-radius: var(--radius-md);
          background: #eff6ff;
          color: var(--accent-blue);
          font-weight: 800;
          font-size: 1.1rem;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 1px solid #bfdbfe;
        }

        .tech-title {
          font-size: 1.05rem;
          font-weight: 700;
          color: var(--text-main);
        }

        .tech-badge {
          font-size: 0.76rem;
          color: var(--accent-blue);
          font-weight: 600;
        }

        .tech-stats-row {
          display: flex;
          justify-content: space-between;
          padding: 12px;
          background: #f8fafc;
          border-radius: var(--radius-md);
        }

        .t-stat {
          display: flex;
          flex-direction: column;
          gap: 4px;
        }

        .t-label {
          font-size: 0.7rem;
          color: var(--text-muted);
          text-transform: uppercase;
        }

        .t-pill {
          font-size: 0.74rem;
          font-weight: 700;
          padding: 2px 8px;
          border-radius: var(--radius-full);
          display: inline-block;
        }

        .t-pill.avail {
          background: #dcfce7;
          color: #15803d;
        }

        .t-pill.busy {
          background: #eff6ff;
          color: #1e40af;
        }

        .t-count {
          font-size: 0.88rem;
          color: var(--text-main);
        }

        .tech-contact-row {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 0.82rem;
          color: var(--text-muted);
        }

        .btn-sm.full {
          width: 100%;
        }

        .modal-overlay {
          position: fixed;
          inset: 0;
          background: rgba(15, 23, 42, 0.5);
          backdrop-filter: blur(2px);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 60;
          padding: 20px;
        }

        .modal-box {
          background: #ffffff;
          border-radius: var(--radius-lg);
          box-shadow: var(--shadow-modal);
          width: 100%;
          max-width: 440px;
          padding: 24px;
        }

        .modal-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding-bottom: 16px;
          border-bottom: 1px solid var(--border-light);
          margin-bottom: 18px;
        }

        .close-btn {
          background: transparent;
          border: none;
          cursor: pointer;
          color: var(--text-muted);
        }

        .modal-form {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .modal-actions {
          display: flex;
          align-items: center;
          justify-content: flex-end;
          gap: 10px;
          margin-top: 10px;
        }
      `}</style>
    </div>
  );
};

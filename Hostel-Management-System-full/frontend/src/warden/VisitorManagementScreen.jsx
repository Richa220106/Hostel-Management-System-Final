import React, { useState, useEffect } from 'react';
import { api } from '../api/apiService';
import { Users, Search, Check, LogOut, CheckCircle2 } from 'lucide-react';

export const VisitorManagementScreen = () => {
  const [visitors, setVisitors] = useState([]);
  const [search, setSearch] = useState('');

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

  const handleUpdateStatus = async (id, status) => {
    try {
      await api.updateVisitorStatus(id, status);
      loadVisitors();
    } catch (e) {
      alert('Error updating visitor status: ' + e.message);
    }
  };

  const filtered = visitors.filter(v => 
    v.visitorName.toLowerCase().includes(search.toLowerCase()) ||
    v.roomNo.toLowerCase().includes(search.toLowerCase()) ||
    (v.studentName && v.studentName.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="v-mgmt-page">
      <div className="page-header-flex">
        <div>
          <h1 className="page-title">Visitor Management Log</h1>
          <p className="page-subtitle">Warden administrative log of campus visitors, entries, approvals, and check-outs.</p>
        </div>
      </div>

      <div className="card table-card">
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Visitor Name</th>
                <th>Student Host</th>
                <th>Room</th>
                <th>Contact</th>
                <th>Visit Date</th>
                <th>Entry - Exit</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Gate Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(v => (
                <tr key={v.id}>
                  <td><strong>{v.visitorName}</strong></td>
                  <td>{v.studentName || 'Student'}</td>
                  <td><span className="room-pill-v">{v.roomNo}</span></td>
                  <td>{v.contact}</td>
                  <td>{v.visitDate}</td>
                  <td>{v.entryTime} - {v.exitTime}</td>
                  <td>{v.reason}</td>
                  <td>
                    <span className={`status-pill ${v.status === 'APPROVED' ? 'approved' : 'exited'}`}>
                      {v.status}
                    </span>
                  </td>
                  <td>
                    {v.status === 'APPROVED' ? (
                      <button 
                        className="btn btn-secondary btn-sm"
                        onClick={() => handleUpdateStatus(v.id, 'EXITED')}
                      >
                        <LogOut size={13} />
                        <span>Check Out</span>
                      </button>
                    ) : (
                      <span className="text-muted text-sm">Checked Out</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <style>{`
        .v-mgmt-page {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .page-header-flex {
          display: flex;
          align-items: center;
          justify-content: space-between;
        }

        .room-pill-v {
          background: #eff6ff;
          color: var(--accent-blue);
          font-weight: 700;
          padding: 2px 8px;
          border-radius: var(--radius-sm);
        }

        .status-pill {
          font-size: 0.72rem;
          font-weight: 700;
          padding: 3px 10px;
          border-radius: var(--radius-full);
        }

        .status-pill.approved {
          background: #dcfce7;
          color: #15803d;
        }

        .status-pill.exited {
          background: #f1f5f9;
          color: var(--text-muted);
        }

        .text-sm {
          font-size: 0.78rem;
        }
      `}</style>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { api } from '../api/apiService';
import { StatusBadge } from '../components/StatusBadge';
import { ComplaintTimeline } from '../components/ComplaintTimeline';
import { Search, Filter, Wrench, Zap, Droplets, UtensilsCrossed, AlertTriangle, ArrowRight, X } from 'lucide-react';

export const MyComplaintsScreen = () => {
  const [complaints, setComplaints] = useState([]);
  const [activeFilter, setActiveFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedComplaint, setSelectedComplaint] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchComplaints();
  }, []);

  const fetchComplaints = async () => {
    setLoading(true);
    try {
      const data = await api.getComplaints();
      if (Array.isArray(data)) {
        setComplaints(data);
        if (data.length > 0 && !selectedComplaint) {
          setSelectedComplaint(data[0]);
        }
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = complaints.filter(c => {
    const matchesFilter = 
      activeFilter === 'ALL' || 
      c.status?.toUpperCase() === activeFilter.toUpperCase();

    const matchesSearch = 
      c.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.roomNo.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.type.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (c.department && c.department.toLowerCase().includes(searchQuery.toLowerCase()));

    return matchesFilter && matchesSearch;
  });

  return (
    <div className="my-complaints-page">
      <div className="page-header-box">
        <h1 className="page-title">My Complaints</h1>
        <p className="page-subtitle">Track, review, and monitor your submitted maintenance and hostel requests.</p>
      </div>

      {/* Filter and Search Bar */}
      <div className="filter-controls-card card">
        <div className="search-box">
          <Search size={18} color="#64748b" />
          <input 
            type="text" 
            placeholder="Search by issue, room, or department..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="filter-search-input"
          />
        </div>

        <div className="filter-tabs">
          {['ALL', 'PENDING', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED'].map(tab => (
            <button
              key={tab}
              className={`filter-tab-btn ${activeFilter === tab ? 'active' : ''}`}
              onClick={() => setActiveFilter(tab)}
            >
              {tab.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* 2-Column Layout: List on Left, Timeline Details on Right */}
      <div className="complaints-two-col">
        {/* Left Column: Complaint Cards */}
        <div className="complaints-list-col">
          {filtered.length === 0 ? (
            <div className="card empty-card">
              <p>No complaints match your selection.</p>
            </div>
          ) : (
            filtered.map(c => (
              <div 
                key={c.id} 
                className={`card complaint-item-selectable ${selectedComplaint?.id === c.id ? 'active' : ''}`}
                onClick={() => setSelectedComplaint(c)}
              >
                <div className="c-item-top">
                  <div className="c-id-type">
                    <span className="c-id">#{c.id}</span>
                    <span className="c-type-pill">{c.type}</span>
                    <span className="c-dept-pill">{c.department || 'Processing'}</span>
                  </div>
                  <StatusBadge status={c.status} />
                </div>

                <h4 className="c-desc-title">{c.description}</h4>

                <div className="c-item-bottom">
                  <div className="c-room-info">
                    <span>Room <strong>{c.roomNo}</strong></span>
                    <span>•</span>
                    <span>Priority: <strong>{c.priority}</strong></span>
                  </div>

                  {c.technicianName && (
                    <div className="c-tech-name">
                      <Wrench size={13} />
                      <span>{c.technicianName}</span>
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>

        {/* Right Column: Visual Timeline & Tracking Details */}
        <div className="complaints-detail-col">
          {selectedComplaint ? (
            <div className="sticky-detail-wrap">
              <ComplaintTimeline complaint={selectedComplaint} />
            </div>
          ) : (
            <div className="card placeholder-card">
              <p>Select a complaint from the left to view detailed lifecycle tracking.</p>
            </div>
          )}
        </div>
      </div>

      <style>{`
        .my-complaints-page {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .filter-controls-card {
          padding: 16px 20px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 16px;
          flex-wrap: wrap;
        }

        .search-box {
          display: flex;
          align-items: center;
          gap: 10px;
          background: #f8fafc;
          border: 1px solid var(--border-light);
          padding: 8px 16px;
          border-radius: var(--radius-full);
          flex: 1;
          max-width: 420px;
        }

        .filter-search-input {
          border: none;
          background: transparent;
          outline: none;
          font-size: 0.88rem;
          color: var(--text-main);
          width: 100%;
        }

        .filter-tabs {
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
        }

        .filter-tab-btn {
          padding: 7px 14px;
          border-radius: var(--radius-full);
          font-size: 0.78rem;
          font-weight: 700;
          border: 1px solid var(--border-light);
          background: #ffffff;
          color: var(--text-muted);
          cursor: pointer;
          transition: all 0.15s ease;
        }

        .filter-tab-btn.active {
          background: var(--accent-blue);
          color: #ffffff;
          border-color: var(--accent-blue);
        }

        .complaints-two-col {
          display: grid;
          grid-template-columns: 1.2fr 1fr;
          gap: 24px;
          align-items: start;
        }

        .complaints-list-col {
          display: flex;
          flex-direction: column;
          gap: 14px;
        }

        .complaint-item-selectable {
          padding: 18px;
          cursor: pointer;
          border: 1px solid var(--border-light);
          transition: all 0.15s ease;
        }

        .complaint-item-selectable:hover {
          border-color: #cbd5e1;
          transform: translateY(-1px);
        }

        .complaint-item-selectable.active {
          border-color: var(--accent-blue);
          box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
          background: #ffffff;
        }

        .c-item-top {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 10px;
        }

        .c-id-type {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .c-id {
          font-size: 0.8rem;
          font-weight: 800;
          color: var(--accent-blue);
        }

        .c-type-pill {
          background: #f1f5f9;
          color: var(--text-main);
          font-size: 0.72rem;
          font-weight: 700;
          padding: 2px 8px;
          border-radius: var(--radius-sm);
        }

        .c-dept-pill {
          background: #eff6ff;
          color: var(--accent-blue);
          font-size: 0.72rem;
          font-weight: 700;
          padding: 2px 8px;
          border-radius: var(--radius-sm);
        }

        .c-desc-title {
          font-size: 0.98rem;
          color: var(--text-main);
          margin-bottom: 12px;
          font-weight: 700;
        }

        .c-item-bottom {
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 0.8rem;
          color: var(--text-muted);
          border-top: 1px solid #f1f5f9;
          padding-top: 10px;
        }

        .c-room-info {
          display: flex;
          align-items: center;
          gap: 6px;
        }

        .c-tech-name {
          display: flex;
          align-items: center;
          gap: 5px;
          color: var(--accent-blue);
          font-weight: 700;
        }

        .sticky-detail-wrap {
          position: sticky;
          top: 96px;
        }

        .empty-card, .placeholder-card {
          text-align: center;
          padding: 40px;
          color: var(--text-muted);
        }

        @media (max-width: 1024px) {
          .complaints-two-col {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
};

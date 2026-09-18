import React, { useState, useEffect } from 'react';
import { api } from '../api/apiService';
import { DoorClosed, Plus, Search, Filter, CheckCircle2, AlertCircle, Users, X } from 'lucide-react';

export const RoomManagementScreen = () => {
  const [rooms, setRooms] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [blockFilter, setBlockFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [showAddModal, setShowAddModal] = useState(false);
  const [showAllocateModal, setShowAllocateModal] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);

  // Form states
  const [newRoomNo, setNewRoomNo] = useState('');
  const [newBlock, setNewBlock] = useState('Block A');
  const [newCapacity, setNewCapacity] = useState('2');
  const [allocStudentId, setAllocStudentId] = useState('2');

  useEffect(() => {
    loadRooms();
  }, []);

  const loadRooms = async () => {
    try {
      const data = await api.getRooms();
      if (Array.isArray(data)) setRooms(data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleAddRoom = async (e) => {
    e.preventDefault();
    try {
      await api.addRoom({
        roomNo: newRoomNo,
        block: newBlock,
        capacity: Number(newCapacity),
        currentOccupancy: 0,
        status: 'Available'
      });
      setShowAddModal(false);
      setNewRoomNo('');
      loadRooms();
    } catch (e) {
      alert('Failed to add room: ' + e.message);
    }
  };

  const handleAllocate = async (e) => {
    e.preventDefault();
    if (!selectedRoom) return;
    try {
      await api.allocateRoom(Number(allocStudentId), selectedRoom.roomNo);
      setShowAllocateModal(false);
      loadRooms();
      alert(`Student allocated to room ${selectedRoom.roomNo}!`);
    } catch (e) {
      alert('Failed to allocate: ' + e.message);
    }
  };

  const filtered = rooms.filter(r => {
    const matchesSearch = r.roomNo.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          r.block.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesBlock = blockFilter === 'ALL' || r.block.includes(blockFilter);
    const matchesStatus = statusFilter === 'ALL' || r.status.toLowerCase() === statusFilter.toLowerCase();
    return matchesSearch && matchesBlock && matchesStatus;
  });

  return (
    <div className="room-mgmt-page">
      <div className="page-header-flex">
        <div>
          <h1 className="page-title">Room Management</h1>
          <p className="page-subtitle">Inspect hostel capacity, monitor room occupancy, and allocate student residences.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowAddModal(true)}>
          <Plus size={16} />
          <span>Add New Room</span>
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div className="filter-bar card">
        <div className="search-box">
          <Search size={18} color="#64748b" />
          <input 
            type="text" 
            placeholder="Search by Room No or Block..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="filters-group">
          <div className="filter-select-wrap">
            <span className="filter-label">Block:</span>
            <select value={blockFilter} onChange={(e) => setBlockFilter(e.target.value)}>
              <option value="ALL">All Blocks</option>
              <option value="Block A">Block A</option>
              <option value="Block B">Block B</option>
              <option value="Block C">Block C</option>
            </select>
          </div>

          <div className="filter-select-wrap">
            <span className="filter-label">Status:</span>
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="ALL">All Status</option>
              <option value="Available">Available</option>
              <option value="Occupied">Occupied</option>
            </select>
          </div>
        </div>
      </div>

      {/* Rooms Table */}
      <div className="card table-card">
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Room No</th>
                <th>Block</th>
                <th>Capacity</th>
                <th>Occupied</th>
                <th>Available</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(r => (
                <tr key={r.roomNo}>
                  <td>
                    <strong className="room-code">{r.roomNo}</strong>
                  </td>
                  <td>{r.block}</td>
                  <td>{r.capacity} Beds</td>
                  <td>{r.currentOccupancy}</td>
                  <td>
                    <span className={`avail-count ${r.available > 0 ? 'green' : 'gray'}`}>
                      {r.available}
                    </span>
                  </td>
                  <td>
                    <span className={`status-pill-r ${r.status === 'Available' ? 'avail' : 'occ'}`}>
                      {r.status}
                    </span>
                  </td>
                  <td>
                    <button 
                      className="btn btn-secondary btn-sm"
                      onClick={() => {
                        setSelectedRoom(r);
                        setShowAllocateModal(true);
                      }}
                    >
                      <Users size={14} />
                      <span>Allocate</span>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add Room Modal */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add New Hostel Room</h3>
              <button className="close-btn" onClick={() => setShowAddModal(false)}><X size={18} /></button>
            </div>
            <form onSubmit={handleAddRoom} className="modal-form">
              <div className="form-group">
                <label className="form-label">Room Number</label>
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="e.g. A-204" 
                  value={newRoomNo} 
                  onChange={e => setNewRoomNo(e.target.value)} 
                  required 
                />
              </div>
              <div className="form-group">
                <label className="form-label">Block</label>
                <select className="form-input" value={newBlock} onChange={e => setNewBlock(e.target.value)}>
                  <option value="Block A">Block A</option>
                  <option value="Block B">Block B</option>
                  <option value="Block C">Block C</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Capacity (Beds)</label>
                <input 
                  type="number" 
                  min="1" 
                  max="6" 
                  className="form-input" 
                  value={newCapacity} 
                  onChange={e => setNewCapacity(e.target.value)} 
                  required 
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowAddModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Room</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Allocate Room Modal */}
      {showAllocateModal && selectedRoom && (
        <div className="modal-overlay" onClick={() => setShowAllocateModal(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Allocate Room {selectedRoom.roomNo}</h3>
              <button className="close-btn" onClick={() => setShowAllocateModal(false)}><X size={18} /></button>
            </div>
            <form onSubmit={handleAllocate} className="modal-form">
              <p className="modal-hint">
                Select a student to allocate to Room <strong>{selectedRoom.roomNo}</strong> ({selectedRoom.block}).
              </p>
              <div className="form-group">
                <label className="form-label">Select Student</label>
                <select 
                  className="form-input" 
                  value={allocStudentId} 
                  onChange={e => setAllocStudentId(e.target.value)}
                >
                  <option value="2">Aarav Patel (Current: A-203)</option>
                  <option value="3">Ria Sharma (Current: A-203)</option>
                  <option value="4">Ananya Verma (Current: B-101)</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowAllocateModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Confirm Allocation</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <style>{`
        .room-mgmt-page {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .page-header-flex {
          display: flex;
          align-items: center;
          justify-content: space-between;
        }

        .filter-bar {
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
          max-width: 360px;
        }

        .search-box input {
          border: none;
          background: transparent;
          outline: none;
          font-size: 0.88rem;
          color: var(--text-main);
          width: 100%;
        }

        .filters-group {
          display: flex;
          gap: 14px;
        }

        .filter-select-wrap {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 0.82rem;
          color: var(--text-muted);
        }

        .filter-select-wrap select {
          padding: 6px 12px;
          border-radius: var(--radius-md);
          border: 1px solid var(--border-light);
          background: #ffffff;
          font-size: 0.82rem;
          color: var(--text-main);
          outline: none;
        }

        .room-code {
          color: var(--accent-blue);
          font-weight: 700;
        }

        .avail-count.green {
          color: #16a34a;
          font-weight: 700;
        }

        .avail-count.gray {
          color: var(--text-light);
        }

        .status-pill-r {
          font-size: 0.72rem;
          font-weight: 700;
          padding: 3px 10px;
          border-radius: var(--radius-full);
        }

        .status-pill-r.avail {
          background: #dcfce7;
          color: #15803d;
        }

        .status-pill-r.occ {
          background: #f1f5f9;
          color: var(--text-main);
        }

        /* Modals */
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

        .modal-hint {
          font-size: 0.85rem;
          color: var(--text-muted);
          line-height: 1.4;
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

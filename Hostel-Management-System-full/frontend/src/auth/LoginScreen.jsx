import React, { useState } from 'react';
import { useAuth } from './AuthContext';
import { Building2, Lock, User, Shield, ArrowRight, CheckCircle2 } from 'lucide-react';

export const LoginScreen = () => {
  const { login, switchRole } = useAuth();
  const [username, setUsername] = useState('student1');
  const [password, setPassword] = useState('student123');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const ok = await login(username, password);
      if (!ok) {
        setError('Invalid username or password');
      }
    } catch (err) {
      setError('Connection error: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-screen-container">
      <div className="login-card-box">
        {/* Brand */}
        <div className="login-brand-header">
          <div className="login-icon-box">
            <Building2 size={28} color="#ffffff" />
          </div>
          <h1 className="login-title">HostelHub</h1>
          <p className="login-subtitle">Smart Hostel Management & Issue-Resolution Platform</p>
        </div>

        {error && (
          <div className="login-error-alert">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label className="form-label">Username</label>
            <div className="input-with-icon">
              <User size={18} color="#64748b" />
              <input 
                type="text" 
                value={username} 
                onChange={e => setUsername(e.target.value)} 
                placeholder="Enter username" 
                required 
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <div className="input-with-icon">
              <Lock size={18} color="#64748b" />
              <input 
                type="password" 
                value={password} 
                onChange={e => setPassword(e.target.value)} 
                placeholder="Enter password" 
                required 
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary login-btn" disabled={loading}>
            <span>{loading ? 'Authenticating...' : 'Sign In to Portal'}</span>
            <ArrowRight size={18} />
          </button>
        </form>

        {/* Demo Fast Login Buttons */}
        <div className="demo-accounts-box">
          <div className="demo-title">⚡ Instant Demo Personas</div>
          <div className="demo-buttons-row">
            <button 
              type="button" 
              className="demo-btn student"
              onClick={() => switchRole('STUDENT')}
            >
              <span className="demo-role">Student</span>
              <strong>Aarav Patel</strong>
              <span className="demo-meta">Room A-203</span>
            </button>

            <button 
              type="button" 
              className="demo-btn warden"
              onClick={() => switchRole('WARDEN')}
            >
              <span className="demo-role">Warden</span>
              <strong>Chief Warden</strong>
              <span className="demo-meta">All Facilities</span>
            </button>
          </div>
        </div>
      </div>

      <style>{`
        .login-screen-container {
          min-height: 100vh;
          background: linear-gradient(135deg, #f0f4f9 0%, #e2e8f0 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 24px;
        }

        .login-card-box {
          background: #ffffff;
          border-radius: var(--radius-xl);
          box-shadow: 0 20px 45px -10px rgba(15, 23, 42, 0.12);
          border: 1px solid var(--border-light);
          padding: 40px;
          max-width: 440px;
          width: 100%;
        }

        .login-brand-header {
          display: flex;
          flex-direction: column;
          align-items: center;
          text-align: center;
          margin-bottom: 28px;
        }

        .login-icon-box {
          width: 56px;
          height: 56px;
          border-radius: var(--radius-lg);
          background: linear-gradient(135deg, #2563eb, #1d4ed8);
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 6px 18px rgba(37, 99, 235, 0.35);
          margin-bottom: 14px;
        }

        .login-title {
          font-size: 1.7rem;
          font-weight: 800;
          color: var(--text-main);
          letter-spacing: -0.02em;
        }

        .login-subtitle {
          font-size: 0.85rem;
          color: var(--text-muted);
          margin-top: 4px;
        }

        .login-error-alert {
          background: #fee2e2;
          border: 1px solid #fecaca;
          color: #b91c1c;
          padding: 10px 14px;
          border-radius: var(--radius-md);
          font-size: 0.82rem;
          margin-bottom: 18px;
        }

        .login-form {
          display: flex;
          flex-direction: column;
          gap: 18px;
        }

        .input-with-icon {
          display: flex;
          align-items: center;
          gap: 10px;
          background: #f8fafc;
          border: 1px solid var(--border-light);
          border-radius: var(--radius-md);
          padding: 10px 14px;
        }

        .input-with-icon input {
          border: none;
          background: transparent;
          outline: none;
          font-size: 0.9rem;
          color: var(--text-main);
          width: 100%;
        }

        .login-btn {
          width: 100%;
          padding: 13px;
          margin-top: 8px;
        }

        .demo-accounts-box {
          margin-top: 28px;
          padding-top: 20px;
          border-top: 1px solid var(--border-light);
        }

        .demo-title {
          font-size: 0.74rem;
          font-weight: 700;
          color: var(--text-muted);
          text-transform: uppercase;
          letter-spacing: 0.04em;
          text-align: center;
          margin-bottom: 12px;
        }

        .demo-buttons-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 10px;
        }

        .demo-btn {
          display: flex;
          flex-direction: column;
          align-items: center;
          text-align: center;
          padding: 12px 10px;
          border-radius: var(--radius-md);
          border: 1px solid var(--border-light);
          background: #f8fafc;
          cursor: pointer;
          transition: all 0.15s ease;
        }

        .demo-btn:hover {
          background: #ffffff;
          box-shadow: var(--shadow-sm);
        }

        .demo-btn.student:hover {
          border-color: var(--accent-blue);
        }

        .demo-btn.warden:hover {
          border-color: #a855f7;
        }

        .demo-role {
          font-size: 0.68rem;
          text-transform: uppercase;
          font-weight: 800;
          padding: 2px 6px;
          border-radius: var(--radius-full);
          margin-bottom: 4px;
        }

        .demo-btn.student .demo-role {
          background: #eff6ff;
          color: var(--accent-blue);
        }

        .demo-btn.warden .demo-role {
          background: #faf5ff;
          color: #9333ea;
        }

        .demo-btn strong {
          font-size: 0.85rem;
          color: var(--text-main);
        }

        .demo-meta {
          font-size: 0.72rem;
          color: var(--text-muted);
          margin-top: 2px;
        }
      `}</style>
    </div>
  );
};

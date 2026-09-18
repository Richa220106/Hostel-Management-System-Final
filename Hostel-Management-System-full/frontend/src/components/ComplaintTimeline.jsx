import React from 'react';
import { CheckCircle2, Clock, Wrench, ShieldAlert, Cpu, Check } from 'lucide-react';
import { StatusBadge } from './StatusBadge';

export const ComplaintTimeline = ({ complaint }) => {
  if (!complaint) return null;

  const status = complaint.status?.toUpperCase() || 'PENDING';

  // Determine active step (0: Submitted, 1: Dept Identified, 2: Tech Assigned, 3: In Progress, 4: Resolved)
  let currentStep = 0;
  if (status === 'RESOLVED') {
    currentStep = 4;
  } else if (status === 'IN_PROGRESS' || status === 'IN PROGRESS') {
    currentStep = 3;
  } else if (status === 'ASSIGNED') {
    currentStep = 2;
  } else if (complaint.department && complaint.department !== 'General') {
    currentStep = 1;
  }

  const steps = [
    {
      title: 'Complaint Submitted',
      desc: `Created by Student for Room ${complaint.roomNo || 'A-203'}`,
      icon: <Clock size={16} />
    },
    {
      title: `${complaint.department || 'Electrical'} Department Identified`,
      desc: 'Chain of Responsibility routed issue',
      icon: <Cpu size={16} />
    },
    {
      title: complaint.technicianName ? `Technician ${complaint.technicianName} Assigned` : 'Technician Assigned',
      desc: `${complaint.department || 'Specialist'} team dispatched`,
      icon: <Wrench size={16} />
    },
    {
      title: 'Repair In Progress',
      desc: 'Technician currently on-site working on resolution',
      icon: <Wrench size={16} />
    },
    {
      title: 'Resolution Completed',
      desc: 'Issue verified and resolved successfully',
      icon: <CheckCircle2 size={16} />
    }
  ];

  return (
    <div className="timeline-container">
      <div className="timeline-meta-bar">
        <div>
          <span className="complaint-id-tag">Complaint #{complaint.id}</span>
          <h3 className="timeline-title">{complaint.description}</h3>
          <p className="timeline-sub">Room {complaint.roomNo} • {complaint.type} • Priority: <strong>{complaint.priority}</strong></p>
        </div>
        <div>
          <StatusBadge status={complaint.status} />
        </div>
      </div>

      <div className="steps-list">
        {steps.map((step, idx) => {
          const isDone = idx <= currentStep && (currentStep === 4 || idx < currentStep);
          const isCurrent = idx === currentStep && currentStep !== 4;
          const isUpcoming = idx > currentStep;

          return (
            <div key={idx} className={`timeline-step ${isDone ? 'done' : ''} ${isCurrent ? 'current' : ''}`}>
              <div className="step-marker-col">
                <div className="step-circle">
                  {isDone ? <Check size={14} color="#ffffff" /> : (idx + 1)}
                </div>
                {idx < steps.length - 1 && <div className="step-line" />}
              </div>
              <div className="step-content">
                <div className="step-header">
                  <div className="step-title-text">{step.title}</div>
                  {isCurrent && <span className="step-status-chip">Active Now</span>}
                  {isDone && <span className="step-status-chip done">Completed</span>}
                </div>
                <div className="step-desc-text">{step.desc}</div>
              </div>
            </div>
          );
        })}
      </div>

      <style>{`
        .timeline-container {
          background: #ffffff;
          border-radius: var(--radius-lg);
          padding: 24px;
          border: 1px solid var(--border-light);
          box-shadow: var(--shadow-card);
        }

        .timeline-meta-bar {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          padding-bottom: 20px;
          margin-bottom: 24px;
          border-bottom: 1px solid var(--border-light);
        }

        .complaint-id-tag {
          display: inline-block;
          font-size: 0.76rem;
          font-weight: 700;
          color: var(--accent-blue);
          background: #eff6ff;
          padding: 3px 8px;
          border-radius: var(--radius-sm);
          margin-bottom: 6px;
        }

        .timeline-title {
          font-size: 1.12rem;
          color: var(--text-main);
          margin-bottom: 4px;
        }

        .timeline-sub {
          font-size: 0.84rem;
          color: var(--text-muted);
        }

        .steps-list {
          display: flex;
          flex-direction: column;
          gap: 0;
        }

        .timeline-step {
          display: flex;
          gap: 16px;
          min-height: 64px;
        }

        .step-marker-col {
          display: flex;
          flex-direction: column;
          align-items: center;
          width: 32px;
        }

        .step-circle {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          background: #f1f5f9;
          color: var(--text-muted);
          border: 2px solid #cbd5e1;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 0.75rem;
          font-weight: 700;
          z-index: 2;
          transition: all 0.2s ease;
        }

        .timeline-step.done .step-circle {
          background: var(--accent-green);
          border-color: var(--accent-green);
          color: #ffffff;
        }

        .timeline-step.current .step-circle {
          background: var(--accent-blue);
          border-color: var(--accent-blue);
          color: #ffffff;
          box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.2);
        }

        .step-line {
          width: 2px;
          flex: 1;
          background: #e2e8f0;
          margin: 4px 0;
        }

        .timeline-step.done .step-line {
          background: var(--accent-green);
        }

        .step-content {
          flex: 1;
          padding-bottom: 24px;
        }

        .step-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 4px;
        }

        .step-title-text {
          font-size: 0.94rem;
          font-weight: 700;
          color: var(--text-main);
        }

        .timeline-step.current .step-title-text {
          color: var(--accent-blue);
        }

        .step-status-chip {
          font-size: 0.7rem;
          font-weight: 700;
          padding: 2px 8px;
          border-radius: var(--radius-full);
          background: #eff6ff;
          color: var(--accent-blue);
        }

        .step-status-chip.done {
          background: #f0fdf4;
          color: var(--accent-green);
        }

        .step-desc-text {
          font-size: 0.82rem;
          color: var(--text-muted);
        }
      `}</style>
    </div>
  );
};

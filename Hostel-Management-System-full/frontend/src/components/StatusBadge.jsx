import React from 'react';

export const StatusBadge = ({ status }) => {
  if (!status) return null;
  const s = status.toUpperCase().replace(/\s+/g, '_');

  let badgeClass = 'badge-pending';
  let label = status;

  switch (s) {
    case 'PENDING':
      badgeClass = 'badge-pending';
      label = 'Pending';
      break;
    case 'ASSIGNED':
      badgeClass = 'badge-assigned';
      label = 'Assigned';
      break;
    case 'IN_PROGRESS':
    case 'IN PROGRESS':
      badgeClass = 'badge-in-progress';
      label = 'In Progress';
      break;
    case 'RESOLVED':
      badgeClass = 'badge-resolved';
      label = 'Resolved';
      break;
    case 'CRITICAL':
    case 'EMERGENCY':
    case 'HIGH':
      badgeClass = 'badge-emergency';
      label = status;
      break;
    default:
      badgeClass = 'badge-assigned';
      label = status;
  }

  return (
    <span className={`badge ${badgeClass}`}>
      <span className="badge-dot" />
      {label}
    </span>
  );
};

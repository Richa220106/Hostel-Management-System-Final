package com.hostelhub.memento;

import java.sql.Timestamp;

/**
 * Memento Pattern: ComplaintMemento
 * Stores an immutable snapshot of a Complaint's state for rollback/restoration.
 */
public class ComplaintMemento {
    private final int complaintId;
    private final String status;
    private final int technicianId;
    private final String technicianName;
    private final String department;
    private final String description;
    private final String priority;
    private final Timestamp savedAt;

    public ComplaintMemento(int complaintId, String status, int technicianId, 
                            String technicianName, String department, 
                            String description, String priority, Timestamp savedAt) {
        this.complaintId = complaintId;
        this.status = status;
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.department = department;
        this.description = description;
        this.priority = priority;
        this.savedAt = savedAt != null ? new Timestamp(savedAt.getTime()) : new Timestamp(System.currentTimeMillis());
    }

    public ComplaintMemento(int complaintId, String status) {
        this(complaintId, status, 0, null, null, null, null, new Timestamp(System.currentTimeMillis()));
    }

    public int getComplaintId() {
        return complaintId;
    }

    public String getStatus() {
        return status;
    }

    public int getTechnicianId() {
        return technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public String getDepartment() {
        return department;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public Timestamp getSavedAt() {
        return savedAt != null ? new Timestamp(savedAt.getTime()) : null;
    }

    @Override
    public String toString() {
        return "ComplaintMemento{" +
                "complaintId=" + complaintId +
                ", status='" + status + '\'' +
                ", technicianId=" + technicianId +
                ", technicianName='" + technicianName + '\'' +
                ", department='" + department + '\'' +
                ", priority='" + priority + '\'' +
                ", savedAt=" + savedAt +
                '}';
    }
}

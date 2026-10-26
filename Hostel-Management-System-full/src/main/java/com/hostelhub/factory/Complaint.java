package com.hostelhub.factory;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.memento.ComplaintMemento;

import java.sql.Timestamp;

public class Complaint {
    private int id;
    private int studentId;
    private String studentName;
    private String roomNo;
    private String type;
    private String department;
    private String description;
    private String priority;
    private String status; // PENDING, ASSIGNED, IN_PROGRESS, RESOLVED
    private int technicianId;
    private String technicianName;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Complaint() {
        this.status = "PENDING";
    }

    public Complaint(String type, int studentId, String roomNo, String description, String priority) {
        this.type = type;
        this.studentId = studentId;
        this.roomNo = roomNo;
        this.description = description;
        this.priority = priority;
        this.status = "PENDING";
    }

    public Complaint(String type, int studentId, String description, String priority) {
        this(type, studentId, "", description, priority);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTechnicianId() { return technicianId; }
    public void setTechnicianId(int technicianId) { this.technicianId = technicianId; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // -------------------------------------------------------
    // Builder Pattern integration
    // -------------------------------------------------------
    /**
     * Returns a new ComplaintBuilder for fluent complaint construction.
     */
    public static ComplaintBuilder builder() {
        return new ComplaintBuilder();
    }

    // -------------------------------------------------------
    // Memento Pattern integration (Originator role)
    // -------------------------------------------------------
    /**
     * Creates and returns a snapshot (memento) of this complaint's current state.
     */
    public ComplaintMemento saveState() {
        return new ComplaintMemento(
                this.id,
                this.status,
                this.technicianId,
                this.technicianName,
                this.department,
                this.description,
                this.priority,
                this.updatedAt
        );
    }

    /**
     * Restores this complaint's state from the given memento snapshot.
     */
    public void restoreState(ComplaintMemento memento) {
        if (memento == null) return;
        this.status = memento.getStatus();
        this.technicianId = memento.getTechnicianId();
        this.technicianName = memento.getTechnicianName();
        if (memento.getDepartment() != null) this.department = memento.getDepartment();
        if (memento.getDescription() != null) this.description = memento.getDescription();
        if (memento.getPriority() != null) this.priority = memento.getPriority();
    }
}

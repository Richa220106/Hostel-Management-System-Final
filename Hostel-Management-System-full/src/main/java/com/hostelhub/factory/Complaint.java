package com.hostelhub.factory;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.memento.ComplaintMemento;
import com.hostelhub.prototype.Prototype;

import java.sql.Timestamp;

public class Complaint implements Prototype<Complaint>, Cloneable {
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

    // -------------------------------------------------------
    // Prototype Pattern integration
    // -------------------------------------------------------
    /**
     * Prototype Pattern: Clones this complaint into a fresh complaint instance.
     * Deep-copies the complaint template/core fields (type, department, description, priority),
     * while correctly initializing and resetting runtime/identity fields:
     * - id is reset to 0 (so database generates a new unique ID)
     * - studentId is reset to 0
     * - studentName is reset to null
     * - roomNo is reset to empty string
     * - status is reset to "PENDING" (never completed/resolved/assigned)
     * - technicianId is reset to 0
     * - technicianName is reset to null
     * - createdAt & updatedAt are cleared to null
     */
    @Override
    public Complaint clone() {
        Complaint copy = new Complaint();
        copy.setId(0);
        copy.setType(this.type);
        copy.setDepartment(this.department != null ? this.department : "General");
        copy.setDescription(this.description);
        copy.setPriority(this.priority != null ? this.priority : "MEDIUM");
        copy.setStatus("PENDING");
        copy.setStudentId(0);
        copy.setStudentName(null);
        copy.setRoomNo("");
        copy.setTechnicianId(0);
        copy.setTechnicianName(null);
        copy.setCreatedAt(null);
        copy.setUpdatedAt(null);
        return copy;
    }

    /**
     * Clones this complaint and specializes it for a specific student and room.
     */
    public Complaint cloneForStudent(int studentId, String roomNo) {
        Complaint copy = this.clone();
        copy.setStudentId(studentId);
        copy.setRoomNo(roomNo != null ? roomNo : "");
        return copy;
    }

    /**
     * Clones this complaint, specializes it for a student and room, and optionally appends or overrides description details.
     */
    public Complaint cloneWith(int studentId, String roomNo, String customDescription) {
        Complaint copy = this.cloneForStudent(studentId, roomNo);
        if (customDescription != null && !customDescription.trim().isEmpty()) {
            if (copy.getDescription() == null || copy.getDescription().trim().isEmpty()) {
                copy.setDescription(customDescription.trim());
            } else {
                copy.setDescription(copy.getDescription() + " - " + customDescription.trim());
            }
        }
        return copy;
    }

    /**
     * Prototype and Builder integration:
     * Converts this Complaint prototype into a ComplaintBuilder pre-populated with
     * this prototype's core attributes, ready for fluent modifications.
     */
    public ComplaintBuilder toBuilder() {
        return ComplaintBuilder.fromPrototype(this);
    }
}

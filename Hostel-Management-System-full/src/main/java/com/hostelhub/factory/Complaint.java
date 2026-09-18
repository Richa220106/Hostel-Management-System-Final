package com.hostelhub.factory;

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
}

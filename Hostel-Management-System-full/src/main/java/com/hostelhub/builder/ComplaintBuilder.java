package com.hostelhub.builder;

import com.hostelhub.factory.Complaint;
import java.sql.Timestamp;

/**
 * Builder Pattern: ComplaintBuilder
 * Provides a flexible, fluent step-by-step API for constructing Complaint objects.
 */
public class ComplaintBuilder {
    private int id;
    private int studentId;
    private String studentName;
    private String roomNo = "";
    private String type = "Maintenance";
    private String department = "General";
    private String description = "";
    private String priority = "MEDIUM";
    private String status = "PENDING";
    private int technicianId;
    private String technicianName;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ComplaintBuilder() {
    }

    public static ComplaintBuilder builder() {
        return new ComplaintBuilder();
    }

    public ComplaintBuilder id(int id) {
        this.id = id;
        return this;
    }

    public ComplaintBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public ComplaintBuilder studentId(int studentId) {
        this.studentId = studentId;
        return this;
    }

    public ComplaintBuilder setStudentId(int studentId) {
        this.studentId = studentId;
        return this;
    }

    public ComplaintBuilder studentName(String studentName) {
        this.studentName = studentName;
        return this;
    }

    public ComplaintBuilder setStudentName(String studentName) {
        this.studentName = studentName;
        return this;
    }

    public ComplaintBuilder roomNo(String roomNo) {
        this.roomNo = roomNo;
        return this;
    }

    public ComplaintBuilder setRoomNo(String roomNo) {
        this.roomNo = roomNo;
        return this;
    }

    public ComplaintBuilder type(String type) {
        this.type = type;
        return this;
    }

    public ComplaintBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public ComplaintBuilder department(String department) {
        this.department = department;
        return this;
    }

    public ComplaintBuilder setDepartment(String department) {
        this.department = department;
        return this;
    }

    public ComplaintBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ComplaintBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ComplaintBuilder priority(String priority) {
        this.priority = priority;
        return this;
    }

    public ComplaintBuilder setPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public ComplaintBuilder status(String status) {
        this.status = status;
        return this;
    }

    public ComplaintBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public ComplaintBuilder technicianId(int technicianId) {
        this.technicianId = technicianId;
        return this;
    }

    public ComplaintBuilder setTechnicianId(int technicianId) {
        this.technicianId = technicianId;
        return this;
    }

    public ComplaintBuilder technicianName(String technicianName) {
        this.technicianName = technicianName;
        return this;
    }

    public ComplaintBuilder setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
        return this;
    }

    public ComplaintBuilder createdAt(Timestamp createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ComplaintBuilder setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ComplaintBuilder updatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ComplaintBuilder setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Builds and returns a fully constructed Complaint instance.
     */
    public Complaint build() {
        Complaint complaint = new Complaint();
        complaint.setId(this.id);
        complaint.setStudentId(this.studentId);
        complaint.setStudentName(this.studentName);
        complaint.setRoomNo(this.roomNo);
        complaint.setType(this.type);
        complaint.setDepartment(this.department);
        complaint.setDescription(this.description);
        complaint.setPriority(this.priority);
        complaint.setStatus(this.status != null ? this.status : "PENDING");
        complaint.setTechnicianId(this.technicianId);
        complaint.setTechnicianName(this.technicianName);
        complaint.setCreatedAt(this.createdAt);
        complaint.setUpdatedAt(this.updatedAt);
        return complaint;
    }
}

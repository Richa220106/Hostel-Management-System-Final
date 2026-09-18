package com.hostelhub.model;

import java.sql.Timestamp;

public class Visitor {
    private int id;
    private int studentId;
    private String studentName;
    private String roomNo;
    private String visitorName;
    private String contact;
    private String visitDate;
    private String entryTime;
    private String exitTime;
    private String reason;
    private String status; // PENDING, APPROVED, EXITED

    public Visitor() {}

    public Visitor(int id, int studentId, String roomNo, String visitorName, String contact, String visitDate, String entryTime, String exitTime, String reason, String status) {
        this.id = id;
        this.studentId = studentId;
        this.roomNo = roomNo;
        this.visitorName = visitorName;
        this.contact = contact;
        this.visitDate = visitDate;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.reason = reason;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
    public String getEntryTime() { return entryTime; }
    public void setEntryTime(String entryTime) { this.entryTime = entryTime; }
    public String getExitTime() { return exitTime; }
    public void setExitTime(String exitTime) { this.exitTime = exitTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

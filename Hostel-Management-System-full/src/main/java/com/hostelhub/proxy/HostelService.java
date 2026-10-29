package com.hostelhub.proxy;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.factory.Complaint;
import com.hostelhub.memento.ComplaintHistory;
import com.hostelhub.model.Notification;
import com.hostelhub.model.Room;
import com.hostelhub.model.Technician;
import com.hostelhub.model.User;
import com.hostelhub.model.Visitor;

import com.hostelhub.prototype.ComplaintPrototypeRegistry;
import com.hostelhub.strategy.TechnicianAssignmentStrategy;

import java.util.List;

public interface HostelService {
    // Actions any authenticated user can do
    User viewProfile();

    // Complaint operations
    Complaint createComplaint(Complaint complaint);
    Complaint createComplaint(ComplaintBuilder builder);   // Builder overload
    List<Complaint> getComplaints();
    Complaint getComplaintById(int id);

    // Prototype operations: create from template or clone existing complaint
    Complaint createComplaintFromTemplate(String templateKey, String customNote);
    Complaint cloneComplaint(int complaintId, String customNote);
    List<String> getAvailableComplaintTemplates();
    ComplaintPrototypeRegistry getPrototypeRegistry();

    // Warden-only complaint actions
    void updateComplaintStatus(int complaintId, String status);
    void assignTechnician(int complaintId, int technicianId);

    // Strategy operations: configure technician assignment policy
    void setTechnicianAssignmentStrategy(TechnicianAssignmentStrategy strategy);
    TechnicianAssignmentStrategy getTechnicianAssignmentStrategy();

    // Memento: restore and history (Warden only)
    Complaint restoreComplaintStatus(int complaintId);
    ComplaintHistory getComplaintHistory();

    // Room operations
    List<Room> getRooms();
    void allocateRoom(int studentId, String roomNo);
    void addRoom(Room room);

    // Visitor operations
    List<Visitor> getVisitors();
    void registerVisitor(Visitor visitor);
    void updateVisitorStatus(int visitorId, String status);

    // Technician operations (Warden only)
    List<Technician> getTechnicians();
    void addTechnician(Technician technician);
    void updateTechnicianStatus(int technicianId, String status);

    // Notification operations
    List<Notification> getNotifications();
    void markNotificationAsRead(int notificationId);
}

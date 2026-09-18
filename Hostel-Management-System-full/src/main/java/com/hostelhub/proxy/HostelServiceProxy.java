package com.hostelhub.proxy;

import com.hostelhub.factory.Complaint;
import com.hostelhub.model.Notification;
import com.hostelhub.model.Room;
import com.hostelhub.model.Technician;
import com.hostelhub.model.User;
import com.hostelhub.model.Visitor;

import java.util.List;

public class HostelServiceProxy implements HostelService {
    private RealHostelService realService;
    private User currentUser;

    public HostelServiceProxy(User currentUser) {
        this.currentUser = currentUser;
        this.realService = new RealHostelService(currentUser);
    }

    private boolean isAuthenticated() {
        return currentUser != null;
    }

    private boolean isWarden() {
        return currentUser != null && "WARDEN".equalsIgnoreCase(currentUser.getRole());
    }

    private boolean isStudent() {
        return currentUser != null && "STUDENT".equalsIgnoreCase(currentUser.getRole());
    }

    @Override
    public User viewProfile() {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: You must be logged in.");
        }
        return realService.viewProfile();
    }

    @Override
    public Complaint createComplaint(Complaint complaint) {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Please log in to file a complaint.");
        }
        // Students and wardens can both submit complaints, but set studentId to current user if student
        if (isStudent() && complaint.getStudentId() == 0) {
            complaint.setStudentId(currentUser.getId());
            if (complaint.getRoomNo() == null || complaint.getRoomNo().isEmpty()) {
                complaint.setRoomNo(currentUser.getRoomNo());
            }
        }
        return realService.createComplaint(complaint);
    }

    @Override
    public List<Complaint> getComplaints() {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Authentication required.");
        }
        // Real service automatically scopes to current student if student, or all if warden
        return realService.getComplaints();
    }

    @Override
    public Complaint getComplaintById(int id) {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Authentication required.");
        }
        Complaint c = realService.getComplaintById(id);
        if (c != null && isStudent() && c.getStudentId() != currentUser.getId()) {
            throw new SecurityException("Access Denied: You cannot view complaints from other students.");
        }
        return c;
    }

    @Override
    public void updateComplaintStatus(int complaintId, String status) {
        if (!isWarden()) {
            System.err.println("Proxy Security Violation: Student attempted to update complaint status!");
            throw new SecurityException("Access Denied: Only Wardens can update complaint status.");
        }
        realService.updateComplaintStatus(complaintId, status);
    }

    @Override
    public void assignTechnician(int complaintId, int technicianId) {
        if (!isWarden()) {
            System.err.println("Proxy Security Violation: Student attempted to manually assign technician!");
            throw new SecurityException("Access Denied: Only Wardens can assign technicians.");
        }
        realService.assignTechnician(complaintId, technicianId);
    }

    @Override
    public List<Room> getRooms() {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Authentication required.");
        }
        return realService.getRooms();
    }

    @Override
    public void allocateRoom(int studentId, String roomNo) {
        if (!isWarden()) {
            System.err.println("Proxy Security Violation: Student attempted to allocate room!");
            throw new SecurityException("Access Denied: Only Wardens can allocate rooms.");
        }
        realService.allocateRoom(studentId, roomNo);
    }

    @Override
    public void addRoom(Room room) {
        if (!isWarden()) {
            System.err.println("Proxy Security Violation: Student attempted to add room!");
            throw new SecurityException("Access Denied: Only Wardens can add rooms.");
        }
        realService.addRoom(room);
    }

    @Override
    public List<Visitor> getVisitors() {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Authentication required.");
        }
        return realService.getVisitors();
    }

    @Override
    public void registerVisitor(Visitor visitor) {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Authentication required.");
        }
        if (isStudent()) {
            visitor.setStudentId(currentUser.getId());
            if (visitor.getRoomNo() == null || visitor.getRoomNo().isEmpty()) {
                visitor.setRoomNo(currentUser.getRoomNo());
            }
        }
        realService.registerVisitor(visitor);
    }

    @Override
    public void updateVisitorStatus(int visitorId, String status) {
        if (!isWarden()) {
            throw new SecurityException("Access Denied: Only Wardens can approve or modify visitor statuses.");
        }
        realService.updateVisitorStatus(visitorId, status);
    }

    @Override
    public List<Technician> getTechnicians() {
        if (!isWarden()) {
            throw new SecurityException("Access Denied: Only Wardens can access technician management.");
        }
        return realService.getTechnicians();
    }

    @Override
    public void addTechnician(Technician technician) {
        if (!isWarden()) {
            throw new SecurityException("Access Denied: Only Wardens can add technicians.");
        }
        realService.addTechnician(technician);
    }

    @Override
    public void updateTechnicianStatus(int technicianId, String status) {
        if (!isWarden()) {
            throw new SecurityException("Access Denied: Only Wardens can update technician status.");
        }
        realService.updateTechnicianStatus(technicianId, status);
    }

    @Override
    public List<Notification> getNotifications() {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Authentication required.");
        }
        return realService.getNotifications();
    }

    @Override
    public void markNotificationAsRead(int notificationId) {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Authentication required.");
        }
        realService.markNotificationAsRead(notificationId);
    }
}
